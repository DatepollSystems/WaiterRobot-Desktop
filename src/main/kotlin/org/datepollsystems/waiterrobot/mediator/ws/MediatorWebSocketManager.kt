package org.datepollsystems.waiterrobot.mediator.ws

import kotlinx.coroutines.*
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.utils.SuspendingExponentialBackoff
import org.datepollsystems.waiterrobot.mediator.ws.messages.AbstractWsMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.WsMessageBody
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

/**
 * Handles a websocket session and auto recovers on an exception in the session.
 * For auto recover an exponential backoff is used.
 * @author Fabian Schedler
 */
class MediatorWebSocketManager {
    private lateinit var session: MediatorWebSocketSession

    private val wsClient = createWsClient(Config.WS_NETWORK_LOGGING)
    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val closedIntentional = AtomicBoolean(false)
    private val closed = AtomicBoolean(false)
    private val registerMessages: MutableSet<AbstractWsMessage<WsMessageBody>> = mutableSetOf()
    private var registerCalled = false
    private val registerLock = Object()
    private val handlers: MutableMap<KClass<out AbstractWsMessage<WsMessageBody>>, WsMessageHandler<out WsMessageBody>> =
        mutableMapOf()
    private val suspendingBackoff =
        SuspendingExponentialBackoff(
            initialDelay = Duration.ofSeconds(1),
            maxBackoffs = 10,
            resetAfter = Duration.ofMinutes(2),
            name = "WebSocket auto recovery"
        )

    init {
        startWatching()
    }

    private fun startWatching() {
        if (closed.get()) return

        // TODO logger
        if (this::session.isInitialized) {
            println("Auto Restarting WebSocketSession")
        } else {
            println("Starting WebSockeSession")
        }
        session = MediatorWebSocketSession(wsClient, this::handleMessageThrowing)

        managerScope.launch {
            suspendingBackoff.acquire()
            session.start().invokeOnCompletion {
                closeCurrentSession()
                // Handle auto reconnect. This should only be triggered on Errors as ktor already handles connection loss internally
                if (!closedIntentional.get()) {
                    suspendingBackoff.backoff(it)
                    startWatching()
                }
            }
            synchronized(registerLock) {
                registerMessages.forEach { session.send(it) }
                registerCalled = true
            }
        }
    }

    fun close() {
        if (closed.getAndSet(true)) return
        closedIntentional.set(true)
        closeCurrentSession()
        managerScope.cancel()
    }

    private fun closeCurrentSession() {
        registerCalled = false
        runBlocking(NonCancellable) { session.close() }
    }

    /**
     * Register a [handler] for a specific MessageType of [clazz]. Overrides existing handler for that type of message
     */
    fun <T : WsMessageBody> handle(clazz: KClass<out AbstractWsMessage<T>>, handler: WsMessageHandler<T>) {
        @Suppress("UNCHECKED_CAST") // T must be a subtype of WsMessageBody (see function signature)
        handler as WsMessageHandler<WsMessageBody>
        if (handlers.put(clazz, handler) != null) {
            // This is probably not what we want -> log it
            println("Replaced handler for class ${clazz.simpleName}") // TODO logger
        }
    }

    /**
     * Register a [handler] for a specific MessageType [T]. Overrides existing handler for that type of message
     */
    inline fun <reified T : AbstractWsMessage<WsMessageBody>> handle(noinline handler: suspend (T) -> Unit) {
        @Suppress("UNCHECKED_CAST") // T must be a subtype of WsMessageBody (see function signature)
        handler as WsMessageHandler<WsMessageBody>
        handle(T::class, handler)
    }

    // TODO handle session "switch" (still referencing old session which ist not active any more -> reemit message (also reemit messages which were not sent on the old session? or not because this message may caused an error -> infinite loop?)
    /**
     * Send a message
     */
    fun <T : WsMessageBody> send(message: AbstractWsMessage<T>) {
        if (closed.get()) throw IllegalStateException("SocketManager is already closed")
        session.send(message)
    }

    /**
     * Add a message which should be always send right after a connection was established.
     * When the session "breaks" and a new one is created these messages are sent again.
     */
    fun <T : WsMessageBody> addRegisterMessage(message: AbstractWsMessage<T>) {
        if (closed.get()) return
        synchronized(registerLock) {
            registerMessages.add(message)
            if (registerCalled) session.send(message)
        }
    }

    private suspend fun handleMessageThrowing(message: AbstractWsMessage<WsMessageBody>) {
        val handler = handlers[message::class]
        if (handler == null) {
            println("No handler for message type '${message::class.simpleName}' found") // TODO logger
            return
        }
        handler(message)
    }
}