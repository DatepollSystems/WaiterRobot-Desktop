package org.datepollsystems.waiterrobot.mediator.data.api.dto

import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.mediator.core.ID

@Serializable
data class GetEventDto(
    val id: ID,
    val name: String,
    val street: String,
    val streetNumber: String,
    val postalCode: String,
    val city: String,
    val organisationId: ID,
    val isDemo: Boolean
)
