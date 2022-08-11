package org.datepollsystems.waiterrobot.mediator.ws

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.SerializationException
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.ws.messages.AbstractWsMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.WsMessageBody
import kotlin.coroutines.coroutineContext

/**
 * Ktor ws internally handles connection loss and reestablishes the connection without destroying the session.
 * When there is no connection messages are queued. TODO check how spring handles this
 * @author Fabian Schedler
 */
class MediatorWebSocketSession(
    private val client: HttpClient,
    private val outgoing: ReceiveChannel<AbstractWsMessage<WsMessageBody>>,
    private val incoming: SendChannel<AbstractWsMessage<WsMessageBody>>
) {
    private lateinit var session: DefaultClientWebSocketSession
    private lateinit var sessionScope: CoroutineScope

    /**
     * Initiates the websocket connection and returns the session handing job.
     * Suspends till the connection is established. The returned job completes when the session is closed.
     */
    suspend fun start(): Job {
        session = client.webSocketSession {
            url.takeFrom(Config.WS_URL)
            headers.append("organisationId", Settings.organisationId.toString())
        }
        session.ensureActive()
        sessionScope = CoroutineScope(Job(session.coroutineContext.job) + Dispatchers.IO)
        sessionScope.launch(CoroutineName("WsSendHandler")) { handleSend() }
        sessionScope.launch(CoroutineName("WsReceiveHandler")) { handleIncomingMessage() }
        sessionScope.ensureActive()
        return sessionScope.coroutineContext.job
    }

    private suspend fun handleSend() {
        while (coroutineContext.isActive) { // Keep listening as long as the coroutine is active
            try {
                val message = outgoing.receive()
                println("Sending message: $message") // TODO logger
                session.sendSerialized(message)
            } catch (e: ContentConvertException) {
                println("Could not convert outgoing message: $e") // TODO logger
            } catch (e: SerializationException) {
                println("Could not serialize outgoing message: $e") // TODO logger
            } catch (e: Exception) {
                sessionScope.cancel(
                    e as? CancellationException ?: CancellationException("Incoming message handler failed", e)
                )
            }
        }
        println("Send handler finished")
    }

    private suspend fun handleIncomingMessage() {
        while (coroutineContext.isActive) { // Keep listening as long as the coroutine is active
            try {
                val message = session.receiveDeserialized<AbstractWsMessage<WsMessageBody>>()
                if (Config.WS_NETWORK_LOGGING) println("Got message: $message") // TODO logger
                incoming.send(message)
            } catch (e: ContentConvertException) {
                println("Could not convert incoming message: $e") // TODO logger
            } catch (e: SerializationException) {
                println("Could not deserialize incoming message: $e") // TODO logger
            } catch (e: Exception) {
                coroutineContext.cancel(
                    e as? CancellationException ?: CancellationException("Incoming message handler failed", e)
                )
            }
        }
        println("Receive handler finished")
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