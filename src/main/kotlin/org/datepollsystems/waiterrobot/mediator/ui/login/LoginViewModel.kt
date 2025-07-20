package org.datepollsystems.waiterrobot.mediator.ui.login

import io.ktor.http.*
import io.sentry.Sentry
import io.sentry.protocol.User
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.app.removeLoginIdentifierEnvPrefix
import org.datepollsystems.waiterrobot.mediator.core.AbstractViewModel
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.sentry.SentryHelper
import org.datepollsystems.waiterrobot.mediator.data.api.ApiException
import org.datepollsystems.waiterrobot.mediator.data.api.AuthApi
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.Res
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.login_wrong_credentials
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen
import org.datepollsystems.waiterrobot.mediator.printer.service.PrinterDiscoverService

class LoginViewModel(
    navigator: Navigator,
    private val authApi: AuthApi
) : AbstractViewModel<LoginState>(navigator, LoginState()) {

    fun doLogin(email: String, password: String) = inVmScope {
        reduce { copy(screenState = ScreenState.Loading, loginErrorMessage = null) }

        App.config = Config.getFromLoginIdentifier(email)
        Settings.loginPrefix = App.config.loginPrefix
        SentryHelper.updateEnvironment()
        PrinterDiscoverService.refreshPrinters()

        try {
            val tokens = authApi.login(email.removeLoginIdentifierEnvPrefix(), password)
            Sentry.setUser(
                User().apply {
                    setEmail(email.removeLoginIdentifierEnvPrefix())
                }
            )
            Settings.accessToken = tokens.accessToken
            Settings.refreshToken = tokens.refreshToken!!

            navigator.navigate(Screen.ConfigurePrintersScreen)
        } catch (e: ApiException) {
            // TODO this should be unified (see WR-307)
            @Suppress("InstanceOfCheckForException")
            if (e is ApiException.Unauthorized ||
                e is ApiException.CredentialsIncorrect ||
                e.httpCode == HttpStatusCode.Unauthorized.value
            ) {
                logger.d(e) { "Login failed" }
                reduce {
                    copy(
                        screenState = ScreenState.Idle,
                        loginErrorMessage = Res.string.login_wrong_credentials
                    )
                }
            } else {
                throw e
            }
        }
    }
}
