package com.ivy.domain.statement

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.math.BigDecimal

/** One line of a statement (already formatted for display). */
data class StatementLine(
    val date: String,
    val description: String,
    val amount: String,
)

/** A rendered statement: header, period, lines and a bold net line. */
data class Statement(
    val title: String,
    val subtitle: String,
    val period: String,
    val lines: List<StatementLine>,
    val netLabel: String,
    val netAmount: String,
)

/**
 * Renders a "Statement of account" PDF with the platform's PdfDocument.
 *
 * Plain Helvetica (no external font files), tabular rows and a bold net line.
 * Shared by the Ledger and - later - the Splitly boards.
 */
class StatementRenderer {

    companion object {
        private const val PAGE_WIDTH = 595   // A4 @ 72dpi
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 48f
        private const val ROW_HEIGHT = 22f
        private const val BODY_SIZE = 10f
        private const val TITLE_SIZE = 16f
    }

    /**
     * Writes the PDF into the app cache and returns the file.
     * @param fileName base name (without extension) of the produced file.
     */
    fun render(
        context: Context,
        statement: Statement,
        fileName: String = "statement",
    ): File {
        val document = PdfDocument()
        try {
            var pageNumber = 1
            var page = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textSize = TITLE_SIZE
                color = Color.BLACK
            }
            val subtitlePaint = Paint().apply {
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                textSize = BODY_SIZE + 1
                color = Color.DKGRAY
            }
            val headerPaint = Paint().apply {
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textSize = BODY_SIZE
                color = Color.BLACK
            }
            val bodyPaint = Paint().apply {
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                textSize = BODY_SIZE
                color = Color.BLACK
            }
            val netPaint = Paint().apply {
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textSize = BODY_SIZE + 1
                color = Color.BLACK
            }
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 0.7f
            }

            var y = MARGIN + 20f

            // Header
            canvas.drawText("Statement of account", MARGIN, y, titlePaint)
            y += 22f
            canvas.drawText(statement.title, MARGIN, y, subtitlePaint)
            y += 16f
            canvas.drawText(statement.period, MARGIN, y, subtitlePaint)
            y += 18f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 16f

            // Table header
            canvas.drawText("DATE", MARGIN, y, headerPaint)
            canvas.drawText("DESCRIPTION", MARGIN + 95f, y, headerPaint)
            canvas.drawText("AMOUNT", PAGE_WIDTH - MARGIN - 110f, y, headerPaint)
            y += 14f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 18f

            statement.lines.forEach { line ->
                if (y > PAGE_HEIGHT - MARGIN - 70f) {
                    document.finishPage(page)
                    pageNumber += 1
                    page = document.startPage(
                        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    )
                    canvas = page.canvas
                    y = MARGIN + 20f
                }
                canvas.drawText(line.date, MARGIN, y, bodyPaint)
                canvas.drawText(
                    line.description.take(48),
                    MARGIN + 95f,
                    y,
                    bodyPaint
                )
                canvas.drawText(line.amount, PAGE_WIDTH - MARGIN - 110f, y, bodyPaint)
                y += ROW_HEIGHT
            }

            // Net line
            y += 6f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 18f
            canvas.drawText(statement.netLabel, MARGIN, y, netPaint)
            canvas.drawText(statement.netAmount, PAGE_WIDTH - MARGIN - 110f, y, netPaint)

            document.finishPage(page)

            val outDir = File(context.cacheDir, "statements").apply { if (!exists()) mkdirs() }
            val outFile = File(outDir, "$fileName.pdf")
            outFile.outputStream().use { stream -> document.writeTo(stream) }
            return outFile
        } finally {
            document.close()
        }
    }

    /** Formats an amount with the given symbol (e.g. "Rs." for INR, "₹" for the app). */
    fun formatAmount(
        amount: BigDecimal,
        symbol: String,
    ): String = "$symbol${amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()}"
}
