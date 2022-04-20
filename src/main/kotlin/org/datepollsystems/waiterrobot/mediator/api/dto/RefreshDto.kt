package org.datepollsystems.waiterrobot.mediator.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RefreshDto(val sessionToken: String, val sessionInformation: String) : Sendable
