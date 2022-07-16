package org.datepollsystems.waiterrobot.mediator.ui.configurePrinters

import kotlinx.coroutines.CoroutineScope
import org.datepollsystems.waiterrobot.mediator.api.EventApi
import org.datepollsystems.waiterrobot.mediator.api.OrganisationApi
import org.datepollsystems.waiterrobot.mediator.api.PrinterApi
import org.datepollsystems.waiterrobot.mediator.api.dto.GetEventDto
import org.datepollsystems.waiterrobot.mediator.api.dto.GetOrganisationDto
import org.datepollsystems.waiterrobot.mediator.api.dto.GetPrinterDto
import org.datepollsystems.waiterrobot.mediator.app.MediatorConfiguration
import org.datepollsystems.waiterrobot.mediator.core.*
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator

class ConfigurePrintersViewModel(
    navigator: Navigator,
    viewModelScope: CoroutineScope,
    private val organisationApi: OrganisationApi,
    private val eventApi: EventApi,
    private val printerApi: PrinterApi,
    private val printerService: PrinterService,
) : ViewModel<ConfigurePrintersState>(navigator, viewModelScope, ConfigurePrintersState()) {

    init {
        val initConfig = MediatorConfiguration.createFromStore()
        inVmScope {
            val orgs = loadUserOrganisations()
            if (initConfig != null) {
                val selectedOrg = orgs.find { it.id == initConfig.selectedOrganisationId }

                val events = if (selectedOrg != null) loadOrganisationEvents(selectedOrg.id) else null
                val selectedEvent = events?.find { it.id == initConfig.selectedEventId }

                val bePrinters = if (selectedEvent != null) loadEventPrinters(selectedEvent.id) else null
                val backendIds = bePrinters?.map(GetPrinterDto::id) ?: emptyList()
                val localIds = printerService.localPrinters.map(LocalPrinterInfo::localId)

                val pairings = if ( // Pairing can only be applied when all printers are present
                    bePrinters != null &&
                    initConfig.localToBackendPrinterId.values.all { it in backendIds } &&
                    initConfig.localToBackendPrinterId.keys.all { it in localIds }
                ) {
                    initConfig.localToBackendPrinterId.map { (localId, backendId) ->
                        bePrinters.first { it.id == backendId } to printerService.localPrinters.first { it.localId == localId }
                    }
                } else {
                    emptyList()
                }

                reduce {
                    copy(
                        screenState = ScreenState.Idle,
                        availableOrganisations = orgs,
                        availableEvents = events,
                        selectedOrganisation = selectedOrg,
                        selectedEvent = selectedEvent,
                        pairings = pairings,
                        unPairedLocalPrinters = printerService.localPrinters.filter { it.localId !in initConfig.localToBackendPrinterId.keys },
                        unPairedBackendPrinters = bePrinters?.filter { it.id !in initConfig.localToBackendPrinterId.values }
                    )
                }
            } else {
                reduce {
                    copy(
                        screenState = ScreenState.Idle,
                        availableOrganisations = orgs,
                        unPairedLocalPrinters = printerService.localPrinters.toList(),
                    )
                }
            }
        }
    }

    private suspend fun loadUserOrganisations(): List<GetOrganisationDto> {
        val orgs = organisationApi.getUserOrganisations()
        reduce { copy(availableOrganisations = orgs) }
        return orgs
    }

    private suspend fun loadOrganisationEvents(organisationId: ID = state.value.selectedOrganisation!!.id): List<GetEventDto> {
        // TODO handle org not set
        val events = eventApi.getOrganisationEvents(organisationId)
        reduce { copy(screenState = ScreenState.Idle, availableEvents = events) }
        return events
    }

    private suspend fun loadEventPrinters(eventId: ID = state.value.selectedEvent!!.id): List<GetPrinterDto> {
        // TODO handle event not set
        val bePrinters = printerApi.getEventPrinters(eventId)
        reduce { copy(screenState = ScreenState.Idle, unPairedBackendPrinters = bePrinters) }
        return bePrinters
    }

    fun saveAndContinue() {
        val state = state.value
        if (state.selectedOrganisation == null || state.selectedEvent == null) return

        inVmScope {
            reduce { copy(screenState = ScreenState.Loading) }

            MediatorConfiguration(
                selectedOrganisationId = state.selectedOrganisation.id,
                selectedEventId = state.selectedEvent.id,
                localToBackendPrinterId = state.pairings.associate { (bePrinter, loPrinter) -> loPrinter.localId to bePrinter.id }
            ).save()

            state.pairings.forEach { (bePrinter, loPrinter) ->
                printerService.pair(loPrinter.localId, bePrinter.id)
            }

            // TODO go to next screen and pass printerService.
            //  Next screen then initiates ws connection and registers all the printers in the backend (or should this be done by the printerService?)
        }
    }

    fun changeOrganisation(organisation: GetOrganisationDto) = inVmScope {
        reduce {
            copy(
                selectedOrganisation = organisation,
                selectedEvent = null,
                availableEvents = null,
                unPairedBackendPrinters = null,
                unPairedLocalPrinters = printerService.localPrinters.toList(),
                pairings = emptyList()
            )
        }
        loadOrganisationEvents()
    }

    fun changeEvent(event: GetEventDto) = inVmScope {
        reduce {
            copy(
                selectedEvent = event,
                unPairedBackendPrinters = null,
                unPairedLocalPrinters = printerService.localPrinters.toList(),
                pairings = emptyList()
            )
        }
        loadEventPrinters()
    }

    fun pairPrinters(localPrinter: LocalPrinterInfo, backendPrinter: GetPrinterDto) = inVmScope {
        reduce {
            copy(
                unPairedLocalPrinters = unPairedLocalPrinters?.minus(localPrinter),
                unPairedBackendPrinters = unPairedBackendPrinters?.minus(backendPrinter),
                pairings = pairings.plus(backendPrinter to localPrinter)
            )
        }
    }

    fun removePairing(pairing: Pair<GetPrinterDto, LocalPrinterInfo>) = inVmScope {
        reduce {
            copy(
                unPairedLocalPrinters = unPairedLocalPrinters?.plus(pairing.second),
                unPairedBackendPrinters = unPairedBackendPrinters?.plus(pairing.first),
                pairings = pairings.minus(pairing)
            )
        }
    }
}