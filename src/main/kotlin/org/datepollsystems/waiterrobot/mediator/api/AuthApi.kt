package org.datepollsystems.waiterrobot.mediator.api

import io.ktor.client.*
import io.ktor.http.*
import org.datepollsystems.waiterrobot.mediator.api.dto.LoginDto
import org.datepollsystems.waiterrobot.mediator.api.dto.LogoutDto
import org.datepollsystems.waiterrobot.mediator.api.dto.RefreshDto
import org.datepollsystems.waiterrobot.mediator.api.dto.TokenDto
import org.datepollsystems.waiterrobot.mediator.app.Config

// TODO add better session information (version, os, ...)
class AuthApi(override val client: HttpClient) : AbstractApi("${Config.API_BASE}auth/") {

    suspend fun login(email: String, password: String, stayLoggedIn: Boolean = true) =
        post<TokenDto>(
            "/signIn",
            LoginDto(email = email, password = password, sessionInformation = "Mediator", stayLoggedIn = stayLoggedIn)
        )

    suspend fun refresh(sessionToken: String) =
        post<TokenDto>("/refresh", RefreshDto(sessionToken = sessionToken, sessionInformation = "Mediator"))

    suspend fun logout(sessionToken: String): Boolean =
        post("/logout", LogoutDto(sessionToken = sessionToken)).status == HttpStatusCode.OK
}