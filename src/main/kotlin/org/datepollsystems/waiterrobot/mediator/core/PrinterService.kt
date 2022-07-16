package org.datepollsystems.waiterrobot.mediator.core

import javax.print.DocFlavor
import javax.print.PrintServiceLookup

class PrinterService {
    // Get all printers that support printing PDF's in ByteArray format
    private val localPrinterMap: Map<String, LocalPrinter> = PrintServiceLookup
        .lookupPrintServices(DocFlavor.BYTE_ARRAY.PDF, null)
        .associate { LocalPrinter(it).let { it.localId to it } }

    val localPrinters: Collection<LocalPrinterInfo> get() = localPrinterMap.values

    // Maps printerId from backend to a local printer
    private val idToPrinter = mutableMapOf<Long, LocalPrinter>()

    suspend fun print(printerId: Long, base64data: String) {
        idToPrinter[printerId]?.print(base64data) ?: throw PrinterWithIdNotFoundException(printerId)
    }

    fun pair(localId: String, backendId: ID) {
        idToPrinter[backendId] = localPrinterMap[localId]!!
        // TODO should the PrinterService send the pairing to the be?
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