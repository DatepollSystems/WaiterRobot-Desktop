package org.datepollsystems.waiterrobot.mediator.ui.main

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.core.ViewModel
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.printer.service.PrinterService
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintTestPdfMessage

class MainScreenViewModel(
    navigator: Navigator,
    viewModelScope: CoroutineScope,
) : ViewModel<MainScreenState>(navigator, viewModelScope, MainScreenState()) {

    override suspend fun onCreate() {
        reduce { copy(printers = PrinterService.printers) }
        viewModelScope.launch {
            PrinterService.printQueueFlow.collect {
                reduce { copy(printTransactions = printTransactions.add(it)) }
            }
        }
    }

    fun printTestPdf(printerId: ID) = App.socketManager.send(PrintTestPdfMessage(printerId = printerId))
}