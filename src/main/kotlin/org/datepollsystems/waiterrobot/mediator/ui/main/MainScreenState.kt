package org.datepollsystems.waiterrobot.mediator.ui.main

import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.State
import org.datepollsystems.waiterrobot.mediator.ui.configurePrinters.ConfigurePrintersState
import org.datepollsystems.waiterrobot.mediator.utils.toHex
import java.time.LocalDateTime
import kotlin.random.Random

data class MainScreenState(
    override val screenState: ScreenState = ScreenState.Idle,
    val printTransactions: CircularQueue<PrintTransaction> = CircularQueue(MAX_TRANSACTIONS),
    val printers: List<Pair<ID, ConfigurePrintersState.PrinterPairing>> = emptyList(),
) : State<MainScreenState> {
    override fun withScreenState(screenState: ScreenState): MainScreenState = copy(screenState = screenState)
}

class PrintTransaction(val jobName: String, val time: LocalDateTime, val printer: String) {
    @Suppress("MagicNumber")
    val id: String = jobName + if (jobName == "test") Random.nextBytes(10).toHex() else ""
}

class CircularQueue<T : Any> private constructor(private val stack: ArrayDeque<T>, private val maxItems: Int) {

    constructor(maxItems: Int) : this(ArrayDeque(), maxItems)

    fun add(item: T): CircularQueue<T> {
        stack.addFirst(item)
        if (stack.count() > maxItems) {
            stack.removeLast()
        }
        return CircularQueue(stack, maxItems)
    }

    fun isEmpty() = stack.isEmpty()

    val items get() = stack.toList()
}

private const val MAX_TRANSACTIONS = 100
