package com.example.moneyflow.model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val fileNameFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    // ── Colors ────────────────────────────────────────────────────
    private val colorBg        = Color.parseColor("#38A580")
    private val colorCard      = Color.parseColor("#2D453D")
    private val colorWhite     = Color.WHITE
    private val colorIncome    = Color.parseColor("#4CAF50")
    private val colorExpense   = Color.parseColor("#E53935")
    private val colorGray      = Color.parseColor("#B0D4C4")
    private val colorDivider   = Color.parseColor("#3A5449")

    // ── Page dimensions (A4 @ 72dpi) ─────────────────────────────
    private const val PAGE_WIDTH  = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN      = 40f
    private const val LINE_H      = 22f
    private const val ROW_H       = 32f

    /**
     * Generates a PDF with all transactions for the given user.
     * Returns the File on success, null on failure.
     */
    fun export(context: Context, userId: Long, userLogin: String, db: MoneyFlowDatabaseHelper): File? {
        return try {
            val transactions = db.getAllTransactions(userId)
            val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
            val incomes  = transactions.filter { it.type == TransactionType.INCOME }

            val totalExpense = expenses.sumOf { it.amount }
            val totalIncome  = incomes.sumOf  { it.amount }

            val document = PdfDocument()
            var pageNumber = 1
            var pageInfo  = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page      = document.startPage(pageInfo)
            var canvas    = page.canvas
            var y         = 0f

            // ── Draw header ───────────────────────────────────────
            y = drawHeader(canvas, userLogin, totalIncome, totalExpense, y)

            // ── Summary block ─────────────────────────────────────
            y = drawSummaryBlock(canvas, totalIncome, totalExpense, y)

            y += 16f

            // ── Sections: Расходы then Доходы ─────────────────────
            val sections = listOf(
                "РАСХОДЫ" to expenses,
                "ДОХОДЫ"  to incomes
            )

            for ((sectionTitle, txList) in sections) {
                // Section header
                y = drawSectionTitle(canvas, sectionTitle, y)

                if (txList.isEmpty()) {
                    y = drawEmptyRow(canvas, y)
                } else {
                    y = drawTableHeader(canvas, y)

                    for (tx in txList) {
                        // New page if not enough space
                        if (y + ROW_H > PAGE_HEIGHT - MARGIN) {
                            document.finishPage(page)
                            pageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                            page   = document.startPage(pageInfo)
                            canvas = page.canvas
                            y      = MARGIN
                            y      = drawTableHeader(canvas, y)
                        }
                        y = drawTransactionRow(canvas, tx, y)
                    }
                }
                y += 20f
            }

            document.finishPage(page)

            // ── Save file ─────────────────────────────────────────
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            dir.mkdirs()
            val fileName = "MoneyFlow_${userLogin}_${fileNameFormatter.format(Date())}.pdf"
            val file = File(dir, fileName)
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ── Drawing helpers ───────────────────────────────────────────

    private fun drawHeader(
        canvas: Canvas,
        userLogin: String,
        totalIncome: Double,
        totalExpense: Double,
        startY: Float
    ): Float {
        // Background rect
        val bgPaint = Paint().apply { color = colorBg; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 110f, bgPaint)

        // App name
        val titlePaint = Paint().apply {
            color     = colorWhite
            textSize  = 26f
            isFakeBoldText = true
            isAntiAlias    = true
        }
        canvas.drawText("MoneyFlow", MARGIN, 44f, titlePaint)

        // Subtitle
        val subPaint = Paint().apply { color = colorWhite; textSize = 12f; isAntiAlias = true; alpha = 200 }
        canvas.drawText("Отчёт пользователя: $userLogin", MARGIN, 62f, subPaint)
        canvas.drawText("Дата создания: ${dateFormatter.format(Date())}", MARGIN, 78f, subPaint)

        return 122f
    }

    private fun drawSummaryBlock(canvas: Canvas, totalIncome: Double, totalExpense: Double, y: Float): Float {
        val cardPaint = Paint().apply { color = colorCard; style = Paint.Style.FILL }
        val radius = 8f

        // Card background
        canvas.drawRoundRect(
            MARGIN, y, PAGE_WIDTH - MARGIN, y + 68f,
            radius, radius, cardPaint
        )

        val labelPaint = Paint().apply { color = colorGray; textSize = 11f; isAntiAlias = true }
        val valuePaint = Paint().apply { color = colorWhite; textSize = 16f; isFakeBoldText = true; isAntiAlias = true }
        val incPaint   = Paint().apply { color = colorIncome; textSize = 16f; isFakeBoldText = true; isAntiAlias = true }
        val expPaint   = Paint().apply { color = colorExpense; textSize = 16f; isFakeBoldText = true; isAntiAlias = true }

        val col1 = MARGIN + 16f
        val col2 = PAGE_WIDTH / 2f + 16f

        canvas.drawText("Доходы", col1, y + 22f, labelPaint)
        canvas.drawText("+${totalIncome.toInt()} ₽", col1, y + 44f, incPaint)

        canvas.drawText("Расходы", col2, y + 22f, labelPaint)
        canvas.drawText("-${totalExpense.toInt()} ₽", col2, y + 44f, expPaint)

        val balance = totalIncome - totalExpense
        val balPaint = if (balance >= 0) incPaint else expPaint
        canvas.drawText("Баланс: ${balance.toInt()} ₽", col1, y + 62f, balPaint.apply { textSize = 12f })

        return y + 80f
    }

    private fun drawSectionTitle(canvas: Canvas, title: String, y: Float): Float {
        val paint = Paint().apply {
            color          = colorWhite
            textSize       = 14f
            isFakeBoldText = true
            isAntiAlias    = true
        }
        val accent = if (title == "ДОХОДЫ") colorIncome else colorExpense
        val accentPaint = Paint().apply { color = accent; style = Paint.Style.FILL }
        canvas.drawRect(MARGIN, y + 2f, MARGIN + 4f, y + LINE_H, accentPaint)
        canvas.drawText(title, MARGIN + 10f, y + LINE_H - 4f, paint)
        return y + LINE_H + 8f
    }

    private fun drawTableHeader(canvas: Canvas, y: Float): Float {
        val bgPaint = Paint().apply { color = colorCard; style = Paint.Style.FILL }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + LINE_H + 4f, bgPaint)

        val paint = Paint().apply { color = colorGray; textSize = 11f; isAntiAlias = true }
        canvas.drawText("Дата",       MARGIN + 8f,  y + LINE_H - 2f, paint)
        canvas.drawText("Категория",  MARGIN + 80f, y + LINE_H - 2f, paint)
        canvas.drawText("Примечание", MARGIN + 220f, y + LINE_H - 2f, paint)
        canvas.drawText("Сумма",      PAGE_WIDTH - MARGIN - 70f, y + LINE_H - 2f, paint)
        return y + LINE_H + 6f
    }

    private fun drawTransactionRow(canvas: Canvas, tx: Transaction, y: Float): Float {
        // Alternating row bg
        val divPaint = Paint().apply { color = colorDivider; style = Paint.Style.FILL; alpha = 60 }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + ROW_H - 2f, divPaint)

        val textPaint = Paint().apply { color = colorWhite; textSize = 11f; isAntiAlias = true }
        val amountPaint = Paint().apply {
            color          = if (tx.type == TransactionType.INCOME) colorIncome else colorExpense
            textSize       = 11f
            isFakeBoldText = true
            isAntiAlias    = true
        }

        val textY = y + ROW_H - 10f
        canvas.drawText(dateFormatter.format(Date(tx.date)), MARGIN + 8f,   textY, textPaint)
        canvas.drawText(tx.categoryName.take(18),            MARGIN + 80f,  textY, textPaint)
        canvas.drawText(tx.note.take(22),                    MARGIN + 220f, textY, textPaint)

        val sign   = if (tx.type == TransactionType.INCOME) "+" else "-"
        val amtStr = "$sign${tx.amount.toInt()} ₽"
        val amtX   = PAGE_WIDTH - MARGIN - amountPaint.measureText(amtStr)
        canvas.drawText(amtStr, amtX, textY, amountPaint)

        return y + ROW_H
    }

    private fun drawEmptyRow(canvas: Canvas, y: Float): Float {
        val paint = Paint().apply { color = colorGray; textSize = 11f; isAntiAlias = true; alpha = 180 }
        canvas.drawText("Нет операций", MARGIN + 8f, y + LINE_H, paint)
        return y + LINE_H + 8f
    }
}