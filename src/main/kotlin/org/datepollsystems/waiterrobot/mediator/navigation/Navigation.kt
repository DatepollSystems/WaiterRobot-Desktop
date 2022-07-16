package org.datepollsystems.waiterrobot.mediator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import org.datepollsystems.waiterrobot.mediator.api.*
import org.datepollsystems.waiterrobot.mediator.core.PrinterService
import org.datepollsystems.waiterrobot.mediator.ui.configurePrinters.ConfigurePrintersScreen
import org.datepollsystems.waiterrobot.mediator.ui.configurePrinters.ConfigurePrintersViewModel
import org.datepollsystems.waiterrobot.mediator.ui.login.LoginScreen
import org.datepollsystems.waiterrobot.mediator.ui.login.LoginViewModel
import org.datepollsystems.waiterrobot.mediator.ui.startup.StartUpScreen
import org.datepollsystems.waiterrobot.mediator.ui.startup.StartUpViewModel

@Composable
fun Navigation() {
    val navigator = remember { Navigator(Screen.StartUpScreen) }

    val screenState = navigator.screenState.collectAsState().value
    // TODO proper dependency injection (use koin?)
    when (screenState) {
        Screen.StartUpScreen -> WithCoroutineScope { StartUpScreen(StartUpViewModel(navigator, it)) }
        Screen.LoginScreen -> WithCoroutineScope {
            LoginScreen(
                LoginViewModel(
                    navigator,
                    it,
                    AuthApi(createClient(true))
                )
            )
        }
        //is Screen.MainScreen -> WithCoroutineScope { MainScreen(screenState, MainScreenViewModel(navigator, it)) }
        Screen.ConfigurePrintersScreen -> WithCoroutineScope {
            val client = createAuthenticatedClient(true)
            ConfigurePrintersScreen(
                ConfigurePrintersViewModel(
                    navigator,
                    it,
                    OrganisationApi(client),
                    EventApi(client),
                    PrinterApi(client),
                    PrinterService()
                )
            )
        }
    }.let {} // Force exhaustive
}

@Composable
// Helper to get a screen scoped coroutineContext for the viewModel
fun WithCoroutineScope(content: @Composable (CoroutineScope) -> Unit) {
    val scope = rememberCoroutineScope()
    content(scope)
}