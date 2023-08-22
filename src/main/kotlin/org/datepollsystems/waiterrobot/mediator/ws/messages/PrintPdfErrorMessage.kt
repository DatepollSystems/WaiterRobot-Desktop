package org.datepollsystems.waiterrobot.mediator.ws.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MB_PRINT_PDF_ERROR") // Used as discriminator value
data class PrintPdfErrorMessage(
    override val httpStatus: Int,
    override val body: Body,
) : AbstractWsMessage<PrintPdfErrorMessage.Body>() {
    @Serializable
    data class Body(val id: String) : WsMessageBody

    constructor(httpStatus: Int = 200, pdfId: String) : this(httpStatus, Body(pdfId))
}
