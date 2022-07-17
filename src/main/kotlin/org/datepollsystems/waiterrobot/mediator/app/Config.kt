package org.datepollsystems.waiterrobot.mediator.app

object Config {
    private const val DOMAIN = "lava.kellner.team"

    const val BASE_URL = "https://$DOMAIN/"
    const val API_BASE = "${BASE_URL}api/v1/"

    const val WS_URL = "wss://$DOMAIN/api/mediator"

    const val API_NETWORK_LOGGING = true
    const val WS_NETWORK_LOGGING = true
}