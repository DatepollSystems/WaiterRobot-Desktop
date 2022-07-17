package org.datepollsystems.waiterrobot.mediator.printer.service

import kotlinx.coroutines.CoroutineScope
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinter
import org.datepollsystems.waiterrobot.mediator.printer.PrinterWithIdNotFoundException
import org.datepollsystems.waiterrobot.mediator.ws.WsClient
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintPdfMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.PrintedPdfMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.RegisterPrinterMessage

class PrinterService(organisationId: ID, scope: CoroutineScope) {

    private val wsClient = WsClient(Config.WS_NETWORK_LOGGING, organisationId, scope)

    // Maps printerId from backend to a local printer
    private val idToPrinter = mutableMapOf<Long, LocalPrinter>()

    init {
        wsClient.connect()
        wsClient.onReady {
            idToPrinter.forEach {
                // Register all printers which were added before wsClient was ready
                registerPrinter(it.key)
            }
        }
        registerHandlers()
    }

    private suspend fun print(pdfId: ID, printerId: ID, base64data: String) {
        idToPrinter[printerId]?.printPdf(pdfId, base64data) ?: throw PrinterWithIdNotFoundException(printerId)
        wsClient.send(PrintedPdfMessage(pdfId = pdfId))
    }

    fun pair(backendId: ID, printer: LocalPrinter) {
        idToPrinter[backendId] = printer
        if (wsClient.isReady) {
            registerPrinter(backendId)
        }
    }

    private fun registerPrinter(id: ID) = wsClient.send(RegisterPrinterMessage(printerId = id))

    private fun registerHandlers() {
        wsClient.handle<PrintPdfMessage> {
            print(it.body.id, it.body.printerId, it.body.file.data)
        }
    }
}

// Example of the java printer API
/*
fun main() {
    var completed = false

    class PrintJobMonitor : PrintJobListener {
        override fun printDataTransferCompleted(pje: PrintJobEvent) {
            println("printDataTransferCompleted")
            // This means only that it was submitted to the Queue (Printer can also be offline -> Job is waiting till it comes online)
        }

        override fun printJobCanceled(pje: PrintJobEvent) {
            println("printJobCanceled")
        }

        override fun printJobCompleted(pje: PrintJobEvent) {
            println("printJobCompleted")
            completed = true
        }

        override fun printJobFailed(pje: PrintJobEvent) {
            println("printJobFailed")
        }

        override fun printJobNoMoreEvents(pje: PrintJobEvent) {
            // Printer can't verify if the job was successful or not
            // It's also not guaranteed that the job was even started (Printer can be offline)
            // Only tells the "User" that he will not be notified over any other events
            println("printJobNoMoreEvents")
            completed = true
        }

        override fun printJobRequiresAttention(pje: PrintJobEvent) {
            println("printJobRequiresAttention")
        }
    }

    val myPrinterService = PrintServiceLookup.lookupDefaultPrintService()
    val file = File("/Users/fabianschedler/tmp/test-mediator.pdf")
    val base64String = Base64.getEncoder().encodeToString(file.readBytes())
    val decoded = Base64.getDecoder().decode(base64String)
    val doc = SimpleDoc(decoded, DocFlavor.BYTE_ARRAY.PDF, HashDocAttributeSet(DocumentName("Bon", null)))

    val printJob = myPrinterService.createPrintJob()
    printJob.addPrintJobListener(PrintJobMonitor())

    // Some printers need the PageSize (e.g. for "PapierStau" detection)
    printJob.print(doc, HashPrintRequestAttributeSet(MediaSizeName.ISO_A4))
    printJob.addPrintJobAttributeListener({
        println(it)
    }, null)

    while (!completed) {
        Thread.sleep(1000)
    }

    println("end")
}
*/