package org.datepollsystems.waiterrobot.mediator.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class TokenDto(
    val accessToken: String,
    val refreshToken: String?
)
