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
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.core.di.injectLoggerForClass
import org.datepollsystems.waiterrobot.mediator.ws.messages.AbstractWsMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.WsMessageBody
import org.koin.core.component.KoinComponent
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.coroutineContext

/**
 * Ktor ws internally handles connection loss and reestablishes the connection without destroying the session.
 * When there is no connection messages are queued.
 * @author Fabian Schedler
 */
class MediatorWebSocketSession(
    private val client: HttpClient,
    private val outgoing: ReceiveChannel<AbstractWsMessage<WsMessageBody>>,
    private val incoming: SendChannel<AbstractWsMessage<WsMessageBody>>
) : KoinComponent {
    private lateinit var session: DefaultClientWebSocketSession
    private lateinit var sessionScope: CoroutineScope

    private val logger by injectLoggerForClass()

    private val closed = AtomicBoolean(false)
    private val started = AtomicBoolean(false)

    /**
     * Initiates the websocket connection and returns the session handing job.
     * Suspends till the connection is established. The returned job completes when the session is closed.
     */
    suspend fun start(): Job {
        check(started.getAndSet(true)) { "Session already started" }
        session = client.webSocketSession {
            url.takeFrom(App.config.wsUrl)
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
            @Suppress("TooGenericExceptionCaught")
            try {
                val message = outgoing.receive()
                logger.d("Sending message: $message")
                session.sendSerialized(message)
            } catch (e: ContentConvertException) {
                logger.e(e) { "Could not convert outgoing message: $e" }
            } catch (e: SerializationException) {
                logger.e(e) { "Could not serialize outgoing message: $e" }
            } catch (e: Exception) {
                sessionScope.cancel(
                    e as? CancellationException ?: CancellationException("Incoming message handler failed", e)
                )
            }
        }
        logger.d("Send handler finished")
        close()
    }

    private suspend fun handleIncomingMessage() {
        while (coroutineContext.isActive) { // Keep listening as long as the coroutine is active
            @Suppress("TooGenericExceptionCaught")
            try {
                val message = session.receiveDeserialized<AbstractWsMessage<WsMessageBody>>()
                logger.d("Got message: $message")
                incoming.send(message)
            } catch (e: ContentConvertException) {
                logger.e(e) { "Could not convert incoming message: $e" }
            } catch (e: SerializationException) {
                logger.e(e) { "Could not deserialize incoming message: $e" }
            } catch (e: Exception) {
                coroutineContext.cancel(
                    e as? CancellationException ?: CancellationException("Incoming message handler failed", e)
                )
            }
        }
        logger.d("Receive handler finished")
        close()
    }

    suspend fun close() {
        try {
            if (closed.getAndSet(true)) return
            logger.i("Closing WebSocket session")
            session.close()
            sessionScope.cancel()
        } catch (_: Exception) {
        }
    }
}
