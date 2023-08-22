package org.datepollsystems.waiterrobot.mediator.ws.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("BM_HELLO") // Used as discriminator value
data class HelloMessageResponse(
    override val httpStatus: Int,
    override val body: Body,
) : AbstractWsMessage<HelloMessageResponse.Body>() {
    @Serializable
    data class Body(val text: String) : WsMessageBody
}
