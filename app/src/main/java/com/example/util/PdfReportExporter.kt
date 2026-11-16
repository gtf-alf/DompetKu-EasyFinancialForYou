package com.example.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.model.TransactionModel
import com.example.model.TransactionType
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import kotlin.math.abs

object PdfReportExporter {

    private const val PAGE_WIDTH = 595 // A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 height in points (72 dpi)
    private const val MARGIN_X = 36f
    private const val MARGIN_TOP = 36f
    private const val MARGIN_BOTTOM = 44f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2f)

    // Formal print-friendly fintech palette
    private val COLOR_HEADER_BG = Color.rgb(15, 23, 42)     // Slate 900
    private val COLOR_HEADER_ACCENT = Color.rgb(16, 185, 129) // Emerald 500
    private val COLOR_TEXT_PRIMARY = Color.rgb(15, 23, 42)
    private val COLOR_TEXT_SECONDARY = Color.rgb(71, 85, 105)
    private val COLOR_TEXT_MUTED = Color.rgb(100, 116, 139)
    private val COLOR_BORDER = Color.rgb(203, 213, 225)
    private val COLOR_ROW_ALT = Color.rgb(248, 250, 252)
    private val COLOR_CARD_BG = Color.rgb(241, 245, 249)
    private val COLOR_EMERALD = Color.rgb(5, 150, 105)
    private val COLOR_ROSE = Color.rgb(225, 29, 72)
    private val COLOR_AMBER = Color.rgb(217, 119, 6)

    fun writeMonthlySummaryPdf(
        outputStream: OutputStream,
        report: MonthlySpendingReport,
        selectedMonth: MonthlySpendingPoint? = report.months.lastOrNull(),
        transactions: List<TransactionModel> = emptyList(),
        userEmail: String = "",
        generatedDateLabel: String = DateUtils.formatHeaderDate(),
        isPrivacyHidden: Boolean = false,
    ) {
        val nativeSuccess = try {
            writeWithAndroidPdfDocument(
                outputStream = outputStream,
                report = report,
                selectedMonth = selectedMonth,
                transactions = transactions,
                userEmail = userEmail,
                generatedDateLabel = generatedDateLabel,
                isPrivacyHidden = isPrivacyHidden
            )
            true
        } catch (_: IllegalStateException) {
            // Native Skia PdfDocument is uninitialized in JVM/Robolectric sandboxes.
            false
        } catch (_: UnsatisfiedLinkError) {
            false
        }

        if (!nativeSuccess) {
            writeWithPurePdf14(
                outputStream = outputStream,
                report = report,
                selectedMonth = selectedMonth,
                transactions = transactions,
                userEmail = userEmail,
                generatedDateLabel = generatedDateLabel,
                isPrivacyHidden = isPrivacyHidden
            )
        }
    }

    private fun writeWithAndroidPdfDocument(
        outputStream: OutputStream,
        report: MonthlySpendingReport,
        selectedMonth: MonthlySpendingPoint?,
        transactions: List<TransactionModel>,
        userEmail: String,
        generatedDateLabel: String,
        isPrivacyHidden: Boolean,
    ) {
        val document = PdfDocument()
        try {
            var pageNumber = 1
            var page = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            var canvas = page.canvas
            var y = MARGIN_TOP

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 18f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(203, 213, 225)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_TEXT_PRIMARY
                textSize = 11.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_TEXT_SECONDARY
                textSize = 8.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_TEXT_PRIMARY
                textSize = 9f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_TEXT_PRIMARY
                textSize = 9f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val rightBodyPaint = Paint(boldBodyPaint).apply {
                textAlign = Paint.Align.RIGHT
            }
            val rightRegularPaint = Paint(bodyPaint).apply {
                textAlign = Paint.Align.RIGHT
            }
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
                color = COLOR_BORDER
            }

            fun drawFooter(c: Canvas, pageNum: Int) {
                val footerY = PAGE_HEIGHT - 22f
                c.drawLine(MARGIN_X, footerY - 12f, PAGE_WIDTH - MARGIN_X, footerY - 12f, strokePaint)
                val fPaint = Paint(labelPaint).apply { color = COLOR_TEXT_MUTED; textSize = 8f }
                c.drawText("DompetKu • Ringkasan Keuangan Resmi", MARGIN_X, footerY, fPaint)
                val rightFooter = Paint(fPaint).apply { textAlign = Paint.Align.RIGHT }
                c.drawText("Halaman $pageNum", PAGE_WIDTH - MARGIN_X, footerY, rightFooter)
            }

            fun ensureSpace(neededHeight: Float) {
                if (y + neededHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
                    drawFooter(canvas, pageNumber)
                    document.finishPage(page)
                    pageNumber += 1
                    page = document.startPage(
                        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    )
                    canvas = page.canvas
                    y = MARGIN_TOP
                }
            }

            // 1. Formal Header Banner
            val headerRect = RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 74f)
            fillPaint.color = COLOR_HEADER_BG
            canvas.drawRoundRect(headerRect, 8f, 8f, fillPaint)
            fillPaint.color = COLOR_HEADER_ACCENT
            canvas.drawRect(MARGIN_X, y, MARGIN_X + 6f, y + 74f, fillPaint)

            canvas.drawText("DOMPETKU", MARGIN_X + 18f, y + 26f, titlePaint)
            canvas.drawText(
                "LAPORAN RINGKASAN KEUANGAN BULANAN (6 BULAN TERAKHIR)",
                MARGIN_X + 18f,
                y + 42f,
                subtitlePaint
            )

            val firstMonth = report.months.firstOrNull()?.fullMonthLabel ?: "-"
            val lastMonth = report.months.lastOrNull()?.fullMonthLabel ?: "-"
            val metaLine = buildString {
                append("Periode: $firstMonth – $lastMonth")
                if (userEmail.isNotBlank()) append("  |  Akun: $userEmail")
            }
            canvas.drawText(ellipsize(metaLine, subtitlePaint, CONTENT_WIDTH - 36f), MARGIN_X + 18f, y + 58f, subtitlePaint)
            y += 88f

            // Print Date & Currency Metadata Line
            canvas.drawText("Tanggal Cetak: $generatedDateLabel", MARGIN_X, y, labelPaint)
            val rightMeta = Paint(labelPaint).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText("Mata Uang: IDR (Rupiah)", PAGE_WIDTH - MARGIN_X, y, rightMeta)
            y += 16f

            // 2. Executive Summary KPI Boxes (4 cards)
            canvas.drawText("1. Ringkasan Eksekutif 6 Bulan", MARGIN_X, y, sectionPaint)
            y += 10f

            val cardGap = 8f
            val cardWidth = (CONTENT_WIDTH - cardGap * 3f) / 4f
            val cardHeight = 52f
            val momText = when (val delta = report.monthOverMonthDeltaPercent) {
                null -> "Belum ada data"
                else -> "${if (delta > 0) "+" else ""}$delta% vs bln lalu"
            }

            val kpis = listOf(
                Triple("Total Pemasukan", CurrencyUtils.formatIdr(report.totalSixMonthIncome, isPrivacyHidden), COLOR_EMERALD),
                Triple("Total Pengeluaran", CurrencyUtils.formatIdr(report.totalSixMonthExpense, isPrivacyHidden), COLOR_ROSE),
                Triple("Rata-rata / Bulan", CurrencyUtils.formatIdr(report.averageMonthlyExpense, isPrivacyHidden), COLOR_AMBER),
                Triple(
                    "Bulan Tertinggi",
                    report.highestMonth?.let { CurrencyUtils.formatIdr(it.expenseTotal, isPrivacyHidden) } ?: "Rp 0",
                    COLOR_TEXT_PRIMARY
                )
            )

            kpis.forEachIndexed { index, (label, value, accent) ->
                val left = MARGIN_X + index * (cardWidth + cardGap)
                val rect = RectF(left, y, left + cardWidth, y + cardHeight)
                fillPaint.color = COLOR_CARD_BG
                canvas.drawRoundRect(rect, 6f, 6f, fillPaint)
                canvas.drawRoundRect(rect, 6f, 6f, strokePaint)

                canvas.drawText(label, left + 8f, y + 16f, labelPaint)
                val valPaint = Paint(boldBodyPaint).apply {
                    color = accent
                    textSize = 9.5f
                }
                canvas.drawText(ellipsize(value, valPaint, cardWidth - 16f), left + 8f, y + 32f, valPaint)

                val subText = when (index) {
                    3 -> report.highestMonth?.shortMonthLabel ?: "-"
                    1 -> momText
                    else -> "6 bulan terakhir"
                }
                val subPaint = Paint(labelPaint).apply { textSize = 7.5f; color = COLOR_TEXT_MUTED }
                canvas.drawText(ellipsize(subText, subPaint, cardWidth - 16f), left + 8f, y + 44f, subPaint)
            }
            y += cardHeight + 18f

            // 3. Focused Month Summary Box (if available)
            if (selectedMonth != null) {
                ensureSpace(76f)
                canvas.drawText("2. Fokus Bulan Terpilih: ${selectedMonth.fullMonthLabel}", MARGIN_X, y, sectionPaint)
                y += 8f
                val focusRect = RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 44f)
                fillPaint.color = COLOR_ROW_ALT
                canvas.drawRoundRect(focusRect, 6f, 6f, fillPaint)
                canvas.drawRoundRect(focusRect, 6f, 6f, strokePaint)

                val netCashFlow = selectedMonth.incomeTotal - selectedMonth.expenseTotal
                val netFormatted = if (isPrivacyHidden) {
                    "Rp •••••••"
                } else {
                    "${if (netCashFlow >= 0) "+" else "-"}${CurrencyUtils.formatIdr(abs(netCashFlow))}"
                }

                val colW = CONTENT_WIDTH / 4f
                val focusCols = listOf(
                    Triple("Pemasukan", CurrencyUtils.formatIdr(selectedMonth.incomeTotal, isPrivacyHidden), COLOR_EMERALD),
                    Triple("Pengeluaran", CurrencyUtils.formatIdr(selectedMonth.expenseTotal, isPrivacyHidden), COLOR_ROSE),
                    Triple("Tabungan", CurrencyUtils.formatIdr(selectedMonth.savingsTotal, isPrivacyHidden), COLOR_AMBER),
                    Triple("Arus Kas Bersih", netFormatted, if (netCashFlow >= 0) COLOR_EMERALD else COLOR_ROSE)
                )
                focusCols.forEachIndexed { idx, (lbl, valStr, clr) ->
                    val cx = MARGIN_X + 10f + idx * colW
                    canvas.drawText(lbl, cx, y + 16f, labelPaint)
                    val vp = Paint(boldBodyPaint).apply { color = clr; textSize = 9.5f }
                    canvas.drawText(ellipsize(valStr, vp, colW - 14f), cx, y + 32f, vp)
                }
                y += 60f
            }

            // 4. 6-Month Cash Flow Breakdown Table
            ensureSpace(170f)
            canvas.drawText("3. Rincian Tren Bulanan (6 Bulan Terakhir)", MARGIN_X, y, sectionPaint)
            y += 10f

            val rowHeight = 20f
            val colMonthX = MARGIN_X + 8f
            val colIncomeX = MARGIN_X + 175f
            val colExpenseX = MARGIN_X + 275f
            val colSavingsX = MARGIN_X + 370f
            val colNetX = MARGIN_X + 472f
            val colCountX = PAGE_WIDTH - MARGIN_X - 8f

            // Table header
            fillPaint.color = COLOR_HEADER_BG
            canvas.drawRoundRect(RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowHeight), 4f, 4f, fillPaint)
            val thPaint = Paint(boldBodyPaint).apply { color = Color.WHITE; textSize = 8.5f }
            val thRightPaint = Paint(thPaint).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText("Bulan", colMonthX, y + 13f, thPaint)
            canvas.drawText("Pemasukan", colIncomeX, y + 13f, thRightPaint)
            canvas.drawText("Pengeluaran", colExpenseX, y + 13f, thRightPaint)
            canvas.drawText("Tabungan", colSavingsX, y + 13f, thRightPaint)
            canvas.drawText("Arus Bersih", colNetX, y + 13f, thRightPaint)
            canvas.drawText("Tx", colCountX, y + 13f, thRightPaint)
            y += rowHeight

            report.months.forEachIndexed { idx, m ->
                ensureSpace(rowHeight + 24f)
                if (idx % 2 == 1) {
                    fillPaint.color = COLOR_ROW_ALT
                    canvas.drawRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowHeight, fillPaint)
                }
                val net = m.incomeTotal - m.expenseTotal
                val netStr = if (isPrivacyHidden) {
                    "Rp •••••"
                } else {
                    "${if (net >= 0) "+" else "-"}${CurrencyUtils.formatIdr(abs(net))}"
                }

                canvas.drawText(m.fullMonthLabel, colMonthX, y + 13f, bodyPaint)
                canvas.drawText(
                    CurrencyUtils.formatIdr(m.incomeTotal, isPrivacyHidden),
                    colIncomeX,
                    y + 13f,
                    Paint(rightRegularPaint).apply { color = COLOR_EMERALD }
                )
                canvas.drawText(
                    CurrencyUtils.formatIdr(m.expenseTotal, isPrivacyHidden),
                    colExpenseX,
                    y + 13f,
                    Paint(rightRegularPaint).apply { color = COLOR_ROSE }
                )
                canvas.drawText(
                    CurrencyUtils.formatIdr(m.savingsTotal, isPrivacyHidden),
                    colSavingsX,
                    y + 13f,
                    rightRegularPaint
                )
                canvas.drawText(
                    netStr,
                    colNetX,
                    y + 13f,
                    Paint(rightBodyPaint).apply { color = if (net >= 0) COLOR_EMERALD else COLOR_ROSE }
                )
                canvas.drawText(m.expenseCount.toString(), colCountX, y + 13f, rightRegularPaint)
                canvas.drawLine(MARGIN_X, y + rowHeight, PAGE_WIDTH - MARGIN_X, y + rowHeight, strokePaint)
                y += rowHeight
            }

            // Total Row
            val totalSavings = report.months.sumOf { it.savingsTotal }
            val totalNet = report.totalSixMonthIncome - report.totalSixMonthExpense
            val totalTx = report.months.sumOf { it.expenseCount }
            val totalNetStr = if (isPrivacyHidden) {
                "Rp •••••"
            } else {
                "${if (totalNet >= 0) "+" else "-"}${CurrencyUtils.formatIdr(abs(totalNet))}"
            }
            fillPaint.color = COLOR_CARD_BG
            canvas.drawRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowHeight, fillPaint)
            canvas.drawText("TOTAL 6 BULAN", colMonthX, y + 13f, boldBodyPaint)
            canvas.drawText(CurrencyUtils.formatIdr(report.totalSixMonthIncome, isPrivacyHidden), colIncomeX, y + 13f, rightBodyPaint)
            canvas.drawText(CurrencyUtils.formatIdr(report.totalSixMonthExpense, isPrivacyHidden), colExpenseX, y + 13f, rightBodyPaint)
            canvas.drawText(CurrencyUtils.formatIdr(totalSavings, isPrivacyHidden), colSavingsX, y + 13f, rightBodyPaint)
            canvas.drawText(totalNetStr, colNetX, y + 13f, rightBodyPaint)
            canvas.drawText(totalTx.toString(), colCountX, y + 13f, rightBodyPaint)
            y += rowHeight + 18f

            // 5. Category Expense Composition
            ensureSpace(70f)
            canvas.drawText("4. Kategori Pengeluaran Terbesar (6 Bulan)", MARGIN_X, y, sectionPaint)
            y += 10f

            if (report.topCategories.isEmpty()) {
                canvas.drawText("Belum ada transaksi pengeluaran dalam periode 6 bulan ini.", MARGIN_X, y + 10f, labelPaint)
                y += 24f
            } else {
                report.topCategories.take(8).forEachIndexed { idx, cat ->
                    ensureSpace(rowHeight + 10f)
                    if (idx % 2 == 1) {
                        fillPaint.color = COLOR_ROW_ALT
                        canvas.drawRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowHeight, fillPaint)
                    }
                    canvas.drawText(
                        ellipsize("${idx + 1}. ${cat.categoryName}", bodyPaint, 220f),
                        MARGIN_X + 8f,
                        y + 13f,
                        bodyPaint
                    )
                    val barLeft = MARGIN_X + 240f
                    val barMaxW = 140f
                    fillPaint.color = Color.rgb(226, 232, 240)
                    canvas.drawRoundRect(RectF(barLeft, y + 7f, barLeft + barMaxW, y + 13f), 3f, 3f, fillPaint)
                    val pctW = (barMaxW * (cat.sharePercent / 100f)).coerceAtLeast(3f)
                    fillPaint.color = COLOR_ROSE
                    canvas.drawRoundRect(RectF(barLeft, y + 7f, barLeft + pctW, y + 13f), 3f, 3f, fillPaint)

                    canvas.drawText("${cat.sharePercent}%", MARGIN_X + 415f, y + 13f, rightRegularPaint)
                    canvas.drawText(
                        CurrencyUtils.formatIdr(cat.amount, isPrivacyHidden),
                        PAGE_WIDTH - MARGIN_X - 8f,
                        y + 13f,
                        rightBodyPaint
                    )
                    canvas.drawLine(MARGIN_X, y + rowHeight, PAGE_WIDTH - MARGIN_X, y + rowHeight, strokePaint)
                    y += rowHeight
                }
                y += 16f
            }

            // 6. Recent Expenses for Selected Month (if any)
            if (selectedMonth != null && transactions.isNotEmpty()) {
                val monthExpenses = transactions
                    .filter {
                        it.date.startsWith(selectedMonth.yearMonth) &&
                            TransactionType.fromKey(it.type) == TransactionType.EXPENSE
                    }
                    .take(10)

                if (monthExpenses.isNotEmpty()) {
                    ensureSpace(60f)
                    canvas.drawText(
                        "5. Catatan Pengeluaran ${selectedMonth.fullMonthLabel}",
                        MARGIN_X,
                        y,
                        sectionPaint
                    )
                    y += 10f

                    monthExpenses.forEachIndexed { idx, tx ->
                        ensureSpace(rowHeight + 10f)
                        if (idx % 2 == 1) {
                            fillPaint.color = COLOR_ROW_ALT
                            canvas.drawRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowHeight, fillPaint)
                        }
                        canvas.drawText(DateUtils.formatShortIndonesianDate(tx.date), MARGIN_X + 8f, y + 13f, labelPaint)
                        val desc = if (tx.note.isNotBlank()) "${tx.categoryName} — ${tx.note}" else tx.categoryName
                        canvas.drawText(ellipsize(desc, bodyPaint, 295f), MARGIN_X + 95f, y + 13f, bodyPaint)
                        canvas.drawText(
                            CurrencyUtils.formatIdr(tx.amount, isPrivacyHidden),
                            PAGE_WIDTH - MARGIN_X - 8f,
                            y + 13f,
                            Paint(rightBodyPaint).apply { color = COLOR_ROSE }
                        )
                        canvas.drawLine(MARGIN_X, y + rowHeight, PAGE_WIDTH - MARGIN_X, y + rowHeight, strokePaint)
                        y += rowHeight
                    }
                }
            }

            drawFooter(canvas, pageNumber)
            document.finishPage(page)
            document.writeTo(outputStream)
        } finally {
            document.close()
        }
    }

    /**
     * Pure-Kotlin spec-compliant PDF 1.4 generator used when native Skia PdfDocument
     * is unavailable (such as JVM/Robolectric unit tests).
     */
    internal fun writeWithPurePdf14(
        outputStream: OutputStream,
        report: MonthlySpendingReport,
        selectedMonth: MonthlySpendingPoint?,
        transactions: List<TransactionModel>,
        userEmail: String,
        generatedDateLabel: String,
        isPrivacyHidden: Boolean,
    ) {
        val stream = StringBuilder()

        fun pdfY(topY: Float): Float = PAGE_HEIGHT - topY

        fun drawRect(x: Float, topY: Float, w: Float, h: Float, r: Float, g: Float, b: Float) {
            stream.append(java.util.Locale.US.let { String.format(it, "%.2f %.2f %.2f rg %.2f %.2f %.2f %.2f re f\n", r, g, b, x, pdfY(topY + h), w, h) })
        }

        fun drawLine(x1: Float, topY1: Float, x2: Float, topY2: Float) {
            stream.append(String.format(java.util.Locale.US, "0.80 0.84 0.88 RG 0.8 w %.2f %.2f m %.2f %.2f l S\n", x1, pdfY(topY1), x2, pdfY(topY2)))
        }

        fun drawText(text: String, x: Float, topY: Float, size: Float, bold: Boolean = false, r: Float = 0.06f, g: Float = 0.09f, b: Float = 0.16f) {
            val fontRef = if (bold) "/F2" else "/F1"
            val escaped = escapePdfText(text)
            stream.append(String.format(java.util.Locale.US, "BT %s %.1f Tf %.2f %.2f %.2f rg %.2f %.2f Td (%s) Tj ET\n", fontRef, size, r, g, b, x, pdfY(topY), escaped))
        }

        var y = MARGIN_TOP
        drawRect(MARGIN_X, y, CONTENT_WIDTH, 72f, 0.06f, 0.09f, 0.16f)
        drawRect(MARGIN_X, y, 6f, 72f, 0.06f, 0.73f, 0.51f)
        drawText("DOMPETKU", MARGIN_X + 18f, y + 24f, 18f, bold = true, r = 1f, g = 1f, b = 1f)
        drawText("LAPORAN RINGKASAN KEUANGAN BULANAN (6 BULAN TERAKHIR)", MARGIN_X + 18f, y + 40f, 9.5f, r = 0.8f, g = 0.84f, b = 0.88f)
        val firstMonth = report.months.firstOrNull()?.fullMonthLabel ?: "-"
        val lastMonth = report.months.lastOrNull()?.fullMonthLabel ?: "-"
        val meta = "Periode: $firstMonth - $lastMonth" + if (userEmail.isNotBlank()) " | Akun: $userEmail" else ""
        drawText(meta, MARGIN_X + 18f, y + 56f, 9f, r = 0.8f, g = 0.84f, b = 0.88f)
        y += 88f

        drawText("Tanggal Cetak: $generatedDateLabel | Mata Uang: IDR (Rupiah)", MARGIN_X, y, 8.5f, r = 0.28f, g = 0.33f, b = 0.41f)
        y += 18f

        drawText("1. Ringkasan Eksekutif 6 Bulan", MARGIN_X, y, 11.5f, bold = true)
        y += 16f
        drawText("Total Pemasukan: ${CurrencyUtils.formatIdr(report.totalSixMonthIncome, isPrivacyHidden)}", MARGIN_X, y, 9.5f)
        drawText("Total Pengeluaran: ${CurrencyUtils.formatIdr(report.totalSixMonthExpense, isPrivacyHidden)}", MARGIN_X + 260f, y, 9.5f)
        y += 14f
        drawText("Rata-rata / Bulan: ${CurrencyUtils.formatIdr(report.averageMonthlyExpense, isPrivacyHidden)}", MARGIN_X, y, 9.5f)
        drawText("Bulan Tertinggi: ${report.highestMonth?.fullMonthLabel ?: "-"} (${CurrencyUtils.formatIdr(report.highestMonth?.expenseTotal ?: 0L, isPrivacyHidden)})", MARGIN_X + 260f, y, 9.5f)
        y += 22f

        if (selectedMonth != null) {
            drawText("2. Fokus Bulan Terpilih: ${selectedMonth.fullMonthLabel}", MARGIN_X, y, 11.5f, bold = true)
            y += 14f
            drawText("Pemasukan: ${CurrencyUtils.formatIdr(selectedMonth.incomeTotal, isPrivacyHidden)} | Pengeluaran: ${CurrencyUtils.formatIdr(selectedMonth.expenseTotal, isPrivacyHidden)}", MARGIN_X, y, 9f)
            y += 20f
        }

        drawText("3. Rincian Tren Bulanan (6 Bulan Terakhir)", MARGIN_X, y, 11.5f, bold = true)
        y += 14f
        report.months.forEach { m ->
            drawLine(MARGIN_X, y + 4f, PAGE_WIDTH - MARGIN_X, y + 4f)
            drawText("${m.fullMonthLabel}: Masuk ${CurrencyUtils.formatIdr(m.incomeTotal, isPrivacyHidden)} | Keluar ${CurrencyUtils.formatIdr(m.expenseTotal, isPrivacyHidden)} (${m.expenseCount} tx)", MARGIN_X + 4f, y, 9f)
            y += 16f
        }
        y += 12f

        drawText("4. Kategori Pengeluaran Terbesar (6 Bulan)", MARGIN_X, y, 11.5f, bold = true)
        y += 14f
        report.topCategories.take(8).forEachIndexed { idx, cat ->
            drawText("${idx + 1}. ${cat.categoryName} (${cat.sharePercent}%): ${CurrencyUtils.formatIdr(cat.amount, isPrivacyHidden)}", MARGIN_X + 4f, y, 9f)
            y += 15f
        }

        if (selectedMonth != null && transactions.isNotEmpty()) {
            val monthExpenses = transactions.filter {
                it.date.startsWith(selectedMonth.yearMonth) && TransactionType.fromKey(it.type) == TransactionType.EXPENSE
            }.take(8)
            if (monthExpenses.isNotEmpty()) {
                y += 10f
                drawText("5. Catatan Pengeluaran ${selectedMonth.fullMonthLabel}", MARGIN_X, y, 11.5f, bold = true)
                y += 14f
                monthExpenses.forEach { tx ->
                    drawText("${tx.date} - ${tx.categoryName}: ${CurrencyUtils.formatIdr(tx.amount, isPrivacyHidden)}", MARGIN_X + 4f, y, 9f)
                    y += 14f
                }
            }
        }

        drawText("DompetKu - Ringkasan Keuangan Resmi | Halaman 1", MARGIN_X, PAGE_HEIGHT - 22f, 8f, r = 0.39f, g = 0.45f, b = 0.55f)

        val contentBytes = stream.toString().toByteArray(Charsets.ISO_8859_1)
        val out = ByteArrayOutputStream()
        val offsets = mutableListOf<Int>()

        fun writeAscii(s: String) {
            out.write(s.toByteArray(Charsets.ISO_8859_1))
        }

        writeAscii("%PDF-1.4\n")

        offsets.add(out.size())
        writeAscii("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")

        offsets.add(out.size())
        writeAscii("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n")

        offsets.add(out.size())
        writeAscii("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $PAGE_WIDTH $PAGE_HEIGHT] /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>\nendobj\n")

        offsets.add(out.size())
        writeAscii("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n")

        offsets.add(out.size())
        writeAscii("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n")

        offsets.add(out.size())
        writeAscii("6 0 obj\n<< /Length ${contentBytes.size} >>\nstream\n")
        out.write(contentBytes)
        writeAscii("\nendstream\nendobj\n")

        val xrefOffset = out.size()
        writeAscii("xref\n0 ${offsets.size + 1}\n")
        writeAscii("0000000000 65535 f \n")
        offsets.forEach { offset ->
            writeAscii("%010d 00000 n \n".format(offset))
        }
        writeAscii("trailer\n<< /Size ${offsets.size + 1} /Root 1 0 R >>\nstartxref\n$xrefOffset\n%%EOF\n")

        outputStream.write(out.toByteArray())
        outputStream.flush()
    }

    private fun escapePdfText(input: String): String {
        val sb = StringBuilder(input.length)
        input.forEach { ch ->
            when (ch) {
                '\\' -> sb.append("\\\\")
                '(' -> sb.append("\\(")
                ')' -> sb.append("\\)")
                '\u2013', '\u2014' -> sb.append('-')
                '\u2022' -> sb.append('*')
                in '\u0020'..'\u007E' -> sb.append(ch)
                else -> sb.append('?')
            }
        }
        return sb.toString()
    }

    private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val suffix = "..."
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + suffix) > maxWidth) {
            end -= 1
        }
        return if (end <= 0) suffix else text.substring(0, end) + suffix
    }
}
