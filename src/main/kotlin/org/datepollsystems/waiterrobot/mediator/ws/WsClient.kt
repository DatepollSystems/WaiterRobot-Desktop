package org.datepollsystems.waiterrobot.mediator.ws

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.api.AuthApi
import org.datepollsystems.waiterrobot.mediator.api.configureAuth
import org.datepollsystems.waiterrobot.mediator.api.createClient
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.ws.messages.AbstractWsMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.HelloMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.HelloMessageResponse
import org.datepollsystems.waiterrobot.mediator.ws.messages.WsMessageBody
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

typealias WsMessageHandler<T> = suspend (AbstractWsMessage<T>) -> Unit

// TODO how to reestablish connection
object WsClient {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val _isReady: AtomicBoolean = AtomicBoolean(false)
    val isReady get() = _isReady.get()

    private val client by lazy { createWsClient(Config.WS_NETWORK_LOGGING) }
    private val sendFlow by lazy { MutableSharedFlow<AbstractWsMessage<WsMessageBody>>() }
    private val isStarted: AtomicBoolean = AtomicBoolean(false)
    private val closed: AtomicBoolean = AtomicBoolean(false)
    private val handlers: MutableMap<KClass<out AbstractWsMessage<*>>, WsMessageHandler<out WsMessageBody>> =
        mutableMapOf()
    private val onStartedListeners: MutableList<() -> Unit> = mutableListOf()
    private var session: WebSocketSession? = null

    fun connect() {
        if (isStarted.getAndSet(true)) return // Must be only called once
        if (closed.get()) throw IllegalStateException("WsClient is already closed")

        // Launch the websocket session and handling in the background and keep running
        scope.launch {
            // When testing with local server change ".wss" to ".ws" (as no TLS possible)
            client.wss(Config.WS_URL,
                request = {
                    // Add additional required headers
                    headers.append("organisationId", Settings.organisationId.toString())
                }
            ) {
                println("Ws session started") // TODO logger

                session = this
                val sendMsgHandler = launch { handleSend() }
                val incomingMsgHandler = launch { handleIncomingMessage() }

                println("Ws client launched in background") // TODO logger

                incomingMsgHandler.join() // Wait for completion
                println("Ws incoming handler job completed") // TODO logger

                sendMsgHandler.cancelAndJoin()
                println("Ws sending job completed") // TODO logger

                stop()
            }
        }
    }

    fun onReady(callback: () -> Unit) {
        if (closed.get()) return
        if (_isReady.get()) return callback()

        onStartedListeners.add(callback)
    }

    suspend fun stop() {
        if (!closed.compareAndSet(false, true)) return
        if (session != null) {
            session?.close()
            session = null
            println("Ws session closed") // TODO logger
        }
        if (isStarted.get()) {
            // Only close if the client was already created
            client.close()
            println("Ws Client closed") // TODO logger
        }
        scope.cancel()
    }

    fun <T : WsMessageBody> handle(clazz: KClass<out AbstractWsMessage<T>>, handler: WsMessageHandler<T>) {
        @Suppress("UNCHECKED_CAST") // T must be a subtype of WsMessageBody (see function signature)
        handler as WsMessageHandler<WsMessageBody>
        if (handlers.put(clazz, handler) != null) {
            // This is probably not what we want -> log it
            println("Replaced handler for class ${clazz.simpleName}") // TODO logger
        }
    }

    inline fun <reified T : AbstractWsMessage<WsMessageBody>> handle(noinline handler: suspend (T) -> Unit) {
        @Suppress("UNCHECKED_CAST") // T must be a subtype of WsMessageBody (see function signature)
        handler as WsMessageHandler<WsMessageBody>
        handle(T::class, handler)
    }

    fun <T : WsMessageBody> send(message: AbstractWsMessage<T>) {
        if (closed.get()) throw IllegalStateException("WsClient is already closed")
        if (!_isReady.get()) throw IllegalStateException("Can not send before WsClient is not running")
        scope.launch { sendFlow.emit(message) }
    }

    private suspend fun DefaultClientWebSocketSession.handleIncomingMessage() {
        while (coroutineContext.isActive) { // Keep listening as long as the coroutine is active
            try {
                val message = receiveDeserialized<AbstractWsMessage<WsMessageBody>>()
                if (Config.WS_NETWORK_LOGGING) println("Got message: $message") // TODO logger
                val handler = handlers[message::class]
                if (handler == null) {
                    println("No handler for message type '${message::class}' found") // TODO logger
                    continue
                }
                scope.launch(Dispatchers.IO) {
                    try {
                        handler.invoke(message)
                    } catch (e: CancellationException) {
                        // Cancellation is fine and expected
                        println("Canceled WebSocket message handler") // TODO logger
                    } catch (e: Exception) {
                        // TODO logger
                        println("Unexpected exception in WebSocket message handler for type '${message::class}'. Handle exceptions directly in the handle block!\n$e")
                        // TODO what should happen?
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                // Ws channel was unexpectedly closed (probably) by the server (server side exception, connection loss)
                // TODO probably this should throw and then re-initiate a new connection?
                println("Websocket channel was closed") // TODO logger
                coroutineContext.cancel()
            } catch (e: CancellationException) {
                // Cancellation is fine and expected
                println("Canceled receiving") // TODO logger
            } catch (e: ContentConvertException) {
                println("Could not convert incoming message: $e") // TODO logger
            } catch (e: SerializationException) {
                println("Could not deserialize incoming message: $e") // TODO logger
            } catch (e: Exception) {
                println("Websocket handleIncomingMessage error: $e") // TODO logger
                TODO("Handle websocket connection gone")
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.handleSend() {
        try {
            sendFlow.onSubscription {
                _isReady.set(true)
                onStartedListeners.forEach { it.invoke() }
                onStartedListeners.clear()
            }.collect {
                if (Config.WS_NETWORK_LOGGING) println("Sending message: $it") // TODO logger
                sendSerialized(it)
            }
        } catch (e: CancellationException) {
            // Cancellation is fine and expected
            println("Canceled sending") // TODO logger
        } catch (e: ContentConvertException) {
            println("Could not convert outgoing message: $e") // TODO logger
        } catch (e: SerializationException) {
            println("Could not serialize outgoing message: $e") // TODO logger
        } catch (e: Exception) {
            println("Websocket handleSend error: $e") // TODO logger
            TODO("Handle websocket connection gone")
        }
    }
}

private fun createWsClient(enableNetworkLogs: Boolean = false) = HttpClient {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json { ignoreUnknownKeys = true })
        pingInterval = 60 * 1000
        // Use compression (improve network usage especially for pdf transferring as internet connection may be very slow)
        // TODO test if it really brings benefits
        // TODO receiving does not work out of the box probably needs adaptions on the backend (how to tell spring to use compression for responses?)
        //  Maybe we should then also add compressMinSize as a header so the server also respects this or how does ktor determine if it must decompress incoming messages
        /*extensions {
            install(WebSocketDeflateExtension) {
                compressIfBiggerThan(500) // Do not compress very small messages as this would probably make them bigger
                // TODO some benchmarking needed for which minSize makes most sense for our type of data
            }
        }*/
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 50000
        socketTimeoutMillis =
            5 * 60 * 1000 // There should be a ping message every minute, so timeout if did not get multiple pings
    }
    configureAuth()
    if (enableNetworkLogs) {
        install(Logging) {
            // TODO use real logger
            logger = object : Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
            level = LogLevel.ALL
        }
    }
}


// Sample usage
fun main(): Unit = runBlocking {
    // Register a Handler for a specific websocket message
    WsClient.handle<HelloMessageResponse> {
        println("Handler for HelloMessageResponse called with: $it")

        if (it.body.text != "Hello second") {
            delay(2000)
            WsClient.send(
                HelloMessage(
                    httpStatus = 200,
                    body = HelloMessage.Body(text = "second")
                )
            )
        }
    }

    // Login
    val tokens = AuthApi(createClient()).login("admin@admin.org", "admin")
    Settings.accessToken = tokens.accessToken
    Settings.refreshToken = tokens.refreshToken!!

    // Connect to the websocket
    // Start suspends till the websocket is ready
    WsClient.connect()
    WsClient.onReady {
        try {
            // Send a message to the server
            WsClient.send(
                HelloMessage(
                    httpStatus = 200,
                    body = HelloMessage.Body(text = "Test WsClient")
                )
            )
        } catch (e: Exception) {
            println("Exception: $e")
        }
    }

    launch {
        repeat(10) {
            println("I'm alive since $it sec.")
            delay(1000)
        }
    }.join() // Simulate some "application live time"
    println("Stopping client")
    WsClient.stop() // WsClient keeps running and handles sending/receiving in background till the client is stopped
    println("finished")
}