package org.datepollsystems.waiterrobot.mediator.app

import org.datepollsystems.waiterrobot.mediator.utils.emptyToNull

sealed class Config(domain: String, secure: Boolean) {
    val apiBase: String
    val wsUrl: String
    val displayName: String = this::class.simpleName!!
    val basePath: String = System.getProperty("app.dir")?.emptyToNull() ?: System.getProperty("user.dir")
    abstract val loginPrefix: String

    val enableNetworkLogging = System.getenv("ENABLE_NETWORK_LOG") == "true"

    init {
        this.apiBase = "${if (secure) "https" else "http"}://$domain/api/"
        this.wsUrl = "${if (secure) "wss" else "ws"}://$domain/api/mediator"
    }

    object Local : Config(domain = "localhost:8080", secure = false) {
        override val loginPrefix = "local://"
    }

    object Lava : Config(domain = "lava.kellner.team", secure = true) {
        override val loginPrefix: String = "lava://"
    }

    object Prod : Config(domain = "my.kellner.team", secure = true) {
        override val loginPrefix: String = ""
    }

    companion object {
        fun getFromLoginIdentifier(username: String): Config = when {
            username.startsWith(Local.loginPrefix) -> Local
            username.startsWith(Lava.loginPrefix) -> Lava
            else -> Prod
        }
    }
}

fun String.removeLoginIdentifierEnvPrefix(): String = this.removePrefix("local://").removePrefix("lava://")
