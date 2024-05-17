package org.datepollsystems.waiterrobot.mediator.core.sentry

import io.sentry.Sentry
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.Settings

object SentryHelper {
    fun updateEnvironment() {
        @Suppress("UnstableApiUsage")
        Sentry.getCurrentHub().options.environment =
            if (Settings.refreshToken == null || Settings.loginPrefix == null) {
                "unknown"
            } else {
                App.config.displayName
            }
    }
}
