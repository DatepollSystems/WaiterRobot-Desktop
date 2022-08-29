package org.datepollsystems.waiterrobot.mediator

import org.apache.pdfbox.Loader
import org.apache.pdfbox.printing.PDFPageable
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinter
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterInfo
import org.datepollsystems.waiterrobot.mediator.printer.service.PrinterDiscoverService
import java.awt.print.PrinterJob
import java.io.File
import javax.print.PrintServiceLookup

/** Some test function for local debugging */
fun main() {
    // listPrinters()
    // printTestFile("")
}

fun listPrinters() {
    val localPdfPrinters = PrinterDiscoverService.localPrinters
    val allLocalPrinters: List<LocalPrinterInfo> =
        PrintServiceLookup.lookupPrintServices(null, null).map { LocalPrinter(it) }
    val longestName = allLocalPrinters.maxOf { it.name.length } + 5
    println("-".repeat(longestName + 30))
    println("Found ${allLocalPrinters.count()} local printers. ${localPdfPrinters.count()} of them support BYTE_ARRAY.PDF")
    println("-".repeat(longestName + 30))
    allLocalPrinters.forEach { printer ->
        println(
            "${printer.name.padEnd(longestName, ' ')} BYTE_ARRAY.PDF ${
            localPdfPrinters.find { printer.localId == it.localId }?.let { "" } ?: "not "
            }supported"
        )
    }
    println("-".repeat(longestName + 30))
}

fun listPrinterCapabilities(printerName: String) {
    val printer = PrintServiceLookup.lookupPrintServices(null, null).find { it.name == printerName }
        ?: throw IllegalArgumentException("Printer with name $printerName not found.")

    println("Supported attribute categories")
    printer.supportedAttributeCategories.forEach {
        println(it.name)
    }
}

fun printTestFile(printerName: String) {
    val printer = PrintServiceLookup.lookupPrintServices(null, null).find { it.name == printerName }
        ?: throw IllegalArgumentException("Printer with name $printerName not found.")
    val file = File("src/test/resources/testBill.pdf")
    val doc = Loader.loadPDF(file.readBytes()) // Use readBytes as the real printing will also use raw bytes

    val job = PrinterJob.getPrinterJob()
    job.setPageable(PDFPageable(doc))
    job.printService = printer
    job.print()
}