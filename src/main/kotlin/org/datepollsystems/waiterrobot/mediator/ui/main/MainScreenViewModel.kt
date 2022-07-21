package org.datepollsystems.waiterrobot.mediator.ui.main

import kotlinx.coroutines.CoroutineScope
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.core.ViewModel
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.printer.service.PrinterService
import org.datepollsystems.waiterrobot.mediator.ws.WsClient
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintTestPdfMessage

class MainScreenViewModel(
    navigator: Navigator,
    viewModelScope: CoroutineScope,
) : ViewModel<MainScreenState>(navigator, viewModelScope, MainScreenState()) {

    override suspend fun onCreate() {
        reduce { copy(printers = PrinterService.printers) }
    }

    fun printTestPdf(printerId: ID) = WsClient.send(PrintTestPdfMessage(printerId = printerId))

}
