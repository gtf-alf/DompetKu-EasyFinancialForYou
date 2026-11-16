package com.example.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import com.example.model.TransactionModel
import com.example.model.TransactionType
import com.example.ui.UiState
import com.example.ui.screens.SpendingReportScreen
import com.example.ui.theme.DompetKuTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class SpendingReportTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun sampleSixMonthTransactions(): List<TransactionModel> = listOf(
        // Older than 6 months (should be excluded for ref date 2026-11-16 -> window 2026-06..2026-11)
        TransactionModel(
            id = "tx_old",
            type = TransactionType.EXPENSE.key,
            amount = 9999000L,
            categoryName = "Lama",
            date = "2026-05-20"
        ),
        // June 2026
        TransactionModel(
            id = "tx_jun_1",
            type = TransactionType.EXPENSE.key,
            amount = 1200000L,
            categoryName = "Makanan & Minuman",
            categoryIcon = "fork_knife",
            categoryColorHex = "#F43F5E",
            date = "2026-06-10"
        ),
        // August 2026
        TransactionModel(
            id = "tx_aug_1",
            type = TransactionType.EXPENSE.key,
            amount = 2400000L,
            categoryName = "Belanja Harian",
            categoryIcon = "shopping_bag",
            categoryColorHex = "#A855F7",
            date = "2026-08-14"
        ),
        // September 2026
        TransactionModel(
            id = "tx_sep_1",
            type = TransactionType.EXPENSE.key,
            amount = 3000000L,
            categoryName = "Tagihan & Utilitas",
            categoryIcon = "bolt",
            categoryColorHex = "#EC4899",
            date = "2026-09-05"
        ),
        TransactionModel(
            id = "tx_sep_inc",
            type = TransactionType.INCOME.key,
            amount = 7500000L,
            categoryName = "Gaji Bulanan",
            categoryIcon = "work",
            categoryColorHex = "#10B981",
            date = "2026-09-01"
        ),
        // October 2026
        TransactionModel(
            id = "tx_oct_1",
            type = TransactionType.EXPENSE.key,
            amount = 2000000L,
            categoryName = "Makanan & Minuman",
            categoryIcon = "fork_knife",
            categoryColorHex = "#F43F5E",
            date = "2026-10-18"
        ),
        // November 2026
        TransactionModel(
            id = "tx_nov_1",
            type = TransactionType.EXPENSE.key,
            amount = 1600000L,
            categoryName = "Transportasi & BBM",
            categoryIcon = "car",
            categoryColorHex = "#F97316",
            date = "2026-11-12"
        ),
        TransactionModel(
            id = "tx_nov_inc",
            type = TransactionType.INCOME.key,
            amount = 8000000L,
            categoryName = "Gaji Bulanan",
            categoryIcon = "work",
            categoryColorHex = "#10B981",
            date = "2026-11-01"
        )
    )

    @Test
    fun calculator_aggregatesLastSixMonthsChronologically_andIgnoresOlderMonths() {
        val report = SpendingReportCalculator.calculateLastSixMonths(
            transactions = sampleSixMonthTransactions(),
            referenceDateIso = "2026-11-16"
        )

        assertEquals(listOf("2026-06", "2026-07", "2026-08", "2026-09", "2026-10", "2026-11"), report.months.map { it.yearMonth })
        assertEquals(listOf("Jun", "Jul", "Agu", "Sep", "Okt", "Nov"), report.months.map { it.shortMonthLabel })

        // Total expenses = 1.2M + 0 + 2.4M + 3.0M + 2.0M + 1.6M = 10.2M (excludes 2026-05)
        assertEquals(10_200_000L, report.totalSixMonthExpense)
        assertEquals(1_700_000L, report.averageMonthlyExpense)
        assertEquals("2026-09", report.highestMonth?.yearMonth)
        assertEquals(3_000_000L, report.highestMonth?.expenseTotal)
        // Nov (1.6M) vs Oct (2.0M) = -20%
        assertEquals(-20, report.monthOverMonthDeltaPercent)
        assertNotNull(report.topCategories.firstOrNull())
        assertEquals("Makanan & Minuman", report.topCategories.first().categoryName)
        assertTrue(report.hasAnyExpenseData)
    }

    @Test
    fun compactIdrFormatter_formatsUnitsAndRespectsPrivacyMode() {
        assertEquals("Rp 0", CurrencyUtils.formatCompactIdr(0L))
        assertEquals("Rp 500rb", CurrencyUtils.formatCompactIdr(500_000L))
        assertEquals("Rp 2jt", CurrencyUtils.formatCompactIdr(2_000_000L))
        assertEquals("•••", CurrencyUtils.formatCompactIdr(2_000_000L, isPrivacyHidden = true))
    }

    @Test
    fun spendingReportScreen_rendersVicoChart_switchesModes_andSelectsMonths() {
        val transactions = sampleSixMonthTransactions()
        val report = SpendingReportCalculator.calculateLastSixMonths(transactions, "2026-11-16")
        var privacyHidden by mutableStateOf(false)

        composeRule.setContent {
            DompetKuTheme {
                SpendingReportScreen(
                    transactionsState = UiState.Success(transactions),
                    report = report,
                    isPrivacyHidden = privacyHidden,
                    onTogglePrivacy = { privacyHidden = !privacyHidden }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("spending_report_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Laporan Pengeluaran").assertIsDisplayed()
        composeRule.onNodeWithTag("report_chart_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("vico_spending_chart").performScrollTo().assertIsDisplayed()

        // Switch chart mode to Line and comparison scope
        composeRule.onNodeWithTag("chart_mode_line").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("vico_spending_chart").assertIsDisplayed()

        composeRule.onNodeWithTag("scope_income_vs_expense").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("legend_income").performScrollTo().assertIsDisplayed()

        // Scroll LazyColumn down to Rincian Per Bulan, select September 2026 chip, and check detail card updates
        composeRule.onNodeWithTag("spending_report_list").performScrollToNode(hasTestTag("selected_month_detail_card"))
        composeRule.onNodeWithTag("month_chip_2026-09").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("selected_month_detail_card").assertIsDisplayed()
        composeRule.onNodeWithTag("selected_month_title").assertTextEquals("September 2026")

        // Scroll back up to top header and toggle privacy mode
        composeRule.onNodeWithTag("spending_report_list").performScrollToIndex(0)
        composeRule.onNodeWithTag("report_privacy_toggle").performClick()
        composeRule.waitForIdle()
        assertTrue(privacyHidden)
    }

    @Test
    fun spendingReportScreen_showsEmptyStateWhenNoExpenses() {
        val emptyReport = SpendingReportCalculator.calculateLastSixMonths(emptyList(), "2026-11-16")
        assertFalse(emptyReport.hasAnyExpenseData)

        composeRule.setContent {
            DompetKuTheme {
                SpendingReportScreen(
                    transactionsState = UiState.Success(emptyList()),
                    report = emptyReport,
                    isPrivacyHidden = false,
                    onTogglePrivacy = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("report_chart_empty").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Belum ada pengeluaran tercatat dalam 6 bulan terakhir").assertIsDisplayed()
    }

    @Test
    fun spendingReportScreen_downloadAsPdfButton_invokesCallbackWithSelectedMonth() {
        val transactions = sampleSixMonthTransactions()
        val report = SpendingReportCalculator.calculateLastSixMonths(transactions, "2026-11-16")
        var downloadedMonth: MonthlySpendingPoint? = null

        composeRule.setContent {
            DompetKuTheme {
                SpendingReportScreen(
                    transactionsState = UiState.Success(transactions),
                    report = report,
                    isPrivacyHidden = false,
                    onTogglePrivacy = {},
                    onDownloadPdf = { downloadedMonth = it }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("download_pdf_button").assertIsDisplayed()
        composeRule.onNodeWithText("Download as PDF").assertIsDisplayed()
        composeRule.onNodeWithTag("download_pdf_button").performClick()
        composeRule.waitForIdle()

        assertNotNull(downloadedMonth)
        assertEquals("2026-11", downloadedMonth?.yearMonth)
    }

    @Test
    fun pdfReportExporter_generatesNonEmptyDocument() {
        val transactions = sampleSixMonthTransactions()
        val report = SpendingReportCalculator.calculateLastSixMonths(transactions, "2026-11-16")
        val output = java.io.ByteArrayOutputStream()

        PdfReportExporter.writeMonthlySummaryPdf(
            outputStream = output,
            report = report,
            selectedMonth = report.months.last(),
            transactions = transactions,
            userEmail = "user@example.com",
            generatedDateLabel = "Senin, 16 November 2026"
        )

        val bytes = output.toByteArray()
        assertTrue("Expected generated PDF output to be non-empty", bytes.isNotEmpty())
        val pdfText = bytes.toString(Charsets.ISO_8859_1)
        assertTrue(pdfText.startsWith("%PDF-1.4"))
        assertTrue(pdfText.trimEnd().endsWith("%%EOF"))
        assertTrue(pdfText.contains("DOMPETKU"))
        assertTrue(pdfText.contains("November 2026"))
        assertTrue(pdfText.contains("Makanan & Minuman"))
        assertTrue(pdfText.contains("user@example.com"))
    }
}
