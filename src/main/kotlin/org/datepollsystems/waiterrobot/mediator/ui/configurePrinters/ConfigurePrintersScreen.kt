package org.datepollsystems.waiterrobot.mediator.ui.configurePrinters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.datepollsystems.waiterrobot.mediator.api.dto.GetPrinterDto
import org.datepollsystems.waiterrobot.mediator.core.LocalPrinterInfo
import org.datepollsystems.waiterrobot.mediator.ui.common.DropDownInput
import org.datepollsystems.waiterrobot.mediator.ui.common.LoadableScreen

@Composable
fun ConfigurePrintersScreen(vm: ConfigurePrintersViewModel) {
    val state = vm.state.collectAsState().value

    LoadableScreen(state.screenState) {
        Column {
            Row {
                DropDownInput(
                    modifier = Modifier
                        .padding(10.dp)
                        .weight(1f),
                    label = "Organisation",
                    placeHolderText = "Select a Organisation...",
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
                        label = "Event",
                        placeHolderText = "Select a Event...",
                        items = state.availableEvents ?: emptyList(),
                        onSelectionChange = { vm.changeEvent(it) },
                        selectedOptionText = state.selectedEvent?.name
                    ) {
                        Text(it.name)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
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
                    Box(modifier = Modifier.padding(horizontal = 20.dp).weight(1.5f)) {
                        Text("Backend Printers") // TODO better naming for user
                    }
                    Box(modifier = Modifier.padding(horizontal = 20.dp).weight(1.5f)) {
                        Text("Local Printers")
                    }
                    Box(modifier = Modifier.padding(horizontal = 20.dp).weight(1f)) {
                        Text("Pairings")
                    }
                }
                Divider(modifier = Modifier.fillMaxWidth())
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        LazyColumn(modifier = Modifier.weight(1.5f)) {
                            items(state.unPairedBackendPrinters, key = GetPrinterDto::id) {
                                PrinterListItem(
                                    title = it.name,
                                    subtitle = "Location?",
                                    selected = selectedBePrinter == it,
                                    onSelect = { selectedBePrinter = it })
                            }
                        }

                        LazyColumn(modifier = Modifier.weight(1.5f)) {
                            items(state.unPairedLocalPrinters ?: emptyList(), key = LocalPrinterInfo::localId) {
                                PrinterListItem(
                                    title = it.name,
                                    subtitle = "Brand?",
                                    selected = selectedLocalPrinter == it,
                                    onSelect = { selectedLocalPrinter = it })
                            }
                        }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(state.pairings, key = { "${it.first.id}-${it.second.localId}" }) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(it.first.name)
                                    Icon(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        contentDescription = "contentDescription",
                                        imageVector = Icons.Filled.ArrowForward
                                    )
                                    Text(it.second.name)
                                    // TODO add remove button
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
                            Text("Pair selected printers")
                        }

                        Button(
                            onClick = vm::saveAndContinue,
                        ) {
                            Text("Continue")
                        }
                    }
                }
            }
        }
    }
}