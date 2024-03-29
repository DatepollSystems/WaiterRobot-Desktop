package org.datepollsystems.waiterrobot.mediator.ws.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MB_PRINTED_PDF") // Used as discriminator value
data class PrintedPdfMessage(
    override val httpStatus: Int,
    override val body: Body,
) : AbstractWsMessage<PrintedPdfMessage.Body>() {
    @Serializable
    data class Body(val id: String) : WsMessageBody

    constructor(httpStatus: Int = 200, pdfId: String) : this(httpStatus, Body(pdfId))
}
