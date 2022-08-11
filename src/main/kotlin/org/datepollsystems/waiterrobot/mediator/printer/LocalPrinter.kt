package org.datepollsystems.waiterrobot.mediator.printer

import org.apache.pdfbox.Loader
import org.apache.pdfbox.printing.PDFPageable
import org.datepollsystems.waiterrobot.mediator.utils.sha256
import java.awt.print.PrinterJob
import java.util.*
import javax.print.PrintService

interface LocalPrinterInfo {
    val localId: String
    val name: String
}

/**
 * Manages a local Printer
 *
 * for an example see [org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterTest.testPrint]
 *
 * @author Fabian Schedler
 * @see org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterTest.testPrint
 */
class LocalPrinter(private val service: PrintService) : LocalPrinterInfo {
    override val localId = service.name.sha256() // Name should be unique (on a system level)
    override val name get() = service.name

    fun printPdf(
        pdfId: String,
        base64data: String,
    ) {
        val decoded = Base64.getDecoder().decode(base64data)
        val doc = PDFPageable(Loader.loadPDF(decoded))

        val printJob = PrinterJob.getPrinterJob()
        printJob.printService = service
        printJob.jobName = pdfId
        printJob.setPageable(doc)

        // TODO can we somehow listen on the job?

        // TODO pass some attributes?
        printJob.print()
    }

    fun checkOnline(): Boolean {
        TODO("Implement ping or any check if the printer is still available")
    }
}