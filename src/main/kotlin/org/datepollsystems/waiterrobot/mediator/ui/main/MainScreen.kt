package org.datepollsystems.waiterrobot.mediator.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(vm: MainScreenViewModel) {
    val state = vm.state.collectAsState().value

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            modifier = Modifier.padding(vertical = 20.dp),
            text = "Running"
        )
        Divider(thickness = 3.dp)

        Row {
            // Transaction log
            Column(modifier = Modifier.weight(2f)) {
                // TODO show a transaction log
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            //Printer list
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.printers, key = { it.second.localId }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(it.second.name)
                        IconButton(onClick = { vm.printTestPdf(it.first) }) {
                            Icon(Icons.Filled.Print, "contentDescription")
                        }
                    }
                }
            }
        }
    }
}
