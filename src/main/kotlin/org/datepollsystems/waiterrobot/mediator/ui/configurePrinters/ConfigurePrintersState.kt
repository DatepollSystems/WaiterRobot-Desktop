package org.datepollsystems.waiterrobot.mediator.ui.configurePrinters

import org.datepollsystems.waiterrobot.mediator.api.dto.GetEventDto
import org.datepollsystems.waiterrobot.mediator.api.dto.GetOrganisationDto
import org.datepollsystems.waiterrobot.mediator.api.dto.GetPrinterDto
import org.datepollsystems.waiterrobot.mediator.core.LocalPrinterInfo
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.State

data class ConfigurePrintersState(
    override val screenState: ScreenState = ScreenState.Loading,
    val availableOrganisations: List<GetOrganisationDto>? = null,
    val selectedOrganisation: GetOrganisationDto? = null,
    val availableEvents: List<GetEventDto>? = null,
    val selectedEvent: GetEventDto? = null,
    val unPairedBackendPrinters: List<GetPrinterDto>? = null,
    val unPairedLocalPrinters: List<LocalPrinterInfo>? = null,
    val pairings: List<Pair<GetPrinterDto, LocalPrinterInfo>> = emptyList()
) : State