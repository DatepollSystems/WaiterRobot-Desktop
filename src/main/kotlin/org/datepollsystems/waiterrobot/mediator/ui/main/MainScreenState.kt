package org.datepollsystems.waiterrobot.mediator.ui.main

import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.State
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterInfo

data class MainScreenState(
    override val screenState: ScreenState = ScreenState.Idle,
    val printTransactions: List<Int> = emptyList(), // TODO
    val printers: List<Pair<ID, LocalPrinterInfo>> = emptyList()
) : State
