package org.datepollsystems.waiterrobot.mediator.app

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.core.ID
import java.io.File

@Serializable
data class MediatorConfiguration(
    val selectedOrganisationId: ID?,
    val selectedEventId: ID?,
    val localToBackendPrinterId: Map<String, ID>
) {
    fun save() {
        val jsonConfig = Json.encodeToString(this)
        val file = getFile()
        file.parentFile.mkdirs()
        file.createNewFile()
        file.writeText(jsonConfig)
    }

    companion object {
        private fun getFile(): File {
            val rootPath = MediatorConfiguration::class.java.getResource("/")!!.path
            return File(rootPath, "resources/cache/mediatorConfig.json")
        }

        fun createFromStore(): MediatorConfiguration? = try {
            Json.decodeFromString(getFile().readText())
        } catch (e: Exception) {
            // TODO log (some errors are expected)
            null
        }
    }
}
