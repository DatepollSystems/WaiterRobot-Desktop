package org.datepollsystems.waiterrobot.mediator.ws

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val wsClient = createWsClient()
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
            initialDelay = Duration.ofMillis(500),
            resetAfter = Duration.ofMinutes(2), // When a connection last for 2 minutes reset the backoff counter
            maxBackoffTime = Duration.ofSeconds(5), // Wait a maximum of 5 seconds for a connection retry
            name = "WebSocket auto recovery"
        )

    // TODO evaluate bufferSize
    private val receiveChannel = Channel<AbstractWsMessage<WsMessageBody>>(10)
    private val sendChannel = Channel<AbstractWsMessage<WsMessageBody>>(10) {
        send(it) // Will be called when the session could not send the element -> queue it again
    }

    private val isConnectedState = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> get() = isConnectedState

    init {
        handleMessages()
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
        session = MediatorWebSocketSession(wsClient, sendChannel, receiveChannel)

        managerScope.launch(CoroutineName("StartWsSession")) {
            suspendingBackoff.acquire()
            try {
                session.start().invokeOnCompletion { e ->
                    println("WebSocket session completed" + (e?.let { " with exception: $it" } ?: ".")) // TODO logger
                    e?.printStackTrace()
                    closeCurrentSession()
                    // Handle auto reconnect. This should only be triggered on Errors as ktor already handles connection loss internally
                    if (!closedIntentional.get()) {
                        suspendingBackoff.backoff(e)
                        startWatching()
                    }
                }
                setIsConnected(true)
            } catch (e: Exception) {
                closeCurrentSession()
                suspendingBackoff.backoff(e)
                return@launch startWatching()
            }
            synchronized(registerLock) {
                registerMessages.forEach { send(it) }
                registerCalled = true
            }
        }
    }

    fun close() {
        if (closed.getAndSet(true)) return
        closedIntentional.set(true)
        closeCurrentSession()
        sendChannel.cancel()
        receiveChannel.cancel()
        managerScope.cancel()
    }

    private fun setIsConnected(isConnected: Boolean) {
        managerScope.launch { isConnectedState.emit(isConnected) }
    }

    private fun closeCurrentSession() {
        setIsConnected(false)
        registerCalled = false
        if (this::session.isInitialized) {
            runBlocking(NonCancellable) { session.close() }
        }
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
        managerScope.launch(CoroutineName("WsSendMessage")) { sendChannel.send(message) }
    }

    /**
     * Add a message which should be always send right after a connection was established.
     * When the session "breaks" and a new one is created these messages are sent again.
     */
    fun <T : WsMessageBody> addRegisterMessage(message: AbstractWsMessage<T>) {
        if (closed.get()) return
        synchronized(registerLock) {
            registerMessages.add(message)
            if (registerCalled) send(message)
        }
    }

    private fun handleMessages() {
        managerScope.launch(CoroutineName("WsHandleMessage")) {
            receiveChannel.consumeEach { message ->
                val handler = handlers[message::class]
                if (handler == null) {
                    println("No handler for message type '${message::class.simpleName}' found") // TODO logger
                } else {
                    // Launch on managerScope (SuperVisorJob) so that a failing handler does not cancel the whole handler
                    managerScope.launch(CoroutineName("WsMessageHandler")) {
                        try {
                            handler(message)
                        } catch (e: Exception) {
                            println("Handler for message: $message failed with: ${e.message}") // TODO logger
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}