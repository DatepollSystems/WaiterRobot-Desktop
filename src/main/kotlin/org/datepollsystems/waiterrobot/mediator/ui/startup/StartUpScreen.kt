package org.datepollsystems.waiterrobot.mediator.ui.startup

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalUriHandler
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.*
import org.datepollsystems.waiterrobot.mediator.ui.common.LoadableScreen
import org.jetbrains.compose.resources.stringResource

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
                        uriHandler.openUri("https://get.kellner.team/download.html")
                    }) {
                        Text(stringResource(Res.string.start_update_now))
                    }
                },
                dismissButton = {
                    Button(onClick = vm::goToStartScreen) {
                        Text(stringResource(Res.string.start_update_not_now))
                    }
                },
                title = {
                    Text(stringResource(Res.string.start_new_version_title))
                },
                text = {
                    Text(stringResource(Res.string.start_new_version_description))
                }
            )
        }
    }
}
