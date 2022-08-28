package org.datepollsystems.waiterrobot.mediator.printer

import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import javax.print.PrintServiceLookup
import kotlin.test.Ignore
import kotlin.test.Test

internal class LocalPrinterTest {
    /** E2E Test
     *
     * Prints a test page on the default printer of the current device, must be verified manually
     * (at least by having a look if a job with the name "Mediator-TestPrint" was added to the print queue)
     */
    @Test
    @Ignore // Queues a print should not be executed by default
    fun testPrint() = runBlocking {
        val printer = LocalPrinter(PrintServiceLookup.lookupDefaultPrintService())
        val file = File("src/test/resources/testBill.pdf")
        val base64String = Base64.getEncoder().encodeToString(file.readBytes())

        printer.printPdf("test", base64String)
    }
}