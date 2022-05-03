package org.datepollsystems.waiterrobot.mediator.ws

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.api.AuthApi
import org.datepollsystems.waiterrobot.mediator.api.configureAuth
import org.datepollsystems.waiterrobot.mediator.api.createClient
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.ws.messages.AbstractWsMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintPdfBody
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintPdfMessage
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

    suspend fun start() {
        if (closed.get()) throw IllegalStateException("WsClient was already closed")
        if (isStarted.getAndSet(true)) throw IllegalStateException("WsClient must be started only once")

        startUpJob.start() // Just needed for suspending the start till initialization is finished

        // Launch the websocket session and handling in the background and keep running
        scope.launch {
            client.wss(Config.WS_URL) {
                val sendMsgHandler = launch { handleSend() }
                val incomingMsgHandler = launch { handleIncomingMessage() }

                incomingMsgHandler.join() // Wait for completion
                sendMsgHandler.cancelAndJoin()
            }
        }

        // Wait and suspend till the send-flow is subscribed (-> websocket is ready)
        startUpJob.join()
    }

    fun stop() {
        if (!closed.compareAndSet(false, true)) return
        scope.coroutineContext.cancelChildren()
    }

    fun <T : WsMessageBody> handle(clazz: KClass<out AbstractWsMessage<T>>, handler: WsMessageHandler<T>) {
        @Suppress("UNCHECKED_CAST")
        handler as WsMessageHandler<WsMessageBody>
        handle(clazz.createType(), handler)
    }

    inline fun <reified T : AbstractWsMessage<WsMessageBody>> handle(noinline handler: suspend (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        handler as WsMessageHandler<WsMessageBody>
        handle(typeOf<T>(), handler)
    }

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
        while (scope.isActive) { // Keep listening as long as the coroutine is active
            try {
                val message = receiveDeserialized<AbstractWsMessage<WsMessageBody>>()
                println("Got message: $message")
                handlers[message::class.createType()]?.invoke(message)
                    ?: throw IllegalArgumentException("No handler implemented for message type '${message::class}'")
            } catch (e: CancellationException) { // Cancellation is fine and expected
            } catch (e: WebsocketContentConvertException) {
                // TODO log
                println("Receiver error: $e")
            } catch (e: Exception) {
                TODO("Handle websocket connection gone")
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.handleSend() {
        try {
            sendFlow.onSubscription {
                startUpJob.complete() // sendFlow has a subscriber -> client is ready
            }.collect {
                println("Sending message: $it")
                sendSerialized(it)
            }
        } catch (e: CancellationException) { // Cancellation is fine and expected
        } catch (e: WebsocketContentConvertException) {
            // TODO log
            println("Send Error: $e")
        } catch (e: Exception) {
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
    wsClient.handle<PrintPdfMessage> {
        println("Handler for PrintPdfMessage called with: $it")
    }

    // Login
    val tokens = AuthApi(createClient()).login("admin@admin.org", "admin")
    System.setProperty("accessToken", tokens.accessToken)
    System.setProperty("sessionToken", tokens.sessionToken!!)

    // Connect to the websocket
    // Start suspends till the websocket is ready
    wsClient.start()

    try {
        // Send a message to the server
        wsClient.send(
            PrintPdfMessage(
                httpStatus = 200,
                body = PrintPdfBody(id = 1, printerId = 1, file = PrintPdfBody.File(mime = "mime", data = "pdf/base64"))
            )
        )
    } catch (e: Exception) {
        println("Exception: $e")
    }

    launch {
        repeat(100) {
            println("I'm alive since $it sec.")
            delay(1000)
        }
    }.join() // Simulate some "application live time"
    println("Stopping client")
    wsClient.stop() // WsClient keeps running and handles sending/receiving in background till the client is stopped
    println("finished")
}