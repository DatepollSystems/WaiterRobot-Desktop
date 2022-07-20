package org.datepollsystems.waiterrobot.mediator.printer.service

import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinter
import org.datepollsystems.waiterrobot.mediator.printer.PrinterWithIdNotFoundException
import org.datepollsystems.waiterrobot.mediator.ws.WsClient
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintPdfMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintedPdfMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.RegisterPrinterMessage

object PrinterService {

    // Maps printerId from backend to a local printer
    private val idToPrinter = mutableMapOf<Long, LocalPrinter>()

    init {
        WsClient.connect()
        WsClient.onReady {
            idToPrinter.forEach {
                // Register all printers which were added before wsClient was ready
                registerPrinter(it.key)
            }
        }
        registerHandlers()
    }

    private suspend fun print(pdfId: ID, printerId: ID, base64data: String) {
        idToPrinter[printerId]?.printPdf(pdfId, base64data) ?: throw PrinterWithIdNotFoundException(printerId)
        WsClient.send(PrintedPdfMessage(pdfId = pdfId))
    }

    fun pair(backendId: ID, printer: LocalPrinter) {
        idToPrinter[backendId] = printer
        if (WsClient.isReady) {
            registerPrinter(backendId)
        }
    }

    private fun registerPrinter(id: ID) = WsClient.send(RegisterPrinterMessage(printerId = id))

    private fun registerHandlers() {
        WsClient.handle<PrintPdfMessage> {
            print(it.body.id, it.body.printerId, it.body.file.data)
        }
    }
}