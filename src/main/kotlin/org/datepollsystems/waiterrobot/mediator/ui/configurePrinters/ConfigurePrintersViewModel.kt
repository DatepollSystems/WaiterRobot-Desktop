package org.datepollsystems.waiterrobot.mediator.ui.configurePrinters

import kotlinx.coroutines.CoroutineScope
import org.datepollsystems.waiterrobot.mediator.api.EventApi
import org.datepollsystems.waiterrobot.mediator.api.OrganisationApi
import org.datepollsystems.waiterrobot.mediator.api.PrinterApi
import org.datepollsystems.waiterrobot.mediator.api.dto.GetPrinterDto
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.ViewModel
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator

class ConfigurePrintersViewModel(
    navigator: Navigator,
    viewModelScope: CoroutineScope,
    private val organisationApi: OrganisationApi,
    private val eventApi: EventApi,
    private val printerApi: PrinterApi,
) : ViewModel<ConfigurePrintersState>(navigator, viewModelScope, ConfigurePrintersState()) {

    init {
        // TODO load saved config
        loadUserOrganisations()
    }

    private fun loadUserOrganisations() = inVmScope {
        val orgs = organisationApi.getUserOrganisations()
        reduce { copy(screenState = ScreenState.Idle, availableOrganisations = orgs) }
    }

    private fun loadOrganisationEvents() = inVmScope {
        // TODO handle org not set
        val events = eventApi.getOrganisationEvents(state.value.selectedOrganisationId!!)
        reduce { copy(screenState = ScreenState.Idle, availableEvents = events) }
    }

    private fun loadEventPrinters() = inVmScope {
        // TODO handle event not set
        val bePrinters = printerApi.getEventPrinters(state.value.selectedEventId!!)
        reduce { copy(screenState = ScreenState.Idle, backendPrinters = bePrinters) }
    }

    fun changeOrganisation(organisationId: ID) = inVmScope {
        reduce {
            copy(
                selectedOrganisationId = organisationId,
                availableEvents = null,
                backendPrinters = null,
                message = "Please select a event"
            )
        }
        loadOrganisationEvents()
    }

    fun changeEvent(eventId: ID) = inVmScope {
        reduce { copy(selectedEventId = eventId, backendPrinters = null) }
        loadEventPrinters()
    }

    fun pairPrinters(localPrinterId: ID, backendPrinterId: ID) = inVmScope {
        val localPrinter = state.value.localPrinters?.find { it.localId == localPrinterId }
        val backendPrinter = state.value.backendPrinters?.find { it.id == backendPrinterId }

        if (localPrinter == null || backendPrinter == null) return@inVmScope

        reduce {
            copy(
                localPrinters = localPrinters?.minus(localPrinter),
                backendPrinters = backendPrinters?.minus(backendPrinter),
                pairings = pairings.plus(backendPrinter to localPrinter)
            )
        }
    }

    fun removePairing(pairing: Pair<GetPrinterDto, Printer>) = inVmScope {
        reduce {
            copy(
                localPrinters = localPrinters?.plus(pairing.second),
                backendPrinters = backendPrinters?.plus(pairing.first),
                pairings = pairings.minus(pairing)
            )
        }
    }

    fun savePairing() {
        // TODO persist pairing and current org/event selection locally
        // TODO send paired backendPrinters to BE (ws) and move to next screen
    }
}