package org.datepollsystems.waiterrobot.mediator.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*
import org.datepollsystems.waiterrobot.mediator.api.dto.LoginDto
import org.datepollsystems.waiterrobot.mediator.api.dto.LogoutDto
import org.datepollsystems.waiterrobot.mediator.api.dto.RefreshDto
import org.datepollsystems.waiterrobot.mediator.api.dto.TokenDto
import org.datepollsystems.waiterrobot.mediator.app.Config

// TODO add better session information (version, os, ...)
class AuthApi(override val client: HttpClient) : AbstractApi("${Config.API_BASE}auth/") {

    suspend fun login(email: String, password: String, stayLoggedIn: Boolean = true) =
        post(
            "/login",
            LoginDto(email = email, password = password, sessionInformation = "Mediator", stayLoggedIn = stayLoggedIn)
        ).body<TokenDto>()

    suspend fun refresh(refreshToken: String) =
        post("/refresh", RefreshDto(refreshToken, sessionInformation = "Mediator")).body<TokenDto>()

    suspend fun logout(refreshToken: String): Boolean =
        post("/logout", LogoutDto(refreshToken)).status == HttpStatusCode.OK
}