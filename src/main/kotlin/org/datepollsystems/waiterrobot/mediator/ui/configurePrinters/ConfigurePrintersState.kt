package org.datepollsystems.waiterrobot.mediator.ui.configurePrinters

import org.datepollsystems.waiterrobot.mediator.api.dto.GetEventDto
import org.datepollsystems.waiterrobot.mediator.api.dto.GetOrganisationDto
import org.datepollsystems.waiterrobot.mediator.api.dto.GetPrinterDto
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.State

data class ConfigurePrintersState(
    override val screenState: ScreenState = ScreenState.Loading,
    val message: String? = null,
    val availableOrganisations: List<GetOrganisationDto>? = null,
    val selectedOrganisationId: ID? = null,
    val availableEvents: List<GetEventDto>? = null,
    val selectedEventId: ID? = null,
    val backendPrinters: List<GetPrinterDto>? = null,
    val localPrinters: List<Printer>? = null,
    val pairings: List<Pair<GetPrinterDto, Printer>> = emptyList()
) : State

data class Printer(val localId: ID)