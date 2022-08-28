package org.datepollsystems.waiterrobot.mediator.api

import io.ktor.client.*
import org.datepollsystems.waiterrobot.mediator.api.dto.GetPrinterDto
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.core.ID

class PrinterApi(override val client: HttpClient) : AbstractApi("${Config.API_BASE}config/printer") {
    suspend fun getEventPrinters(eventId: ID) = get<List<GetPrinterDto>>("/") {
        url.encodedParameters.append("eventId", eventId.toString())
    }
}