package org.datepollsystems.waiterrobot.mediator.ui.main

import kotlinx.coroutines.CoroutineScope
import org.datepollsystems.waiterrobot.mediator.app.MediatorConfiguration
import org.datepollsystems.waiterrobot.mediator.core.ViewModel
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.printer.service.PrinterDiscoverService
import org.datepollsystems.waiterrobot.mediator.printer.service.PrinterService

class MainScreenViewModel(
    navigator: Navigator,
    viewModelScope: CoroutineScope,
    config: MediatorConfiguration,
) : ViewModel<MainScreenState>(navigator, viewModelScope, MainScreenState()) {

    private val printerService: PrinterService

    init {
        printerService = PrinterService(config.selectedOrganisationId, viewModelScope)
        config.localToBackendPrinterId.forEach { (localId, backendId) ->
            val localPrinter = PrinterDiscoverService.localPrinterMap[localId] ?: return@forEach
            printerService.pair(backendId, localPrinter)
        }
    }
}
