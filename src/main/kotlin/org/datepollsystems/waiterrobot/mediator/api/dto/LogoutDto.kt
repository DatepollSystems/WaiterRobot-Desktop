package org.datepollsystems.waiterrobot.mediator.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LogoutDto(val sessionToken: String) : Sendable