package org.datepollsystems.waiterrobot.mediator.ui.configurePrinters

import androidx.compose.ui.input.key.*
import io.sentry.Sentry
import org.datepollsystems.waiterrobot.mediator.app.MediatorConfiguration
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.core.*
import org.datepollsystems.waiterrobot.mediator.core.sentry.SentryTagKeys
import org.datepollsystems.waiterrobot.mediator.data.api.EventApi
import org.datepollsystems.waiterrobot.mediator.data.api.OrganisationApi
import org.datepollsystems.waiterrobot.mediator.data.api.PrinterApi
import org.datepollsystems.waiterrobot.mediator.data.api.dto.GetEventDto
import org.datepollsystems.waiterrobot.mediator.data.api.dto.GetOrganisationDto
import org.datepollsystems.waiterrobot.mediator.data.api.dto.GetPrinterDto
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
) : AbstractViewModel<ConfigurePrintersState>(navigator, ConfigurePrintersState()), ShortcutHandler {

    init {
        ShortcutManager.registerHandler(this)
        val initConfig = MediatorConfiguration.createFromStore()
        inVmScope {
            loadUserOrganisations(
                selectId = initConfig?.selectedOrganisationId,
                selectEventId = initConfig?.selectedEventId
            )
            if (initConfig != null) {
                val selectedEvent = state.value.selectedEvent
                val bePrinters = if (selectedEvent != null) loadEventPrinters(selectedEvent.id) else null
                val backendMap = bePrinters?.associateBy(GetPrinterDto::id) ?: emptyMap()
                val localIds = PrinterDiscoverService.localPrinters.map(LocalPrinterInfo::localId)

                val pairings = if ( // Pairing can only be applied when all printers are present
                    backendMap.isNotEmpty() &&
                    initConfig.printerPairings.all {
                        it.backendPrinterId in backendMap.keys && it.localPrinterId in localIds
                    }
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

                val pairedBackendPrinterIds = pairings.map { it.bePrinter.id }.toSet()
                reduce {
                    copy(
                        screenState = ScreenState.Idle,
                        pairings = pairings,
                        localPrinters = PrinterDiscoverService.localPrinters.toList(),
                        unPairedBackendPrinters = bePrinters?.filter { it.id !in pairedBackendPrinterIds }
                    )
                }
            } else {
                reduce {
                    copy(
                        screenState = ScreenState.Idle,
                        localPrinters = PrinterDiscoverService.localPrinters.toList(),
                    )
                }
            }
        }
    }

    override fun onCleared() {
        ShortcutManager.removeHandler(this)
        super.onCleared()
    }

    private suspend fun loadUserOrganisations(selectId: ID? = null, selectEventId: ID? = null) {
        val organisations = organisationApi.getUserOrganisations()
        reduce { copy(availableOrganisations = organisations) }
        val organisationToSelect = organisations.singleOrNull()
            ?: if (selectId != null) organisations.find { it.id == selectId } else null
        if (organisationToSelect != null) changeOrganisation(organisationToSelect, selectEventId).join()
    }

    private suspend fun loadOrganisationEvents(
        organisationId: ID = state.value.selectedOrganisation!!.id,
        selectId: ID? = null
    ) {
        // TODO handle organization not set
        val events = eventApi.getOrganisationEvents(organisationId)
        reduce { copy(screenState = ScreenState.Idle, availableEvents = events) }
        val eventToSelect = events.singleOrNull()
            ?: if (selectId != null) events.find { it.id == selectId } else null
        if (eventToSelect != null) changeEvent(eventToSelect).join()
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
        Sentry.setTag(SentryTagKeys.organizationId, state.selectedOrganisation.id.toString())
        Sentry.setTag(SentryTagKeys.eventId, state.selectedEvent.id.toString())

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

    fun changeOrganisation(organisation: GetOrganisationDto, selectEventId: ID? = null) = inVmScope {
        Sentry.setTag(SentryTagKeys.organizationId, organisation.id.toString())
        Sentry.removeTag(SentryTagKeys.eventId)
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
        loadOrganisationEvents(selectId = selectEventId)
    }

    fun changeEvent(event: GetEventDto) = inVmScope {
        Sentry.setTag(SentryTagKeys.eventId, event.id.toString())
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

    override fun onKeyEvent(event: KeyEvent): Boolean {
        return when {
            (event.isCtrlPressed || event.isMetaPressed) && event.key == Key.D -> {
                PrinterDiscoverService.addVirtualPrinter()
                reduce {
                    copy(localPrinters = PrinterDiscoverService.localPrinters.toList())
                }
                true
            }

            else -> false
        }
    }
}
