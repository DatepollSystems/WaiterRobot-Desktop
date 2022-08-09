package org.datepollsystems.waiterrobot.mediator.printer.service

import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinter
import org.datepollsystems.waiterrobot.mediator.printer.PrinterWithIdNotFoundException
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintPdfMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintedPdfMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.RegisterPrinterMessage

object PrinterService {

    // Maps printerId from backend to a local printer
    private val idToPrinter = mutableMapOf<ID, LocalPrinter>()

    val printers: List<Pair<ID, LocalPrinter>> get() = idToPrinter.toList()

    init {
        App.socketManager // init the socketManager
        registerHandlers()
    }

    private suspend fun print(pdfId: String, printerId: ID, base64data: String) {
        idToPrinter[printerId]?.printPdf(pdfId, base64data) ?: throw PrinterWithIdNotFoundException(printerId)
        // test is the id of the test pdf, no response expected by backend
        if (pdfId != "test") App.socketManager.send(PrintedPdfMessage(pdfId = pdfId))
    }

    fun pair(backendId: ID, printer: LocalPrinter) {
        idToPrinter[backendId] = printer
        App.socketManager.addRegisterMessage(RegisterPrinterMessage(printerId = backendId))
    }

    private fun registerHandlers() {
        App.socketManager.handle<PrintPdfMessage> {
            print(it.body.id, it.body.printerId, it.body.file.data)
        }
    }
}