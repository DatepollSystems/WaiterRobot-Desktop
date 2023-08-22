package org.datepollsystems.waiterrobot.mediator

import co.touchlab.kermit.Logger
import kotlinx.coroutines.*
import org.apache.pdfbox.Loader
import org.apache.pdfbox.printing.PDFPageable
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.core.api.createClient
import org.datepollsystems.waiterrobot.mediator.data.api.AuthApi
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinter
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterInfo
import org.datepollsystems.waiterrobot.mediator.printer.service.PrinterDiscoverService
import org.datepollsystems.waiterrobot.mediator.ws.messages.HelloMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.HelloMessageResponse
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
    println(
        "Found ${allLocalPrinters.count()} local printers. ${localPdfPrinters.count()} of them support BYTE_ARRAY.PDF"
    )
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

// WebSocket debug/test example (credentials must be replaced)
fun websocket(): Unit = runBlocking {
    // Login
    val tokens = AuthApi(createClient(logger = Logger.withTag("Test"))).login("admin@admin.org", "admin")
    Settings.accessToken = tokens.accessToken
    Settings.refreshToken = tokens.refreshToken!!
    Settings.organisationId = 1L

    // Register a Handler for a specific websocket message
    App.socketManager.handle<HelloMessageResponse> {
        println("Handler for HelloMessageResponse called with: $it")

        if (it.body.text != "Hello second") {
            delay(2000)
            App.socketManager.send(HelloMessage(text = "second"))
        }
    }

    App.socketManager.addRegisterMessage(HelloMessage(text = "Test Register"))
    App.socketManager.send(HelloMessage(text = "Test send"))

    try {
        listOf(
            launch(CoroutineName("lauch1")) {
                repeat(15) {
                    println("I'm alive since $it sec.")
                    delay(1_000)
                }
            },
            launch(CoroutineName("lauch2")) {
                delay(10_000)
                App.socketManager.send(HelloMessage(text = "second"))
            },
            launch(CoroutineName("lauch3")) {
                delay(12_000)
                // App.socketManager.send(HelloMessage2(text = "test crash"))
                // App.socketManager.close()
            }
        ).joinAll() // Simulate some "application live time"
    } catch (e: Exception) {
        println(e)
    }
    println("Stopping client")
    App.socketManager.close()
    println("finished")
}
