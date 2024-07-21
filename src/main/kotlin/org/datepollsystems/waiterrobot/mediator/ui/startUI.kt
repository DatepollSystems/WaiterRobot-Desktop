package org.datepollsystems.waiterrobot.mediator.ui

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.datepollsystems.waiterrobot.mediator.core.ShortcutManager
import org.datepollsystems.waiterrobot.mediator.navigation.Navigation
import org.datepollsystems.waiterrobot.mediator.ui.theme.WaiterRobotTheme
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream

fun startUI(onClose: () -> Unit = {}) {
    application {
        Window(
            title = "kellner.team",
            icon = appIcon,
            state = rememberWindowState(
                size = DpSize(1300.dp, 800.dp)
            ),
            onCloseRequest = {
                onClose()
                this.exitApplication()
            },
            onKeyEvent = ShortcutManager::handleKeyEvent
        ) {
            WaiterRobotTheme {
                Navigation()
            }
        }
    }
}

private val appIcon: Painter? by lazy {
    // app.dir is set when packaged to point at our collected inputs.
    val appDir = System.getProperty("app.dir")?.let { Path.of(it) }
    // On Windows we should use the .ico file.
    // On Linux, there's no native compound image format and Compose can't render SVG icons, so we pick the 128x128
    // icon and let the frameworks/desktop environment rescale.
    // On macOS we don't need to do anything.
    var iconPath = appDir?.resolve("app.ico")?.takeIf { it.exists() }
    iconPath = iconPath ?: appDir?.resolve("icon-square-128.png")?.takeIf { it.exists() }
    if (iconPath?.exists() == true) {
        BitmapPainter(iconPath.inputStream().buffered().use { loadImageBitmap(it) })
    } else {
        null
    }
}
