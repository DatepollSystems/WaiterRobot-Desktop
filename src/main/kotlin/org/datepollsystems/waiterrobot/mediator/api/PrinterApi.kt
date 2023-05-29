package org.datepollsystems.waiterrobot.mediator.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.api.dto.GetPrinterDto
import org.datepollsystems.waiterrobot.mediator.core.ID

class PrinterApi(override val client: HttpClient) : AbstractApi("${App.config.apiBase}config/printer") {
    suspend fun getEventPrinters(eventId: ID) = get("/") {
        parameter("eventId", eventId)
    }.body<List<GetPrinterDto>>()
}