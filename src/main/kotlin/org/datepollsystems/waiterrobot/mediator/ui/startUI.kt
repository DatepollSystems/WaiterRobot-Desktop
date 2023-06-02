package org.datepollsystems.waiterrobot.mediator.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.datepollsystems.waiterrobot.mediator.navigation.Navigation
import org.datepollsystems.waiterrobot.mediator.ui.theme.WaiterRobotTheme

fun startUI(onClose: () -> Unit = {}) {
    application {
        Window(
            title = "WaiterRobot Desktop",
            icon = null, // TODO
            state = rememberWindowState(
                size = DpSize(1300.dp, 800.dp)
            ),
            onCloseRequest = {
                onClose()
                this.exitApplication()
            }
        ) {
            WaiterRobotTheme {
                Navigation()
            }
        }
    }
}