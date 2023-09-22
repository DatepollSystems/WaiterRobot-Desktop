package org.datepollsystems.waiterrobot.mediator.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.AppVersion
import org.datepollsystems.waiterrobot.mediator.core.api.AbstractApi
import org.datepollsystems.waiterrobot.mediator.data.api.dto.LoginDto
import org.datepollsystems.waiterrobot.mediator.data.api.dto.LogoutDto
import org.datepollsystems.waiterrobot.mediator.data.api.dto.RefreshDto
import org.datepollsystems.waiterrobot.mediator.data.api.dto.TokenDto

// TODO add better session information (version, os, ...)
class AuthApi(client: HttpClient) : AbstractApi({ "${App.config.apiBase}auth/" }, client) {

    suspend fun login(email: String, password: String, stayLoggedIn: Boolean = true) =
        post(
            "/login",
            LoginDto(
                email = email,
                password = password,
                sessionInformation = "Mediator ${AppVersion.current}",
                stayLoggedIn = stayLoggedIn
            )
        ).body<TokenDto>()

    suspend fun refresh(refreshToken: String) =
        post(
            "/refresh",
            RefreshDto(refreshToken, sessionInformation = "Mediator ${AppVersion.current}")
        ).body<TokenDto>()

    suspend fun logout(refreshToken: String): Boolean =
        post("/logout", LogoutDto(refreshToken)).status == HttpStatusCode.OK
}
