package org.datepollsystems.waiterrobot.mediator.ui.configurePrinters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.data.api.dto.GetPrinterDto
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.*
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterInfo
import org.datepollsystems.waiterrobot.mediator.ui.common.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConfigurePrintersScreen(vm: ConfigurePrintersViewModel) {
    val state = vm.state.collectAsState().value

    LoadableScreen(state.screenState) {
        Column {
            SelectedEnvironmentInfo()
            DemoEventInfo(state.selectedEvent?.isDemo)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropDownInput(
                    modifier = Modifier
                        .padding(10.dp)
                        .weight(1f),
                    label = stringResource(Res.string.organization),
                    placeHolderText = stringResource(Res.string.select_organization_placeholder),
                    items = state.availableOrganisations ?: emptyList(),
                    onSelectionChange = { vm.changeOrganisation(it) },
                    selectedOptionText = state.selectedOrganisation?.name
                ) {
                    Text(it.name)
                }
                if (state.selectedOrganisation != null) {
                    DropDownInput(
                        modifier = Modifier
                            .padding(10.dp)
                            .weight(1f),
                        label = stringResource(Res.string.event),
                        placeHolderText = stringResource(Res.string.select_event_placeholder),
                        items = state.availableEvents ?: emptyList(),
                        onSelectionChange = { vm.changeEvent(it) },
                        selectedOptionText = state.selectedEvent?.name
                    ) {
                        Text(it.name)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                IconButton(onClick = { App.logout() }) {
                    Icon(Icons.Filled.Logout, contentDescription = null)
                }
            }
            Divider(modifier = Modifier.fillMaxWidth())

            if (state.unPairedBackendPrinters != null) {
                // TODO small infotext for user
                var selectedBePrinter: GetPrinterDto? by remember { mutableStateOf(null) }
                var selectedLocalPrinter: LocalPrinterInfo? by remember { mutableStateOf(null) }
                Row(
                    modifier = Modifier.padding(vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.padding(horizontal = 20.dp).weight(1f)) {
                        Text(stringResource(Res.string.pairing_backend_printers)) // TODO better naming for user
                    }
                    Box(modifier = Modifier.padding(horizontal = 20.dp).weight(1f)) {
                        Text(stringResource(Res.string.pairing_local_printers))
                    }
                    Box(modifier = Modifier.padding(horizontal = 20.dp).weight(1f)) {
                        Text(stringResource(Res.string.pairing_pairings))
                    }
                }

                Divider(modifier = Modifier.fillMaxWidth())

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        // Backend Printers
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(state.unPairedBackendPrinters, key = GetPrinterDto::id) {
                                PrinterListItem(
                                    title = it.name,
                                    subtitle = "", // TODO Location?
                                    selected = selectedBePrinter == it,
                                    onSelect = { selectedBePrinter = it }
                                )
                            }
                        }

                        // Local Printers
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(state.localPrinters ?: emptyList(), key = LocalPrinterInfo::localId) {
                                PrinterListItem(
                                    title = it.name,
                                    subtitle = "", // TODO Brand?
                                    selected = selectedLocalPrinter == it,
                                    onSelect = { selectedLocalPrinter = it }
                                )
                            }
                        }

                        // Pairings
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(state.pairings, key = { "${it.bePrinter.id}-${it.loPrinter.localId}" }) { pairing ->
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = { vm.removePairing(pairing) }
                                    ) {
                                        Icon(Icons.Filled.Delete, "contentDescription")
                                    }
                                    Text(pairing.bePrinter.name)
                                    Icon(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        contentDescription = "contentDescription",
                                        imageVector = Icons.Filled.ArrowForward
                                    )
                                    Text(pairing.loPrinter.name)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp).padding(top = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            enabled = selectedLocalPrinter != null && selectedBePrinter != null,
                            onClick = {
                                vm.pairPrinters(selectedLocalPrinter!!, selectedBePrinter!!)
                                selectedBePrinter = null
                                selectedLocalPrinter = null
                            },
                        ) {
                            Text(stringResource(Res.string.pairing_pair))
                        }

                        Button(
                            onClick = vm::saveAndContinue,
                            enabled = state.pairings.isNotEmpty()
                        ) {
                            Text(stringResource(Res.string.continue_))
                        }
                    }
                }
            } else if (state.selectedOrganisation == null) {
                CenteredText(stringResource(Res.string.pairing_select_organization))
            } else if (state.selectedEvent == null) {
                CenteredText(stringResource(Res.string.pairing_select_event))
            }
        }
    }
}
