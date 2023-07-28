package org.datepollsystems.waiterrobot.mediator.data.api.dto

import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.mediator.core.api.RequestBodyDto

@Serializable
data class RefreshDto(val refreshToken: String, val sessionInformation: String) : RequestBodyDto