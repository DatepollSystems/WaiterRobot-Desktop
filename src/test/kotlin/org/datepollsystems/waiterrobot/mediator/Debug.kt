package org.datepollsystems.waiterrobot.mediator

import kotlinx.coroutines.runBlocking
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinter
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterInfo
import org.datepollsystems.waiterrobot.mediator.printer.service.PrinterDiscoverService
import org.datepollsystems.waiterrobot.mediator.utils.sha256
import java.io.File
import java.util.*
import javax.print.PrintServiceLookup
import javax.print.attribute.HashDocAttributeSet
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.standard.DocumentName
import javax.print.attribute.standard.JobName
import javax.print.attribute.standard.MediaSizeName

/** Some test function for local debugging */
fun main() {
    //listPrinters()
    //printTestFile("")
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
            }supported")
    }
    println("-".repeat(longestName + 30))
}

fun printTestFile(printerName: String) {
    val printer = PrinterDiscoverService.localPrinterMap[printerName.sha256()]
        ?: throw IllegalArgumentException("Printer with name $printerName not found.")
    val file = File("src/test/resources/testPdf.pdf")
    val base64String = Base64.getEncoder().encodeToString(file.readBytes())

    runBlocking {
        printer.printPdf(
            123,
            base64String,
            docAttributes = HashDocAttributeSet(DocumentName("Mediator-TestPrint", null)),
            printAttributes = HashPrintRequestAttributeSet(
                arrayOf(
                    MediaSizeName.ISO_A4,
                    JobName("Mediator-TestPrint", null)
                )
            )
        )
    }
}