package org.datepollsystems.waiterrobot.mediator.ui.startup

import kotlinx.coroutines.delay
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.core.AbstractViewModel
import org.datepollsystems.waiterrobot.mediator.core.sentry.SentryHelper
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen
import kotlin.time.Duration.Companion.milliseconds

class StartUpViewModel(
    navigator: Navigator,
) : AbstractViewModel<StartUpState>(navigator, StartUpState()) {

    init {
        inVmScope {
            start()
        }
    }

    private suspend fun start() {
        delay(200.milliseconds) // Maybe do some animation?
        goToStartScreen()
    }

    fun goToStartScreen() {
        val loginPrefix = Settings.loginPrefix
        val startScreen = if (Settings.refreshToken == null) {
            Screen.LoginScreen
        } else if (loginPrefix == null) {
            App.logout()
            Screen.LoginScreen
        } else {
            App.config = Config.getFromLoginIdentifier(loginPrefix)
            Screen.ConfigurePrintersScreen
        }
        SentryHelper.updateEnvironment()
        navigator.navigate(startScreen)
    }
}
