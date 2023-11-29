package org.datepollsystems.waiterrobot.mediator.ui

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.datepollsystems.waiterrobot.mediator.navigation.Navigation
import org.datepollsystems.waiterrobot.mediator.ui.theme.WaiterRobotTheme
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream

fun startUI(onClose: () -> Unit = {}) {
    application {
        val appIcon = remember {
            System.getProperty("app.dir") // Only available when packaged
                ?.let { Paths.get(it, "icon-512.png") }
                ?.takeIf { it.exists() }
                ?.inputStream()
                ?.buffered()
                ?.use { BitmapPainter(loadImageBitmap(it)) }
        }

        Window(
            title = "kellner.team",
            icon = appIcon,
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
