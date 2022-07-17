package org.datepollsystems.waiterrobot.mediator.ws.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("BM_PRINT_PDF") // Used as discriminator value
data class PrintPdfMessage(
    override val httpStatus: Int,
    override val body: Body,
) : AbstractWsMessage<PrintPdfMessage.Body>() {
    @Serializable
    data class Body(val id: Long, val printerId: Long, val file: File) : WsMessageBody {
        @Serializable
        data class File(val mime: String, val data: String)
    }
}