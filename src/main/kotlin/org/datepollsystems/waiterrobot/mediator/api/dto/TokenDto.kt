package org.datepollsystems.waiterrobot.mediator.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class TokenDto(
    val accessToken: String,
    val refreshToken: String?
)