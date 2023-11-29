package org.datepollsystems.waiterrobot.mediator.app

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.utils.emptyToNull
import java.io.File

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
            val rootPath = System.getProperty("app.dir")?.emptyToNull() ?: System.getProperty("user.dir")
            return File(rootPath, "WaiterRobot/config/mediatorConfig.json")
        }

        fun createFromStore(): MediatorConfiguration? = runCatching {
            Json.decodeFromString<MediatorConfiguration>(getFile().readText())
        }.onFailure {
            // TODO log
        }.getOrNull()
    }
}
