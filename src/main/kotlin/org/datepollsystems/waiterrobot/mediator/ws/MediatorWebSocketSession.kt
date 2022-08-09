package org.datepollsystems.waiterrobot.mediator.ws

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.SerializationException
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.ws.messages.AbstractWsMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.WsMessageBody

/**
 * Ktor ws internally handles connection loss and reestablishes the connection without destroying the session.
 * When there is no connection messages are queued. TODO check how spring handles this
 * @author Fabian Schedler
 */
class MediatorWebSocketSession(
    private val client: HttpClient,
    private val handleMessageThrowing: suspend (AbstractWsMessage<WsMessageBody>) -> Unit
) {
    private lateinit var session: DefaultClientWebSocketSession
    private val sessionScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val sendChannel: Channel<AbstractWsMessage<WsMessageBody>> = Channel()

    /**
     * Initiates the websocket connection and returns the session handing job.
     * Suspends till the connection is established. The returned job completes when the session is closed.
     */
    suspend fun start(): Job {
        session = client.webSocketSession {
            url.takeFrom(Config.WS_URL)
            headers.append("organisationId", Settings.organisationId.toString())
        }

        return sessionScope.launch {
            val sendJob = launch { session.handleSend() }
            launch { session.handleIncomingMessage() }.join()
            sendJob.cancel()
        }
    }

    fun <T : WsMessageBody> send(message: AbstractWsMessage<T>) {
        sessionScope.launch { sendChannel.send(message) }
    }

    private suspend fun DefaultClientWebSocketSession.handleSend() {
        while (true) { // Keep listening as long as the coroutine is active
            coroutineContext.ensureActive() // Cooperative  coroutine canceling
            try {
                val message = sendChannel.receive()
                println("Sending message: $message") // TODO logger
                sendSerialized(message)
            } catch (e: ContentConvertException) {
                println("Could not convert outgoing message: $e") // TODO logger
            } catch (e: SerializationException) {
                println("Could not serialize outgoing message: $e") // TODO logger
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.handleIncomingMessage() {
        while (true) { // Keep listening as long as the coroutine is active
            coroutineContext.ensureActive() // Cooperative  coroutine canceling
            try {
                val message = receiveDeserialized<AbstractWsMessage<WsMessageBody>>()
                if (Config.WS_NETWORK_LOGGING) println("Got message: $message") // TODO logger
                sessionScope.launch(SupervisorJob()) {
                    try {
                        handleMessageThrowing(message)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        println("Handler for message: $message failed with: ${e.message}") // TODO logger
                        e.printStackTrace()
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                // Ws channel was unexpectedly closed (probably) by the server (server side exception, connection loss)
                // TODO probably this should throw and then re-initiate a new connection?
                println("Websocket channel was closed $e") // TODO logger
                coroutineContext.cancel()
            } catch (e: ContentConvertException) {
                println("Could not convert incoming message: $e") // TODO logger
            } catch (e: SerializationException) {
                println("Could not deserialize incoming message: $e") // TODO logger
            }
        }
    }

    suspend fun close() {
        try {
            println("Closing WebSocket session")
            session.close()
            sessionScope.cancel()
        } catch (_: Exception) {
        }
    }
}