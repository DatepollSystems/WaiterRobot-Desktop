package org.datepollsystems.waiterrobot.mediator.ui.configurePrinters

import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.State
import org.datepollsystems.waiterrobot.mediator.data.api.dto.GetEventDto
import org.datepollsystems.waiterrobot.mediator.data.api.dto.GetOrganisationDto
import org.datepollsystems.waiterrobot.mediator.data.api.dto.GetPrinterDto
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterInfo

data class ConfigurePrintersState(
    override val screenState: ScreenState = ScreenState.Loading,
    val availableOrganisations: List<GetOrganisationDto>? = null,
    val selectedOrganisation: GetOrganisationDto? = null,
    val availableEvents: List<GetEventDto>? = null,
    val selectedEvent: GetEventDto? = null,
    val unPairedBackendPrinters: List<GetPrinterDto>? = null,
    val localPrinters: List<LocalPrinterInfo>? = null,
    val pairings: List<PrinterPairing> = emptyList()
) : State<ConfigurePrintersState> {
    data class PrinterPairing(val bePrinter: GetPrinterDto, val loPrinter: LocalPrinterInfo)

    override fun withScreenState(screenState: ScreenState): ConfigurePrintersState = copy(screenState = screenState)
}