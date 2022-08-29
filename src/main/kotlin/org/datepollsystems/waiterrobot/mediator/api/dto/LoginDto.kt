package org.datepollsystems.waiterrobot.mediator.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginDto(
    val email: String,
    val password: String,
    val sessionInformation: String,
    val stayLoggedIn: Boolean
) : Sendable