package org.datepollsystems.waiterrobot.mediator.app

import org.datepollsystems.waiterrobot.mediator.utils.emptyToNull

object Settings {
    private const val ACCESS_TOKEN = "accessToken"
    private const val REFRESH_TOKEN = "refreshToken"

    var accessToken: String?
        get() = System.getProperty(ACCESS_TOKEN, null).emptyToNull()
        set(value) {
            value.emptyToNull()?.also { System.setProperty(ACCESS_TOKEN, it) }
                ?: System.clearProperty(ACCESS_TOKEN)
        }

    var refreshToken: String?
        get() = System.getProperty(REFRESH_TOKEN, null).emptyToNull()
        set(value) {
            value.emptyToNull()?.also { System.setProperty(REFRESH_TOKEN, it) }
                ?: System.clearProperty(REFRESH_TOKEN)
        }
}