package org.datepollsystems.waiterrobot.mediator.ws.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.mediator.core.ID

@Serializable
@SerialName("MB_PRINT_PDF_TEST") // Used as discriminator value
data class PrintTestPdfMessage(
    override val httpStatus: Int,
    override val body: Body,
) : AbstractWsMessage<PrintTestPdfMessage.Body>() {
    @Serializable
    data class Body(val printerId: ID) : WsMessageBody

    constructor(httpStatus: Int = 200, printerId: ID) : this(httpStatus, Body(printerId))
}
