package org.datepollsystems.waiterrobot.mediator.ui.startup

import org.datepollsystems.waiterrobot.mediator.api.GitHubApi
import org.datepollsystems.waiterrobot.mediator.app.AppVersion
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.core.AbstractViewModel
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen

class StartUpViewModel(
    private val gitHubApi: GitHubApi,
    navigator: Navigator,
) : AbstractViewModel<StartUpState>(navigator, StartUpState()) {

    init {
        inVmScope {
            start()
        }
    }

    private suspend fun start() {
        val latestVersion = gitHubApi.getLatestVersion() ?: return
        if (AppVersion.current < latestVersion) {
            reduce {
                copy(screenState = ScreenState.Idle, showUpdateAvailable = true)
            }
        } else {
            goToStartScreen()
        }
    }

    fun goToStartScreen() {
        val startScreen = if (Settings.refreshToken == null) {
            Screen.LoginScreen
        } else {
            Screen.ConfigurePrintersScreen
        }
        navigator.navigate(startScreen)
    }
}