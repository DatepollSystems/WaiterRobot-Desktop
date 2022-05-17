
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable
import java.awt.print.PrinterException
import java.awt.print.PrinterJob
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import javax.print.PrintService

class PrinterManagerKotlin {
    companion object {
        fun printOnPrinter(printerName: String, documentPath: String): Boolean {
            findPrintService(printerName)
            val loPrinterJob = PrinterJob.getPrinterJob()
            val loPageFormat = loPrinterJob.defaultPage()
            val loPaper = loPageFormat.paper
            loPageFormat.paper = loPaper
            var document: PDDocument? = null
            try {
                document = PDDocument.load(File(documentPath))
            } catch (e: IOException) {
                e.printStackTrace()
            }
            loPrinterJob.setPageable(PDFPageable(document))
            try {
                loPrinterJob.print()
            } catch (e: PrinterException) {
                e.printStackTrace()
            }
            return true
        }


        fun findPrinters(): String{
            var printersConcat = ""

            // Get array of all print services - sort order NOT GUARANTEED!
            val services = PrinterJob.lookupPrintServices()

            // Retrieve specified print service from the array
            for (index in services.indices) {
                printersConcat += """
            ${services[index].name},
            
            """.trimIndent()
            }

            // Return the print service
            return printersConcat
        }

        /**
         * Retrieve the specified Print Service; will return null if not found.
         * @return
         */
        fun findPrintService(printerName: String): PrintService? {
            var service: PrintService? = null

            // Get array of all print services - sort order NOT GUARANTEED!
            val services = PrinterJob.lookupPrintServices()

            // Retrieve specified print service from the array
            var index = 0
            while (service == null && index < services.size) {
                if (services[index].name.equals(printerName, ignoreCase = true)) {
                    service = services[index]
                }
                index++
            }

            // Return the print service
            return service
        }

        /**
         * Retrieve a PrinterJob instance set with the PrinterService using the printerName.
         *
         * @return
         * @throws Exception IllegalStateException if expected printer is not found.
         */
        @Throws(Exception::class)
        fun findPrinterJob(printerName: String): PrinterJob? {

            // Retrieve the Printer Service
            val printService = PrinterManagerKotlin.findPrintService(printerName)
                ?: throw IllegalStateException("Unrecognized Printer Service \"$printerName\"")

            // Validate the Printer Service

            // Obtain a Printer Job instance.
            val printerJob = PrinterJob.getPrinterJob()

            // Set the Print Service.
            printerJob.printService = printService

            // Return Print Job
            return printerJob
        }


        /**
         * Printer list does not necessarily refresh if you change the list of
         * printers within the O/S; you can run this to refresh if necessary.
         */
        /*
    public static void refreshSystemPrinterList() {

        Class[] classes = PrintServiceLookup.class.getDeclaredClasses();

        for (int i = 0; i < classes.length; i++) {

            if ("javax.print.PrintServiceLookup$Services".equals(classes[i].getName())) {

                sun.awt.AppContext.getAppContext().remove(classes[i]);
                break;
            }
        }
    }
     */
    }
}