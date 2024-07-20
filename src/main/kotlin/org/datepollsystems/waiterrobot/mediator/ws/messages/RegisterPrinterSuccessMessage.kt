package org.datepollsystems.waiterrobot.mediator.ws.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("BM_REGISTERED_PRINTER_SUCCESSFUL") // Used as discriminator value
data class RegisterPrinterSuccessMessage(
    override val httpStatus: Int,
    override val body: Body,
) : AbstractWsMessage<RegisterPrinterSuccessMessage.Body>() {
    @Serializable
    data class Body(val printerId: Long? = null) : WsMessageBody
}
