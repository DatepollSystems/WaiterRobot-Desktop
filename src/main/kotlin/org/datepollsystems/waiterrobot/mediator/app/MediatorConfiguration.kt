package org.datepollsystems.waiterrobot.mediator.app

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.ID
import java.io.File
import kotlin.io.path.Path

@Serializable
data class MediatorConfiguration(
    val selectedOrganisationId: ID,
    val selectedEventId: ID,
    val printerPairings: List<PrinterPairing>
) {
    @Serializable
    data class PrinterPairing(val localPrinterId: String, val backendPrinterId: ID)

    fun save() {
        runCatching {
            val jsonConfig = Json.encodeToString(this)
            val file = getFile()
            file.parentFile.mkdirs()
            file.createNewFile()
            file.writeText(jsonConfig)
        }.onFailure {
            // TODO log
        }
    }

    companion object {
        private fun getFile(): File {
            return Path(App.config.basePath, "config", "mediatorConfig.json").toFile()
        }

        fun createFromStore(): MediatorConfiguration? = runCatching {
            Json.decodeFromString<MediatorConfiguration>(getFile().also { println("Getting config from ${it.absolutePath}") }.readText())
        }.onFailure {
            // TODO log
        }.getOrNull()
    }
}
