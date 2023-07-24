package org.datepollsystems.waiterrobot.mediator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import org.datepollsystems.waiterrobot.mediator.api.*
import org.datepollsystems.waiterrobot.mediator.ui.configurePrinters.ConfigurePrintersScreen
import org.datepollsystems.waiterrobot.mediator.ui.configurePrinters.ConfigurePrintersViewModel
import org.datepollsystems.waiterrobot.mediator.ui.forceUpdate.ForceUpdateScreen
import org.datepollsystems.waiterrobot.mediator.ui.login.LoginScreen
import org.datepollsystems.waiterrobot.mediator.ui.login.LoginViewModel
import org.datepollsystems.waiterrobot.mediator.ui.main.MainScreen
import org.datepollsystems.waiterrobot.mediator.ui.main.MainScreenViewModel
import org.datepollsystems.waiterrobot.mediator.ui.startup.StartUpScreen
import org.datepollsystems.waiterrobot.mediator.ui.startup.StartUpViewModel

@Composable
fun Navigation() {
    val navigator = remember { Navigator(Screen.StartUpScreen) }

    val screenState = navigator.screenState.collectAsState().value
    // TODO proper dependency injection (use koin?)
    when (screenState) {
        Screen.StartUpScreen -> {
            val viewModel = getViewModel(
                key = "startUp-screen",
                factory = viewModelFactory {
                    val client = createClient()
                    StartUpViewModel(
                        GitHubApi(client),
                        navigator
                    )
                }
            )
            StartUpScreen(viewModel)
        }

        Screen.LoginScreen -> {
            val viewModel = getViewModel(
                key = "login-screen",
                factory = viewModelFactory {
                    LoginViewModel(navigator)
                }
            )
            LoginScreen(viewModel)
        }

        Screen.MainScreen -> {
            val viewModel = getViewModel(
                key = "main-screen",
                factory = viewModelFactory {
                    MainScreenViewModel(navigator)
                }
            )
            MainScreen(viewModel)
        }

        Screen.ConfigurePrintersScreen -> {
            val viewModel = getViewModel(
                key = "configure-printers-screen",
                factory = viewModelFactory {
                    val client = createAuthenticatedClient()
                    ConfigurePrintersViewModel(
                        navigator,
                        OrganisationApi(client),
                        EventApi(client),
                        PrinterApi(client),
                    )
                }
            )
            ConfigurePrintersScreen(viewModel)
        }

        Screen.AppVersionTooOld -> ForceUpdateScreen()
    }.let {} // Force exhaustive
}