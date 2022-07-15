package org.datepollsystems.waiterrobot.mediator.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenDto(
    @SerialName("token") val accessToken: String,
    @SerialName("sessionToken") val refreshToken: String?
)