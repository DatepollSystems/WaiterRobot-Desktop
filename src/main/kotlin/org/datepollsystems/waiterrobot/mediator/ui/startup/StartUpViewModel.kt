package org.datepollsystems.waiterrobot.mediator.ui.startup

import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.AppVersion
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.core.AbstractViewModel
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.data.api.GitHubApi
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen

class StartUpViewModel(
    navigator: Navigator,
    private val gitHubApi: GitHubApi,
) : AbstractViewModel<StartUpState>(navigator, StartUpState()) {

    init {
        inVmScope {
            start()
        }
    }

    private suspend fun start() {
        val latestVersion = gitHubApi.getLatestVersion() ?: return goToStartScreen()

        if (AppVersion.current < latestVersion) {
            reduce {
                copy(screenState = ScreenState.Idle, showUpdateAvailable = true)
            }
        } else {
            goToStartScreen()
        }
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
        navigator.navigate(startScreen)
    }
}