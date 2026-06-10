package com.spendly.financetracker.data.service

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.spendly.financetracker.data.model.BudgetProgress
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.SavingsGoal
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.model.UserProfile
import com.spendly.financetracker.ui.util.formatMoney
import com.spendly.financetracker.ui.util.nextMonthStart
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class ExportedReport(
    val uri: Uri,
    val mimeType: String,
    val fileName: String
)

@Singleton
class ReportExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun exportCsv(
        profile: UserProfile?,
        transactions: List<FinanceTransaction>,
        selectedMonthStart: Long
    ): Result<ExportedReport> = runCatching {
        cleanupExpiredExports()
        val monthEnd = nextMonthStart(selectedMonthStart)
        val rows = transactions.filter { it.dateMillis in selectedMonthStart until monthEnd }
        val file = exportFile("spendly-transactions-${selectedMonthStart}.csv")
        file.writeText(buildString {
            appendLine("Spendly Transactions")
            appendLine("Profile,${csv(profile?.name?.takeIf { it.isNotBlank() } ?: "Spendly User")}")
            appendLine("Month,${csv(monthTitle(selectedMonthStart))}")
            appendLine("Date,Type,Name,Category,Source,Amount,Original Currency,Payment Method,Expense Type")
            rows.forEach { tx ->
                appendLine(
                    listOf(
                        date(tx.dateMillis),
                        tx.type.name,
                        tx.title,
                        tx.category,
                        tx.source,
                        formatMoney(tx.amountCents),
                        tx.originalCurrency,
                        tx.paymentMethod.orEmpty(),
                        tx.expenseType?.name.orEmpty()
                    ).joinToString(",") { csv(it) }
                )
            }
        })
        ExportedReport(uriFor(file), "text/csv", file.name)
    }

    fun exportPdf(
        profile: UserProfile?,
        transactions: List<FinanceTransaction>,
        budgets: List<BudgetProgress>,
        goals: List<SavingsGoal>,
        selectedMonthStart: Long
    ): Result<ExportedReport> = runCatching {
        cleanupExpiredExports()
        val monthEnd = nextMonthStart(selectedMonthStart)
        val rows = transactions.filter { it.dateMillis in selectedMonthStart until monthEnd }
        val income = rows.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents }
        val expense = rows.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
        val net = income - expense
        val categoryTotals = rows
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category.ifBlank { "Other" } }
            .mapValues { entry -> entry.value.sumOf { it.amountCents } }
            .toList()
            .sortedByDescending { it.second }
        val ledgerRows = rows.sortedWith(
            compareByDescending<FinanceTransaction> { it.dateMillis }
                .thenByDescending { it.createdAtMillis }
                .thenByDescending { it.updatedAtMillis }
        )
        val file = exportFile("spendly-report-${selectedMonthStart}.pdf")
        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 0
        var currentPage: PdfDocument.Page? = null
        lateinit var canvas: android.graphics.Canvas
        val green = Color.rgb(28, 163, 120)
        val teal = Color.rgb(27, 184, 155)
        val red = Color.rgb(219, 65, 78)
        val amber = Color.rgb(224, 168, 0)
        val purple = Color.rgb(124, 92, 255)
        val darkText = Color.rgb(27, 33, 47)
        val mutedText = Color.rgb(111, 121, 137)
        val lineColor = Color.rgb(224, 230, 235)
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 25f; isFakeBoldText = true; color = Color.WHITE }
        val whiteSmall = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f; color = Color.WHITE }
        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 15f; isFakeBoldText = true; color = darkText }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10.5f; color = darkText }
        val muted = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9.5f; color = mutedText }
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val lightBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(246, 250, 248) }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 8.5f; color = mutedText }

        fun paint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        fun drawRoundRect(left: Float, top: Float, right: Float, bottom: Float, color: Int, radius: Float = 18f) {
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint(color))
        }
        fun drawProgress(left: Float, top: Float, width: Float, percent: Float, color: Int) {
            drawRoundRect(left, top, left + width, top + 8f, Color.rgb(235, 241, 238), 6f)
            drawRoundRect(left, top, left + width * percent.coerceIn(0f, 1f), top + 8f, color, 6f)
        }
        fun drawCard(left: Float, top: Float, right: Float, bottom: Float) {
            canvas.drawRoundRect(left, top, right, bottom, 16f, 16f, cardPaint)
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = lineColor
            }.also { canvas.drawRoundRect(left, top, right, bottom, 16f, 16f, it) }
        }
        fun finishCurrentPage() {
            val page = currentPage ?: return
            canvas.drawText("Generated by Spendly • Page $pageNumber", 40f, 822f, footerPaint)
            pdf.finishPage(page)
            currentPage = null
        }
        fun startPage(): android.graphics.Canvas {
            finishCurrentPage()
            pageNumber += 1
            val page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            currentPage = page
            val c = page.canvas
            c.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), lightBg)
            canvas = c
            return c
        }
        fun ellipsize(value: String, max: Int): String {
            val cleaned = value.replace('\n', ' ').replace('\r', ' ').trim()
            return if (cleaned.length <= max) cleaned else cleaned.take((max - 1).coerceAtLeast(0)) + "…"
        }
        fun transactionMeta(tx: FinanceTransaction): String = buildString {
            append(if (tx.type == TransactionType.INCOME) tx.source.ifBlank { "Income" } else tx.category.ifBlank { "Expense" })
            tx.paymentMethod?.takeIf { it.isNotBlank() }?.let { append(" • $it") }
            tx.expenseType?.let { append(" • ${it.name.lowercase().replaceFirstChar { ch -> ch.uppercase() }}") }
            if (tx.isRecurring || tx.recurringRuleId != null) append(" • Recurring")
            if (tx.goalId != null) append(" • Goal linked")
            append(if (tx.isSynced) " • Synced" else " • Not synced")
        }
        fun currencyMeta(tx: FinanceTransaction): String = buildString {
            append("${tx.originalCurrency} ${"%.2f".format(Locale.US, tx.originalAmount)}")
            append(" → ${formatMoney(tx.amountCents)}")
            tx.exchangeRate?.let { append(" @ ${"%.4f".format(Locale.US, it)}") }
            tx.cryptoCoin?.takeIf { it.isNotBlank() }?.let { append(" • $it") }
        }

        startPage()
        canvas.drawRect(0f, 0f, 595f, 842f, lightBg)
        drawRoundRect(30f, 28f, 565f, 128f, green, 26f)
        drawRoundRect(360f, 28f, 565f, 128f, teal, 26f)
        canvas.drawText("Spendly Monthly Report", 50f, 66f, title)
        canvas.drawText(monthTitle(selectedMonthStart), 50f, 88f, whiteSmall)
        canvas.drawText(profile?.name?.takeIf { it.isNotBlank() } ?: "Personal finance backup", 50f, 106f, whiteSmall)
        profile?.email?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText(it, 50f, 122f, whiteSmall)
        }

        var y = 154f
        val cardWidth = 163f
        listOf(
            Triple("Income", formatMoney(income), green),
            Triple("Expenses", formatMoney(expense), red),
            Triple("Net Savings", formatMoney(net), if (net >= 0L) green else red)
        ).forEachIndexed { index, item ->
            val left = 40f + index * (cardWidth + 12f)
            drawCard(left, y, left + cardWidth, y + 72f)
            canvas.drawText(item.first, left + 14f, y + 25f, muted)
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 16f
                isFakeBoldText = true
                color = item.third
            }.also { canvas.drawText(item.second, left + 14f, y + 52f, it) }
        }

        y += 104f
        drawCard(40f, y, 555f, y + 146f)
        canvas.drawText("Spending by Category", 58f, y + 28f, heading)
        var rowY = y + 52f
        if (categoryTotals.isEmpty()) {
            canvas.drawText("No expense records for this month.", 58f, rowY, muted)
        } else {
            categoryTotals.take(8).forEachIndexed { index, entry ->
                val color = listOf(green, amber, red, Color.rgb(45, 127, 249), purple)[index % 5]
                val percent = if (expense > 0L) entry.second.toFloat() / expense.toFloat() else 0f
                canvas.drawCircle(62f, rowY - 4f, 4.5f, paint(color))
                canvas.drawText(entry.first.take(18), 74f, rowY, body)
                canvas.drawText(formatMoney(entry.second), 230f, rowY, body)
                canvas.drawText("${(percent * 100).toInt()}%", 318f, rowY, muted)
                drawProgress(360f, rowY - 8f, 150f, percent, color)
                rowY += 18f
            }
        }

        y += 172f
        drawCard(40f, y, 555f, y + 126f)
        canvas.drawText("Budget Progress", 58f, y + 28f, heading)
        rowY = y + 52f
        if (budgets.isEmpty()) {
            canvas.drawText("No category budgets configured for this month.", 58f, rowY, muted)
        } else {
            budgets.take(4).forEach {
                val color = when {
                    it.isExceeded -> red
                    it.isWarning -> amber
                    else -> green
                }
                canvas.drawText(it.budget.category.take(18), 58f, rowY, body)
                canvas.drawText("${it.progressPercent}% used", 190f, rowY, muted)
                canvas.drawText("${formatMoney(it.spentCents)} / ${formatMoney(it.budget.limitCents)}", 270f, rowY, body)
                drawProgress(420f, rowY - 8f, 90f, it.progressPercent / 100f, color)
                rowY += 18f
            }
        }

        y += 152f
        drawCard(40f, y, 555f, y + 112f)
        canvas.drawText("Savings Goals", 58f, y + 28f, heading)
        rowY = y + 52f
        if (goals.isEmpty()) {
            canvas.drawText("No goals created yet.", 58f, rowY, muted)
        } else {
            goals.take(3).forEach {
                val percent = if (it.targetCents > 0L) it.savedCents.toFloat() / it.targetCents.toFloat() else 0f
                canvas.drawText(it.title.take(24), 58f, rowY, body)
                canvas.drawText("${formatMoney(it.savedCents)} / ${formatMoney(it.targetCents)}", 220f, rowY, body)
                canvas.drawText("${it.progressPercent}%", 382f, rowY, muted)
                drawProgress(420f, rowY - 8f, 90f, percent, purple)
                rowY += 18f
            }
        }

        startPage()
        drawRoundRect(30f, 28f, 565f, 90f, green, 22f)
        canvas.drawText("Full Transaction Details", 48f, 62f, title)
        canvas.drawText(monthTitle(selectedMonthStart), 390f, 62f, whiteSmall)
        y = 118f
        if (ledgerRows.isEmpty()) {
            drawCard(40f, y, 555f, y + 80f)
            canvas.drawText("No transactions found for this month.", 58f, y + 44f, body)
        } else {
            ledgerRows.forEachIndexed { index, tx ->
                val rowHeight = if (tx.note.isBlank()) 72f else 90f
                if (y + rowHeight > 790f) {
                    startPage()
                    drawRoundRect(30f, 28f, 565f, 80f, green, 20f)
                    canvas.drawText("Full Transaction Details", 48f, 60f, heading.apply { color = Color.WHITE })
                    heading.color = darkText
                    y = 108f
                }
                val typeColor = if (tx.type == TransactionType.INCOME) green else red
                drawCard(40f, y, 555f, y + rowHeight - 8f)
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 12.5f
                    isFakeBoldText = true
                    color = typeColor
                }.also { canvas.drawText(if (tx.type == TransactionType.INCOME) "INCOME" else "EXPENSE", 58f, y + 24f, it) }
                canvas.drawText("#${index + 1}", 126f, y + 24f, muted)
                canvas.drawText(date(tx.dateMillis), 160f, y + 24f, body)
                canvas.drawText(ellipsize(tx.title.ifBlank { "Untitled transaction" }, 30), 250f, y + 24f, body)
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 13f
                    isFakeBoldText = true
                    color = typeColor
                    textAlign = Paint.Align.RIGHT
                }.also { canvas.drawText(formatMoney(tx.amountCents), 535f, y + 24f, it) }
                canvas.drawText(ellipsize(transactionMeta(tx), 78), 58f, y + 44f, muted)
                canvas.drawText(ellipsize(currencyMeta(tx), 78), 58f, y + 60f, muted)
                if (tx.note.isNotBlank()) {
                    canvas.drawText("Note: ${ellipsize(tx.note, 82)}", 58f, y + 76f, muted)
                }
                y += rowHeight
            }
        }

        finishCurrentPage()
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        ExportedReport(uriFor(file), "application/pdf", file.name)
    }

    private fun exportFile(name: String): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        return File(dir, name)
    }

    private fun cleanupExpiredExports(nowMillis: Long = System.currentTimeMillis()) {
        File(context.cacheDir, "exports").listFiles()?.forEach { file ->
            if (nowMillis - file.lastModified() > EXPORT_RETENTION_MILLIS) {
                file.delete()
            }
        }
    }

    private fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    private fun csv(value: String): String = "\"${value.replace("\"", "\"\"")}\""

    private fun date(timeMillis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timeMillis))

    private fun monthTitle(timeMillis: Long): String =
        SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(timeMillis))

    private companion object {
        const val EXPORT_RETENTION_MILLIS = 24L * 60L * 60L * 1000L
    }
}
