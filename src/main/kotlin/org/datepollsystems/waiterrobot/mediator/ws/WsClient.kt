package org.datepollsystems.waiterrobot.mediator.ws

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.serialization.json.Json
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

object WsClient {
    private val client by lazy { createWsClient() }
    private val sendFlow by lazy { MutableSharedFlow<AbstractWsMessage<WsMessageBody>>() }
    private val startUpJob: CompletableJob by lazy { Job() }
    private val isStarted: AtomicBoolean = AtomicBoolean(false)
    private val handlers: MutableMap<KType, (AbstractWsMessage<WsMessageBody>) -> Unit> = mutableMapOf()
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun start() {
        startUpJob.start() // Just needed for suspending the start till initialization is finished
        if (isStarted.getAndSet(true)) throw IllegalStateException("WsClient must be started only once")

        // Launch the websocket session and handling in the background and keep running
        scope.launch {
            client.webSocket(method = HttpMethod.Get, host = "127.0.0.1", port = 8080) {
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
        client.close()
        scope.cancel()
    }

    fun <T : WsMessageBody> handle(clazz: KClass<out AbstractWsMessage<T>>, handler: (AbstractWsMessage<T>) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        handler as (AbstractWsMessage<WsMessageBody>) -> Unit
        handle(clazz.createType(), handler)
    }

    inline fun <reified T : AbstractWsMessage<WsMessageBody>> handle(noinline handler: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        handler as (AbstractWsMessage<WsMessageBody>) -> Unit
        handle(typeOf<T>(), handler)
    }

    fun handle(type: KType, handler: (AbstractWsMessage<WsMessageBody>) -> Unit) {
        if (!type.isSubtypeOf(typeOf<AbstractWsMessage<WsMessageBody>>())) {
            // check this as handle has to be public, but we can't "put" a constraint on KType
            throw IllegalArgumentException("Handle can only accept subtypes of AbstractWsMessage")
        }
        handlers[type] = handler
    }

    fun <T : WsMessageBody> send(message: AbstractWsMessage<T>) {
        scope.launch { sendFlow.emit(message) }
    }

    private suspend fun DefaultClientWebSocketSession.handleIncomingMessage() {
        while (scope.isActive) { // Keep listening as long as the coroutine is active
            try {
                val message = receiveDeserialized<AbstractWsMessage<WsMessageBody>>()
                println("Got message: $message")
                handlers[message::class.createType()]?.invoke(message)
                    ?: throw IllegalArgumentException("No handler implemented for message type '${message::class}'")
            } catch (e: CancellationException) {
                // Cancellation is ok
                println("Receiver error: $e")
            } catch (e: Exception) {
                // TODO log
                println("Receiver error: $e")
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
        } catch (e: CancellationException) {
            // Cancellation is ok
            println("Receiver error: $e")
        } catch (e: Exception) {
            // TODO log
            println("Send Error: $e")
        }
    }
}

private fun createWsClient() = HttpClient {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json { ignoreUnknownKeys = true })
        pingInterval = 60 * 1000
    }
}


// Sample usage
fun main(): Unit = runBlocking {
    // Register a Handler for a specific websocket message
    WsClient.handle<PrintPdfMessage> {
        println("Handler for PrintPdfMessage called with: $it")
    }

    // Connect to the websocket
    // Start suspends till the websocket is ready
    WsClient.start()

    try {
        // Send a message to the server
        WsClient.send(
            PrintPdfMessage(
                httpStatus = 200,
                body = PrintPdfBody(id = 1, printerId = 1, file = PrintPdfBody.File(mime = "mime", data = "pdf/base64"))
            )
        )
    } catch (e: Exception) {
        println("Exception: $e")
    }

    delay(10000) // Simulate some "application live time"
    WsClient.stop() // WsClient keeps running and handles sending/receiving in background till the client is stopped
}