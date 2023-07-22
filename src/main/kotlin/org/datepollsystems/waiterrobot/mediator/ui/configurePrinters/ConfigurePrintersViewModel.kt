package org.datepollsystems.waiterrobot.mediator.ui.configurePrinters

import org.datepollsystems.waiterrobot.mediator.api.EventApi
import org.datepollsystems.waiterrobot.mediator.api.OrganisationApi
import org.datepollsystems.waiterrobot.mediator.api.PrinterApi
import org.datepollsystems.waiterrobot.mediator.api.dto.GetEventDto
import org.datepollsystems.waiterrobot.mediator.api.dto.GetOrganisationDto
import org.datepollsystems.waiterrobot.mediator.api.dto.GetPrinterDto
import org.datepollsystems.waiterrobot.mediator.app.MediatorConfiguration
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.core.AbstractViewModel
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterInfo
import org.datepollsystems.waiterrobot.mediator.printer.service.PrinterDiscoverService
import org.datepollsystems.waiterrobot.mediator.printer.service.PrinterService

class ConfigurePrintersViewModel(
    navigator: Navigator,
    private val organisationApi: OrganisationApi,
    private val eventApi: EventApi,
    private val printerApi: PrinterApi,
) : AbstractViewModel<ConfigurePrintersState>(navigator, ConfigurePrintersState()) {

    init {
        val initConfig = MediatorConfiguration.createFromStore()
        inVmScope {
            val organisations = loadUserOrganisations()
            if (initConfig != null) {
                val selectedOrg = organisations.find { it.id == initConfig.selectedOrganisationId }

                val events = if (selectedOrg != null) loadOrganisationEvents(selectedOrg.id) else null
                val selectedEvent = events?.find { it.id == initConfig.selectedEventId }

                val bePrinters = if (selectedEvent != null) loadEventPrinters(selectedEvent.id) else null
                val backendMap = bePrinters?.associateBy(GetPrinterDto::id) ?: emptyMap()
                val localIds = PrinterDiscoverService.localPrinters.map(LocalPrinterInfo::localId)

                val pairings = if ( // Pairing can only be applied when all printers are present
                    backendMap.isNotEmpty() &&
                    initConfig.printerPairings.all { it.backendPrinterId in backendMap.keys && it.localPrinterId in localIds }
                ) {
                    initConfig.printerPairings.map { pairing ->
                        ConfigurePrintersState.PrinterPairing(
                            bePrinter = backendMap.getValue(pairing.backendPrinterId),
                            loPrinter = PrinterDiscoverService.localPrinterMap.getValue(pairing.localPrinterId)
                        )
                    }
                } else {
                    emptyList()
                }

                val pairedBackendPrinterIds =
                    initConfig.printerPairings.map(MediatorConfiguration.PrinterPairing::backendPrinterId).toSet()
                reduce {
                    copy(
                        screenState = ScreenState.Idle,
                        availableOrganisations = organisations,
                        availableEvents = events,
                        selectedOrganisation = selectedOrg,
                        selectedEvent = selectedEvent,
                        pairings = pairings,
                        localPrinters = PrinterDiscoverService.localPrinters.toList(),
                        unPairedBackendPrinters = bePrinters?.filter { it.id !in pairedBackendPrinterIds }
                    )
                }
            } else {
                reduce {
                    copy(
                        screenState = ScreenState.Idle,
                        availableOrganisations = organisations,
                        localPrinters = PrinterDiscoverService.localPrinters.toList(),
                    )
                }
            }
        }
    }

    private suspend fun loadUserOrganisations(): List<GetOrganisationDto> {
        val organisations = organisationApi.getUserOrganisations()
        reduce { copy(availableOrganisations = organisations) }
        return organisations
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

            val config = MediatorConfiguration(
                selectedOrganisationId = state.selectedOrganisation.id,
                selectedEventId = state.selectedEvent.id,
                printerPairings = state.pairings.map { paring ->
                    MediatorConfiguration.PrinterPairing(paring.loPrinter.localId, paring.bePrinter.id)
                }
            )
            config.save()

            Settings.organisationId = config.selectedOrganisationId

            state.pairings.forEach { pairing ->
                val localPrinter = PrinterDiscoverService.localPrinterMap[pairing.loPrinter.localId] ?: return@forEach
                PrinterService.pair(pairing.bePrinter, localPrinter)
            }

            navigator.navigate(Screen.MainScreen)
        }
    }

    fun changeOrganisation(organisation: GetOrganisationDto) = inVmScope {
        reduce {
            copy(
                selectedOrganisation = organisation,
                selectedEvent = null,
                availableEvents = null,
                unPairedBackendPrinters = null,
                localPrinters = PrinterDiscoverService.localPrinters.toList(),
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
                localPrinters = PrinterDiscoverService.localPrinters.toList(),
                pairings = emptyList()
            )
        }
        loadEventPrinters()
    }

    fun pairPrinters(localPrinter: LocalPrinterInfo, backendPrinter: GetPrinterDto) = inVmScope {
        reduce {
            copy(
                unPairedBackendPrinters = unPairedBackendPrinters?.minus(backendPrinter),
                pairings = pairings.plus(ConfigurePrintersState.PrinterPairing(backendPrinter, localPrinter))
            )
        }
    }

    fun removePairing(pairing: ConfigurePrintersState.PrinterPairing) = inVmScope {
        reduce {
            copy(
                unPairedBackendPrinters = unPairedBackendPrinters?.plus(pairing.bePrinter),
                pairings = pairings.minus(pairing)
            )
        }
    }
}