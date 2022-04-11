package org.datepollsystems.waiterrobot.mediator.ws.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PrintPdfMessage") // Used as discriminator value
data class PrintPdfMessage(
    override val httpStatus: Int,
    override val body: PrintPdfBody,
) : AbstractWsMessage<PrintPdfBody>()

@Serializable
data class PrintPdfBody(val id: Long, val printerId: Long, val file: File) : WsMessageBody {
    @Serializable
    data class File(val mime: String, val data: String)
}