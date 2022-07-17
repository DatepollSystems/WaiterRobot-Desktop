package org.datepollsystems.waiterrobot.mediator.ws.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.mediator.core.ID

@Serializable
@SerialName("MB_PRINTED_PDF") // Used as discriminator value
data class PrintedPdfMessage(
    override val httpStatus: Int,
    override val body: Body,
) : AbstractWsMessage<PrintedPdfMessage.Body>() {
    @Serializable
    data class Body(val id: ID) : WsMessageBody

    constructor(httpStatus: Int = 200, pdfId: ID) : this(httpStatus, Body(pdfId))
}

