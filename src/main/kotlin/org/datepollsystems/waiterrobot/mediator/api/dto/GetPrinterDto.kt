package org.datepollsystems.waiterrobot.mediator.api.dto

import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.mediator.core.ID

@Serializable
data class GetPrinterDto(
    val id: ID,
    val name: String,
    val printerName: String,
    val eventId: ID,
)