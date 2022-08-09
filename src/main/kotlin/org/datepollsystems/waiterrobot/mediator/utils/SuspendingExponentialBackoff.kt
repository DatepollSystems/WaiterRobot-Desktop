package org.datepollsystems.waiterrobot.mediator.utils

import kotlinx.coroutines.*
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

/**
 * Simple suspending exponential backoff handler
 *
 * @param initialDelay delay on first backoff next backoff will be twice, then four times, ... that long. Must be > 0
 * @param maxBackoffs maximum number of possible backoffs -1 for Unlimited
 * @param resetAfter reset the backoff after a duration (starts then again with [initialDelay]) null for never. Must be > 0 or null
 * @param name for logging
 *
 * @author Fabian Schedler
 */
class SuspendingExponentialBackoff(
    initialDelay: Duration,
    private val maxBackoffs: Int = -1,
    private val resetAfter: Duration? = null,
    private val name: String,
) {
    private val backoffStage: AtomicInteger = AtomicInteger(0)
    private var lastBackoff: Instant = Instant.MIN
    private val resetScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val initialDelay = initialDelay.toMillis().toDouble()

    // TODO should we switch to a linear backoff from a specific stage (see https://images.app.goo.gl/UFGUPFcESLMRDbMx8)
    private val currentDelay: Duration
        get() = Duration.ofMillis((initialDelay * 2f.pow(backoffStage.get())).toLong())

    init {
        require(!initialDelay.isNegative && !initialDelay.isZero) { "initialDelay must be greater than 0" }
        if (resetAfter != null) {
            require(!resetAfter.isNegative && !resetAfter.isZero) { "resetAfter must be greater than 0 or null" }
        }
    }

    /**
     * Call when the next execution should be "backoffed"
     */
    fun backoff(errorOnLimit: Throwable? = null) {
        resetScope.coroutineContext.cancelChildren()

        if (backoffStage.incrementAndGet() > maxBackoffs) {
            throw errorOnLimit ?: TimeoutException("Reached backoff limit")
        }

        lastBackoff = Instant.now()

        resetAfter?.let {
            resetScope.launch {
                delay(it.toMillis())
                reset()
            }
        }
    }

    /**
     * Reset the backoff stage. Use then again [initialDelay] for next backoff
     */
    fun reset() {
        resetScope.coroutineContext.cancelChildren()
        backoffStage.set(0)
    }

    /**
     * Suspends exponentially
     * Call before "restarting" the task which should be "backoffed"
     * CAUTION this function can suspend forever if [maxBackoffs] < 0 (infinite loop)
     */
    suspend fun acquire() {
        val nextExecution = lastBackoff + currentDelay
        val now = Instant.now()
        if (now.isAfter(nextExecution)) return

        val delayDuration = Duration.between(now, nextExecution)
        println("Backoff task \"$name\" for ${delayDuration.seconds}s")
        delay(delayDuration.toMillis())
        acquire() // Ensure that there was not a new backoff while waiting
    }
}