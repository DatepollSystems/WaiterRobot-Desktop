package org.datepollsystems.waiterrobot.mediator.app

import org.datepollsystems.waiterrobot.mediator.core.ID
import java.util.prefs.Preferences
import kotlin.properties.Delegates

object Settings {
    private const val SETTINGS_NODE_NAME = "org.datepollsystems.waiterrobot.mediator.settings"
    private val preferences = Preferences.userRoot().node(SETTINGS_NODE_NAME)

    var accessToken: String? by preferences.nullableString()
    var refreshToken: String? by preferences.nullableString()
    var loginPrefix: String? by preferences.nullableString()

    var organisationId by Delegates.notNull<ID>()
}
