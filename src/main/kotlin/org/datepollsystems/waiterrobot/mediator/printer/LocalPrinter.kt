package org.datepollsystems.waiterrobot.mediator.printer

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable
import org.datepollsystems.waiterrobot.mediator.utils.sha256
import java.awt.print.PrinterJob
import javax.print.PrintService

class LocalPrinter(private val service: PrintService) : AbstractLocalPrinter() {
    override val localId = service.name.sha256() // Name should be unique (on a system level)
    override val name get() = service.name

    override fun printPdf(
        pdfId: String,
        document: PDDocument,
    ) {
        val pageable = PDFPageable(document)

        val printJob = PrinterJob.getPrinterJob()
        printJob.printService = service
        printJob.jobName = pdfId
        printJob.setPageable(pageable)

        // TODO can we somehow listen on the job?

        // TODO pass some attributes?
        printJob.print()
    }

    fun checkOnline(): Boolean {
        TODO("Implement ping or any check if the printer is still available")
    }

    override fun equals(other: Any?): Boolean {
        if (other !is LocalPrinter) return false
        return this.localId == other.localId
    }

    override fun hashCode(): Int = this.localId.hashCode()
}
