package org.datepollsystems.waiterrobot.mediator.app

sealed class Config(domain: String, secure: Boolean) {
    val apiBase: String
    val wsUrl: String

    val enableNetworkLogging = System.getenv("ENABLE_NETWORK_LOG") == "true"

    init {
        this.apiBase = "${if (secure) "https" else "http"}://$domain/api/v1/"
        this.wsUrl = "${if (secure) "wss" else "ws"}://$domain/api/mediator"
    }

    private object Local : Config(domain = "localhost:8080", secure = false)
    private object Lava : Config(domain = "lava.kellner.team", secure = true)
    private object Prod : Config(domain = "my.kellner.team", secure = true)

    companion object {
        fun getFromLoginIdentifier(username: String): Config = when {
            username.startsWith("local://") -> Local
            username.startsWith("lava://") -> Lava
            else -> Prod
        }
    }
}

fun String.removeLoginIdentifierEnvPrefix(): String = this.removePrefix("local://").removePrefix("lava://")