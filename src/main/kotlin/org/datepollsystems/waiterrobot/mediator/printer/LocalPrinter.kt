package org.datepollsystems.waiterrobot.mediator.printer

import kotlinx.coroutines.*
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.utils.sha256
import java.util.*
import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.SimpleDoc
import javax.print.attribute.DocAttributeSet
import javax.print.attribute.HashDocAttributeSet
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.PrintRequestAttributeSet
import javax.print.attribute.standard.DocumentName
import javax.print.attribute.standard.JobName
import javax.print.event.PrintJobEvent
import javax.print.event.PrintJobListener

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

    suspend fun printPdf(
        pdfId: ID,
        base64data: String,
        docAttributes: DocAttributeSet? = null,
        printAttributes: PrintRequestAttributeSet? = null
    ): Unit = coroutineScope {
        val coroutineJob: CompletableJob = Job(this.coroutineContext.job)

        val decoded = Base64.getDecoder().decode(base64data)

        val finalDocAttributes = docAttributes ?: HashDocAttributeSet()
        // Add default document attributes
        finalDocAttributes.addIfCategoryNotPresent(DocumentName(pdfId.toString(), null))
        // TODO any other default attributes?

        val doc = SimpleDoc(decoded, DocFlavor.BYTE_ARRAY.PDF, finalDocAttributes)

        val printJob = service.createPrintJob()
        val jobListener = PrintJobListenerImpl(coroutineJob, pdfId)
        printJob.addPrintJobListener(jobListener)

        val finalPrintAttributes = printAttributes ?: HashPrintRequestAttributeSet()
        // Add default job attributes
        finalPrintAttributes.addIfCategoryNotPresent(JobName(pdfId.toString(), null))

        printJob.print(doc, finalPrintAttributes) // TODO pass some attributes (PageSize?)

        try {
            coroutineJob.join()
        } catch (e: CancellationException) {
            if (e.cause is PrintException) {
                throw e.cause!!
            }
            throw e
        } finally {
            printJob.removePrintJobListener(jobListener)
        }
    }

    fun checkOnline(): Boolean {
        TODO("Implement ping or any check if the printer is still available")
    }

    inner class PrintJobListenerImpl(private val job: CompletableJob, private val printId: ID) : PrintJobListener {
        override fun printDataTransferCompleted(pje: PrintJobEvent?) {
            println("print[$printId] data transferred") // TODO logger
        }

        override fun printJobCompleted(pje: PrintJobEvent?) {
            println("print[$printId] completed") // TODO logger
            job.complete()
        }

        override fun printJobFailed(pje: PrintJobEvent?) {
            println("print[$printId] failed") // TODO logger
            job.completeExceptionally(PrintJobFailedException(pje))
        }

        override fun printJobCanceled(pje: PrintJobEvent?) {
            println("print[$printId] canceled") // TODO logger
            job.completeExceptionally(PrintJobCanceledException(pje))
        }

        override fun printJobNoMoreEvents(pje: PrintJobEvent?) {
            println("print[$printId] no more events") // TODO logger
            job.complete() // Printer does "not allow" to verify the "success", so assume it was successful
        }

        override fun printJobRequiresAttention(pje: PrintJobEvent?) {
            println("print[$printId] requires attention") // TODO logger
            job.completeExceptionally(PrintJobRequiresAttentionException(pje))
        }
    }
}