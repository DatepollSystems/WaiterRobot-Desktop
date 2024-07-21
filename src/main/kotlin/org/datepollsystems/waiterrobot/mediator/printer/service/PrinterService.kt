package org.datepollsystems.waiterrobot.mediator.printer.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.data.api.dto.GetPrinterDto
import org.datepollsystems.waiterrobot.mediator.printer.AbstractLocalPrinter
import org.datepollsystems.waiterrobot.mediator.printer.NETWORK_ERROR_BASE64
import org.datepollsystems.waiterrobot.mediator.printer.PRINTER_CONNECTED_BASE64
import org.datepollsystems.waiterrobot.mediator.printer.PrinterWithIdNotFoundException
import org.datepollsystems.waiterrobot.mediator.ui.configurePrinters.ConfigurePrintersState
import org.datepollsystems.waiterrobot.mediator.ui.main.PrintTransaction
import org.datepollsystems.waiterrobot.mediator.utils.toHex
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintPdfMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintedPdfMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.RegisterPrinterMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.RegisterPrinterSuccessMessage
import java.time.LocalDateTime
import kotlin.random.Random

object PrinterService {

    // Maps printerId from backend to a local printer pairing
    private val backendIdToPairing = mutableMapOf<ID, PrinterPairing>()

    val printers: List<Pair<ID, ConfigurePrintersState.PrinterPairing>>
        get() = backendIdToPairing.map { (id, pairing) ->
            id to ConfigurePrintersState.PrinterPairing(pairing.bePrinter, pairing.loPrinter)
        }

    private val printQueue = MutableSharedFlow<PrintTransaction>(replay = 15) // TODO replay needed?
    val printQueueFlow: Flow<PrintTransaction> get() = printQueue

    init {
        App.socketManager // init the socketManager
        registerHandlers()
    }

    private suspend fun print(pdfId: String, printerId: ID, base64data: String) {
        // TODO handle printer is not registered on this mediator (info to BE)
        val printerPairing = backendIdToPairing[printerId] ?: throw PrinterWithIdNotFoundException(printerId)
        printerPairing.loPrinter.printPdf(pdfId, printerPairing.bePrinter.id, base64data)

        printQueue.emit(PrintTransaction(pdfId, LocalDateTime.now(), printerPairing.bePrinter.name))
        // test is the id of the test pdf, no response expected by backend
        if (pdfId != "test" && !pdfId.startsWith("LOCAL")) App.socketManager.send(PrintedPdfMessage(pdfId = pdfId))
    }

    fun pair(bePrinter: GetPrinterDto, loPrinter: AbstractLocalPrinter) {
        backendIdToPairing[bePrinter.id] = PrinterPairing(bePrinter, loPrinter)
        App.socketManager.addRegisterMessage(RegisterPrinterMessage(printerId = bePrinter.id))
    }

    private fun registerHandlers() {
        App.socketManager.handle<PrintPdfMessage> {
            print(it.body.id, it.body.printerId, it.body.file.data)
        }
        App.socketManager.handle<RegisterPrinterSuccessMessage> {
            if (it.body.printerId == null) return@handle
            print(
                getLocalPrintJobId("Printer_Connected"),
                it.body.printerId,
                PRINTER_CONNECTED_BASE64
            )
        }
    }

    fun printNetworkDisconnect() {
        backendIdToPairing.values.forEach {
            it.loPrinter.printPdf(
                getLocalPrintJobId("Network_Disconnect"),
                it.bePrinter.id,
                NETWORK_ERROR_BASE64
            )
        }
    }

    @Suppress("MagicNumber")
    private fun getLocalPrintJobId(name: String): String = "LOCAL_${name}_${Random.nextBytes(5).toHex()}"
}

class PrinterPairing(val bePrinter: GetPrinterDto, val loPrinter: AbstractLocalPrinter)
