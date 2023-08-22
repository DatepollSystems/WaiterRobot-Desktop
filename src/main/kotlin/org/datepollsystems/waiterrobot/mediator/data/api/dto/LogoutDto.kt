package org.datepollsystems.waiterrobot.mediator.data.api.dto

import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.mediator.core.api.RequestBodyDto

@Serializable
data class LogoutDto(val refreshToken: String) : RequestBodyDto
