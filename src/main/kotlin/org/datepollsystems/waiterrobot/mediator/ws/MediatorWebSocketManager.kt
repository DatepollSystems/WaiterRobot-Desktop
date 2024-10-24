package org.datepollsystems.waiterrobot.mediator.ws

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.datepollsystems.waiterrobot.mediator.core.di.injectLoggerForClass
import org.datepollsystems.waiterrobot.mediator.printer.service.PrinterService
import org.datepollsystems.waiterrobot.mediator.utils.SuspendingExponentialBackoff
import org.datepollsystems.waiterrobot.mediator.ws.messages.AbstractWsMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.WsMessageBody
import org.koin.core.component.KoinComponent
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

private const val MAX_CONSECUTIVE_FAILS = 10

/**
 * Handles a websocket session and auto recovers on an exception in the session.
 * For auto recover an exponential backoff is used.
 * @author Fabian Schedler
 */
class MediatorWebSocketManager : KoinComponent {
    private lateinit var session: MediatorWebSocketSession

    private val logger by injectLoggerForClass()
    private val wsClient = createWsClient(logger = logger)
    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val closedIntentional = AtomicBoolean(false)
    private val closed = AtomicBoolean(false)
    private val registerMessages: MutableSet<AbstractWsMessage<WsMessageBody>> = mutableSetOf()
    private var registerCalled = false
    private val registerLock = Object()
    private val handlers: HandlerMap = mutableMapOf()

    private var websocketExceptionCount = 0 // Counter for consecutive exceptions
    private var hasPrintedWebsocketError = false

    @Suppress("MagicNumber")
    private val suspendingBackoff = SuspendingExponentialBackoff(
        initialDelay = Duration.ofMillis(500),
        resetAfter = Duration.ofMinutes(2), // When a connection last for 2 minutes reset the backoff counter
        maxBackoffTime = Duration.ofSeconds(5), // Wait a maximum of 5 seconds for a connection retry
        name = "WebSocket auto recovery"
    )

    private val receiveChannel = Channel<AbstractWsMessage<WsMessageBody>>(CHANNEL_BUFFER_SIZE)
    private val sendChannel = Channel<AbstractWsMessage<WsMessageBody>>(CHANNEL_BUFFER_SIZE) {
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

        if (this::session.isInitialized) {
            logger.i("Auto Restarting WebSocketSession")
        } else {
            logger.i("Starting WebSockeSession")
        }
        session = MediatorWebSocketSession(wsClient, sendChannel, receiveChannel)

        managerScope.launch(CoroutineName("StartWsSession")) {
            suspendingBackoff.acquire()
            @Suppress("TooGenericExceptionCaught")
            try {
                session.start().invokeOnCompletion { e ->
                    closeCurrentSession()
                    // Handle auto reconnect.
                    // This should only be triggered on Errors as ktor already handles connection loss internally
                    if (!closedIntentional.get()) {
                        logger.w(e) { "WebSocket session completed" }
                        handleConsecutiveWebsocketErrors(e)
                        suspendingBackoff.backoff(e)
                        startWatching()
                    } else {
                        logger.d(e) { "WebSocket session completed" }
                    }
                }
                setIsConnected(true)
                handleConsecutiveWebsocketErrors()
            } catch (e: Exception) {
                closeCurrentSession()
                handleConsecutiveWebsocketErrors(e)
                suspendingBackoff.backoff(e)
                return@launch startWatching()
            }
            synchronized(registerLock) {
                registerMessages.forEach { send(it) }
                registerCalled = true
            }
        }
    }

    private fun handleConsecutiveWebsocketErrors(e: Throwable? = null) {
        if (e != null) {
            if (!hasPrintedWebsocketError && websocketExceptionCount >= MAX_CONSECUTIVE_FAILS) {
                PrinterService.printNetworkDisconnect()
                hasPrintedWebsocketError = true

                return
            }

            websocketExceptionCount++
        } else {
            websocketExceptionCount = 0
            hasPrintedWebsocketError = false
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
            logger.w("Replaced handler for class ${clazz.simpleName}")
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

    // TODO handle session "switch" (still referencing old session which ist not active any more
    //  -> reemit message (also reemit messages which were not sent on the old session?
    //  or not because this message may caused an error -> infinite loop?)
    /**
     * Send a message
     */
    fun <T : WsMessageBody> send(message: AbstractWsMessage<T>) {
        check(!closed.get()) { "SocketManager is already closed" }
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
                    logger.w("No handler for message type '${message::class.simpleName}' found")
                } else {
                    // Launch on managerScope (SuperVisorJob) so that a failing handlers don't cancel the whole handler
                    managerScope.launch(CoroutineName("WsMessageHandler")) {
                        @Suppress("TooGenericExceptionCaught")
                        try {
                            handler(message)
                        } catch (e: Exception) {
                            logger.e(e) { "Handler for message: $message failed with: ${e.message}" }
                        }
                    }
                }
            }
        }
    }

    companion object {
        // TODO evaluate bufferSize
        private const val CHANNEL_BUFFER_SIZE = 10
    }
}

typealias HandlerMap = MutableMap<KClass<out AbstractWsMessage<WsMessageBody>>, WsMessageHandler<out WsMessageBody>>
