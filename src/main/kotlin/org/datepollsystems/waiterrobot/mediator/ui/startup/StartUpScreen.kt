package org.datepollsystems.waiterrobot.mediator.ui.startup

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalUriHandler
import org.datepollsystems.waiterrobot.mediator.ui.common.LoadableScreen

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StartUpScreen(vm: StartUpViewModel) {

    val state = vm.state.collectAsState().value

    LoadableScreen(state.screenState) {
        if (state.showUpdateAvailable) {
            AlertDialog(
                onDismissRequest = { vm.goToStartScreen() },
                confirmButton = {
                    val uriHandler = LocalUriHandler.current
                    Button(onClick = {
                        uriHandler.openUri("https://github.com/DatepollSystems/waiterrobot-desktop/releases/latest")
                    }) {
                        Text("Jetzt aktualisieren")
                    }
                },
                dismissButton = {
                    Button(onClick = vm::goToStartScreen) {
                        Text("Nicht jetzt")
                    }
                },
                title = {
                    Text("Neue Version")
                },
                text = {
                    Text("Eine neue Version steht zur Verfügung. Update jetzt um alle neuen Funktionalitäten nutzen zu können.")
                }
            )
        }
    }
}