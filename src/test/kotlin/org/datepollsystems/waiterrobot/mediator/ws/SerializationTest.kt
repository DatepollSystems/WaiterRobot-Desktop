package org.datepollsystems.waiterrobot.mediator.ws

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.ws.messages.*
import org.junit.Test
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf
import kotlin.test.assertEquals

internal class SerializationTest {

    @Test
    fun `PrintPdfMessage De-Serialization as AbstractOutgoingWsMessage with WsMessageBody`() {
        val data: AbstractWsMessage<WsMessageBody> = PrintPdfMessage(
            httpStatus = 200,
            body = PrintPdfMessage.Body("100", 100L, PrintPdfMessage.Body.File("mime", "data/base64"))
        )

        val string = Json.encodeToString(data)
        val decoded = Json.decodeFromString<AbstractWsMessage<WsMessageBody>>(string)

        assertEquals(data, decoded)
        assertEquals(decoded::class.createType(), typeOf<PrintPdfMessage>())
    }

    @Test
    fun `PrintPdfMessage De-Serialization as AbstractOutgoingWsMessage with PrintPdfBody`() {
        val data: AbstractWsMessage<PrintPdfMessage.Body> = PrintPdfMessage(
            httpStatus = 200,
            body = PrintPdfMessage.Body(
                "JKLjsl-fdjsafas-fjdsakfa",
                100L,
                PrintPdfMessage.Body.File("mime", "data/base64")
            )
        )

        val string = Json.encodeToString(data)
        val decoded = Json.decodeFromString<AbstractWsMessage<WsMessageBody>>(string)

        assertEquals(data, decoded)
        assertEquals(decoded::class.createType(), typeOf<PrintPdfMessage>())
    }

    @Test
    fun `Hello De-Serialization as AbstractOutgoingWsMessage with WsMessageBody`() {
        val data: AbstractWsMessage<WsMessageBody> = HelloMessage(
            httpStatus = 200,
            body = HelloMessage.Body("test")
        )

        val string = Json.encodeToString(data)
        val decoded = Json.decodeFromString<AbstractWsMessage<WsMessageBody>>(string)

        assertEquals(data, decoded)
        assertEquals(decoded::class.createType(), typeOf<HelloMessage>())
    }

    @Test
    fun `Hello De-Serialization as AbstractOutgoingWsMessage with HelloBody`() {
        val data: AbstractWsMessage<HelloMessage.Body> = HelloMessage(
            httpStatus = 200,
            body = HelloMessage.Body("Test")
        )

        val string = Json.encodeToString(data)
        val decoded = Json.decodeFromString<AbstractWsMessage<WsMessageBody>>(string)

        assertEquals(data, decoded)
        assertEquals(decoded::class.createType(), typeOf<HelloMessage>())
    }

    @Test
    fun `PrintedPdfMessage De-Serialization as AbstractIncomingWsMessage with WsMessageBody`() {
        val data: AbstractWsMessage<WsMessageBody> = PrintedPdfMessage(
            httpStatus = 200,
            body = PrintedPdfMessage.Body("jfdlksa-fdsaf-fdsaf-fdsa")
        )

        val string = Json.encodeToString(data)
        val decoded = Json.decodeFromString<AbstractWsMessage<WsMessageBody>>(string)

        assertEquals(data, decoded)
        assertEquals(decoded::class.createType(), typeOf<PrintedPdfMessage>())
    }

    @Test
    fun `PrintedPdfMessage De-Serialization as AbstractIncomingWsMessage with PrintedPdfBody`() {
        val data: AbstractWsMessage<PrintedPdfMessage.Body> = PrintedPdfMessage(
            httpStatus = 200,
            body = PrintedPdfMessage.Body("fdsa98erq-efadsa94fea-f4fads")
        )

        val string = Json.encodeToString(data)
        val decoded = Json.decodeFromString<AbstractWsMessage<WsMessageBody>>(string)

        assertEquals(data, decoded)
        assertEquals(decoded::class.createType(), typeOf<PrintedPdfMessage>())
    }

    @Test
    fun `Test from String`() {
        val text = "Hello Test Message"
        val string = """{"messageObjectId":"BM_HELLO","httpStatus":200,"body":{"text":"$text"}}"""
        val decoded = Json.decodeFromString<AbstractWsMessage<WsMessageBody>>(string)

        assertEquals(200, decoded.httpStatus)
        assertEquals(decoded::class.createType(), typeOf<HelloMessageResponse>())
        assertEquals(text, (decoded as HelloMessageResponse).body.text)
    }
}