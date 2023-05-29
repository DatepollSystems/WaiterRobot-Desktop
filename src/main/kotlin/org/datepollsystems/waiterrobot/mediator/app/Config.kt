package org.datepollsystems.waiterrobot.mediator.app

sealed class Config(baseUrl: String) {
    val baseUrl: String
    val apiBase: String
    val wsUrl: String

    val enableNetworkLogging = System.getenv("ENABLE_NETWORK_LOG") == "true"

    init {
        this.baseUrl = baseUrl.removeSuffix("/") + "/"
        this.wsUrl = "${this.baseUrl}api/mediator"
        this.apiBase = "${this.baseUrl}api/v1/"
    }

    private object Local : Config(baseUrl = "http://localhost:8080")
    private object Lava : Config(baseUrl = "https://lava.kellner.team")
    private object Prod : Config(baseUrl = "https://my.kellner.team")

    companion object {
        fun getFromLoginIdentifier(username: String): Config = when {
            username.startsWith("local://") -> Local
            username.startsWith("lava://") -> Lava
            else -> Prod
        }
    }
}

fun String.removeLoginIdentifierEnvPrefix(): String = this.removePrefix("local://").removePrefix("lava://")