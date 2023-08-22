package org.datepollsystems.waiterrobot.mediator.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.datepollsystems.waiterrobot.mediator.core.ID
import java.time.LocalDate

@Serializable
data class GetEventDto(
    val id: ID,
    val name: String,
    @SerialName("date") private val dateString: String? = null,
    val street: String,
    val streetNumber: String,
    val postalCode: String,
    val city: String,
    val organisationId: ID
) {
    @Transient
    val date: LocalDate? = dateString?.let { LocalDate.parse(it) }
}
