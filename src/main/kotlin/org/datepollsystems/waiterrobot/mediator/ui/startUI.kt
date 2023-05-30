package org.datepollsystems.waiterrobot.mediator.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.datepollsystems.waiterrobot.mediator.navigation.Navigation
import org.datepollsystems.waiterrobot.mediator.ui.theme.WaiterRobotTheme

fun startUI(onClose: () -> Unit = {}) {
    application {
        Window(
            title = "WaiterRobot Desktop",
            icon = null, // TODO
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