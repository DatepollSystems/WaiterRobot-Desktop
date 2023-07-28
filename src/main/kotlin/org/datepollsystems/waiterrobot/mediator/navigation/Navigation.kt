package org.datepollsystems.waiterrobot.mediator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import org.datepollsystems.waiterrobot.mediator.core.di.getViewModel
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
    when (screenState) {
        Screen.StartUpScreen -> {
            val viewModel = getViewModel { StartUpViewModel(navigator, get()) }
            StartUpScreen(viewModel)
        }

        Screen.LoginScreen -> {
            val viewModel = getViewModel { LoginViewModel(navigator) }
            LoginScreen(viewModel)
        }

        Screen.MainScreen -> {
            val viewModel = getViewModel { MainScreenViewModel(navigator) }
            MainScreen(viewModel)
        }

        Screen.ConfigurePrintersScreen -> {
            val viewModel = getViewModel {
                ConfigurePrintersViewModel(navigator, get(), get(), get())
            }
            ConfigurePrintersScreen(viewModel)
        }

        Screen.AppVersionTooOld -> ForceUpdateScreen()
    }.let {} // Force exhaustive
}