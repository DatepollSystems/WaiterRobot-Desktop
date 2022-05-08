package org.datepollsystems.waiterrobot.mediator.ws.messages

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("messageObjectId")
sealed class AbstractWsMessage<out T : WsMessageBody> {
    abstract val httpStatus: Int
    abstract val body: T
}

interface WsMessageBody