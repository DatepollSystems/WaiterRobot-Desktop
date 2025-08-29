package org.datepollsystems.waiterrobot.mediator.core

import co.touchlab.kermit.io.RollingFileLogWriter
import co.touchlab.kermit.io.RollingFileLogWriterConfig
import kotlinx.io.files.Path
import org.datepollsystems.waiterrobot.mediator.App
import java.io.File

fun rollingFileLogWriter(): RollingFileLogWriter {
    val config = RollingFileLogWriterConfig(
        logFileName = "mediator",
        logFilePath = Path(File(App.config.basePath, "logs").also { it.mkdirs() }.absolutePath),
    )

    return RollingFileLogWriter(config)
}
