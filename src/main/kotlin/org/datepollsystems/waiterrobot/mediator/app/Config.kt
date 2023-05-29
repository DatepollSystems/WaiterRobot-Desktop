package org.datepollsystems.waiterrobot.mediator.app

object Config {
    private val VARIANT = Variant.PROD

    private val DOMAIN = when (VARIANT) {
        Variant.LOCAL_DEV -> "localhost:8181"
        Variant.DEV -> "lava.kellner.team"
        Variant.PROD -> "my.kellner.team"
    }

    val BASE_URL = (if (VARIANT == Variant.LOCAL_DEV) "http" else "https") + "://$DOMAIN/"
    val API_BASE = "${BASE_URL}api/v1/"

    val WS_URL = (if (VARIANT == Variant.LOCAL_DEV) "ws" else "wss") + "://$DOMAIN/api/mediator"

    val API_NETWORK_LOGGING = VARIANT != Variant.PROD
    val WS_NETWORK_LOGGING = VARIANT != Variant.PROD

    val isCI = System.getenv("env") == "CI"
}

enum class Variant {
    LOCAL_DEV,
    DEV,
    PROD,
}