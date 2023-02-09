package org.datepollsystems.waiterrobot.mediator.api.dto

import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.mediator.api.RequestBodyDto

@Serializable
data class LogoutDto(val refreshToken: String) : RequestBodyDto