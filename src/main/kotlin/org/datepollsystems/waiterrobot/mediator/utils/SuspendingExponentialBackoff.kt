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
 * @param maxBackoffs maximum number of possible backoffs null for unlimited. When reached an error is thrown.
 * @param resetAfter reset the backoff after a duration (starts then again with [initialDelay]) null for never. Must be > 0 or null
 * @param maxBackoffTime maximum duration of backoff. null for no max time. Must be >= initialDelay or null
 * @param name for logging
 *
 * @author Fabian Schedler
 */
class SuspendingExponentialBackoff(
    initialDelay: Duration,
    private val maxBackoffs: Int? = null,
    private val resetAfter: Duration? = null,
    private val maxBackoffTime: Duration? = null,
    private val name: String,
) {
    private val backoffStage: AtomicInteger = AtomicInteger(0)
    private var lastBackoff: Instant = Instant.MIN
    private val resetScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val initialDelay = initialDelay.toMillis().toDouble()
    private var maxReached = false

    // TODO should we switch to a linear backoff from a specific stage (see https://images.app.goo.gl/UFGUPFcESLMRDbMx8)
    private val currentDelay: Duration
        get() {
            if (maxBackoffTime != null && maxReached) return maxBackoffTime
            var delay = Duration.ofMillis((initialDelay * 2f.pow(backoffStage.get())).toLong())
            if (maxBackoffTime != null && delay > maxBackoffTime) {
                maxReached = true
                delay = maxBackoffTime
            }
            return delay
        }

    init {
        require(initialDelay > Duration.ZERO) { "initialDelay must be > 0" }
        require(resetAfter == null || resetAfter > Duration.ZERO) { "resetAfter must be > 0 or null" }
        require(maxBackoffTime == null || maxBackoffTime >= initialDelay) { "maxBackoffTime must be >= initialDelay or null" }
        require(maxBackoffs == null || maxBackoffs > 0)
    }

    /**
     * Call when the next execution should be "backoffed".
     * Rethrows the [errorOnLimit] or a [TimeoutException] if [maxBackoffs] is set reached
     */
    fun backoff(errorOnLimit: Throwable? = null) {
        resetScope.coroutineContext.cancelChildren()

        val currentStage = backoffStage.incrementAndGet()
        if (maxBackoffs != null && currentStage > maxBackoffs) {
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
     */
    suspend fun acquire() {
        val nextExecution = lastBackoff + currentDelay
        val now = Instant.now()
        if (now.isAfter(nextExecution)) return

        val delayDuration = Duration.between(now, nextExecution)
        println("Backoff task \"$name\" for ${delayDuration.seconds}s")
        delay(delayDuration.toMillis())
    }
}