package org.datepollsystems.waiterrobot.mediator.ws.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MB_HELLO") // Used as discriminator value
data class HelloMessage(
    override val httpStatus: Int,
    override val body: Body,
) : AbstractWsMessage<HelloMessage.Body>() {
    @Serializable
    data class Body(val text: String) : WsMessageBody

    constructor(text: String, httpStatus: Int = 200) : this(httpStatus, Body(text))
}