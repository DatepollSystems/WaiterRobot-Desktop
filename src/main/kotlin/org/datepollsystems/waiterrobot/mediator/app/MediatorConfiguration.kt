package org.datepollsystems.waiterrobot.mediator.app

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.core.ID
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
        // TODO fix this location is not valid in prod
        private fun getFile(): File {
            val rootPath = System.getProperty("user.dir") // TODO maybe replace with app.dir (see https://conveyor.hydraulic.dev/12.1/configs/jvm/#appjvmsystem-properties)
            return File(rootPath, "WaiterRobot/config/mediatorConfig.json")
        }

        fun createFromStore(): MediatorConfiguration? = runCatching {
            Json.decodeFromString<MediatorConfiguration>(getFile().readText())
        }.onFailure {
            // TODO log
        }.getOrNull()
    }
}
