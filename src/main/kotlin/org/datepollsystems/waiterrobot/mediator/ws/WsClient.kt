package org.datepollsystems.waiterrobot.mediator.ws

import io.ktor.client.*
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
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintedPdfMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.WsMessageBody
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

typealias WsMessageHandler<T> = suspend (AbstractWsMessage<T>) -> Unit

class WsClient {
    private val client by lazy { createWsClient() }
    private val sendFlow by lazy { MutableSharedFlow<AbstractWsMessage<WsMessageBody>>() }
    private val startUpJob: CompletableJob by lazy { Job() }
    private val isStarted: AtomicBoolean = AtomicBoolean(false)
    private val closed: AtomicBoolean = AtomicBoolean(false)
    private val handlers: MutableMap<KType, WsMessageHandler<WsMessageBody>> = mutableMapOf()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var session: WebSocketSession? = null

    suspend fun start() {
        if (closed.get()) throw IllegalStateException("WsClient was already closed")
        if (isStarted.getAndSet(true)) throw IllegalStateException("WsClient must be started only once")

        startUpJob.start() // Just needed for suspending the start till initialization is finished

        // Launch the websocket session and handling in the background and keep running
        scope.launch {
            // When testing with local server change ".wss" to ".ws" (as no TLS possible)
            client.wss(Config.WS_URL) {
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

        // Wait and suspend till the send-flow is subscribed (-> websocket is ready)
        startUpJob.join()
    }

    suspend fun stop() {
        if (!closed.compareAndSet(false, true)) return
        if (session != null) {
            session?.close()
            session = null
            println("Ws session closed") // TODO logger
        }
        client.close()
        println("Ws Client closed") // TODO logger
        scope.cancel()
    }

    fun <T : WsMessageBody> handle(clazz: KClass<out AbstractWsMessage<T>>, handler: WsMessageHandler<T>) {
        @Suppress("UNCHECKED_CAST") // T mut be a subtype of WsMessageBody (see function signature)
        handler as WsMessageHandler<WsMessageBody>
        @Suppress("DEPRECATION")
        handle(clazz.createType(), handler)
    }

    inline fun <reified T : AbstractWsMessage<WsMessageBody>> handle(noinline handler: suspend (T) -> Unit) {
        @Suppress("UNCHECKED_CAST") // T mut be a subtype of WsMessageBody (see function signature)
        handler as WsMessageHandler<WsMessageBody>
        @Suppress("DEPRECATION")
        handle(typeOf<T>(), handler)
    }

    @Deprecated("Do not use this, use one of the other handle function") // To mark it as "do not use"
    fun handle(type: KType, handler: WsMessageHandler<WsMessageBody>) {
        if (!type.isSubtypeOf(typeOf<AbstractWsMessage<WsMessageBody>>())) {
            // check this as handle has to be public, but we can't "put" a constraint on KType
            throw IllegalArgumentException("Handle can only accept subtypes of AbstractWsMessage")
        }
        handlers[type] = handler
    }

    fun <T : WsMessageBody> send(message: AbstractWsMessage<T>) {
        if (closed.get()) throw IllegalStateException("WsClient is already closed")
        scope.launch { sendFlow.emit(message) }
    }

    private suspend fun DefaultClientWebSocketSession.handleIncomingMessage() {
        while (coroutineContext.isActive) { // Keep listening as long as the coroutine is active
            try {
                val message = receiveDeserialized<AbstractWsMessage<WsMessageBody>>()
                println("Got message: $message") // TODO logger
                handlers[message::class.createType()]?.invoke(message)
                    ?: throw IllegalArgumentException("No handler implemented for message type '${message::class}'")
            } catch (e: ClosedReceiveChannelException) {
                // Cancellation is fine and expected
                println("Canceled receiving") // TODO logger
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
                startUpJob.complete() // sendFlow has a subscriber -> client is ready
            }.collect {
                println("Sending message: $it") // TODO logger
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

private fun createWsClient() = HttpClient {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json { ignoreUnknownKeys = true })
        pingInterval = 60 * 1000
    }
    configureAuth()
}


// Sample usage
fun main(): Unit = runBlocking {
    val wsClient = WsClient()
    // Register a Handler for a specific websocket message
    wsClient.handle<PrintedPdfMessage> {
        println("Handler for PrintedPdfMessage called with: $it")
    }

    // Login
    val tokens = AuthApi(createClient()).login("admin@admin.org", "admin")
    Settings.accessToken = tokens.accessToken
    Settings.refreshToken = tokens.refreshToken!!

    // Connect to the websocket
    // Start suspends till the websocket is ready
    wsClient.start()

    try {
        // Send a message to the server
        wsClient.send(
            HelloMessage(
                httpStatus = 200,
                body = HelloMessage.Body(text = "Test Message")
            )
        )
    } catch (e: Exception) {
        println("Exception: $e")
    }

    launch {
        repeat(10) {
            println("I'm alive since $it sec.")
            delay(1000)
        }
    }.join() // Simulate some "application live time"
    println("Stopping client")
    wsClient.stop() // WsClient keeps running and handles sending/receiving in background till the client is stopped
    println("finished")
}