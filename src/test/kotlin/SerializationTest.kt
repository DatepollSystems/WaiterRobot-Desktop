import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.ws.messages.AbstractWsMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintPdfBody
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintPdfMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.WsMessageBody
import org.junit.Test
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf
import kotlin.test.assertEquals

class SerializationTest {

    @Test
    fun `PrintPdfMessage De-Serialization`() {
        val data: AbstractWsMessage<WsMessageBody> = PrintPdfMessage(
            httpStatus = 200,
            body = PrintPdfBody(100L, 100L, PrintPdfBody.File("mime", "data/base64"))
        )

        val string = Json.encodeToString(data)
        val decoded = Json.decodeFromString<AbstractWsMessage<WsMessageBody>>(string)

        assertEquals(data, decoded)
        assertEquals(decoded::class.createType(), typeOf<PrintPdfMessage>())
    }
}