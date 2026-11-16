package com.example.util

import com.example.model.TransactionModel
import com.example.model.TransactionType
import java.util.Calendar
import kotlin.math.roundToInt

data class CategorySpendingSummary(
    val categoryName: String,
    val iconKey: String,
    val colorHex: String,
    val amount: Long,
    val sharePercent: Int,
)

data class MonthlySpendingPoint(
    val yearMonth: String,
    val shortMonthLabel: String,
    val fullMonthLabel: String,
    val expenseTotal: Long,
    val incomeTotal: Long,
    val savingsTotal: Long,
    val expenseCount: Int,
    val topCategories: List<CategorySpendingSummary>,
)

data class MonthlySpendingReport(
    val months: List<MonthlySpendingPoint> = emptyList(),
    val totalSixMonthExpense: Long = 0L,
    val totalSixMonthIncome: Long = 0L,
    val averageMonthlyExpense: Long = 0L,
    val highestMonth: MonthlySpendingPoint? = null,
    val lowestActiveMonth: MonthlySpendingPoint? = null,
    val monthOverMonthDeltaPercent: Int? = null,
    val topCategories: List<CategorySpendingSummary> = emptyList(),
    val hasAnyExpenseData: Boolean = false,
)

object SpendingReportCalculator {

    private val shortMonthsId = listOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )

    private val fullMonthsId = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    fun calculateLastSixMonths(
        transactions: List<TransactionModel>,
        referenceDateIso: String = DateUtils.todayIso(),
    ): MonthlySpendingReport {
        val (refYear, refMonthZeroBased) = parseYearMonth(referenceDateIso)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, refYear)
            set(Calendar.MONTH, refMonthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, -5)
        }

        val targetKeys = mutableListOf<Triple<String, String, String>>()
        repeat(6) {
            val year = cal.get(Calendar.YEAR)
            val monthIndex = cal.get(Calendar.MONTH)
            val key = "%04d-%02d".format(year, monthIndex + 1)
            val shortLabel = shortMonthsId.getOrElse(monthIndex) { "" }
            val fullLabel = "${fullMonthsId.getOrElse(monthIndex) { "" }} $year"
            targetKeys.add(Triple(key, shortLabel, fullLabel))
            cal.add(Calendar.MONTH, 1)
        }

        val validKeySet = targetKeys.map { it.first }.toSet()
        val byMonth = transactions
            .filter { it.date.length >= 7 && it.date.substring(0, 7) in validKeySet }
            .groupBy { it.date.substring(0, 7) }

        val monthlyPoints = targetKeys.map { (key, shortLabel, fullLabel) ->
            val monthTxs = byMonth[key].orEmpty()
            var expenseTotal = 0L
            var incomeTotal = 0L
            var savingsTotal = 0L
            var expenseCount = 0
            val expenseTxs = mutableListOf<TransactionModel>()

            monthTxs.forEach { tx ->
                when (TransactionType.fromKey(tx.type)) {
                    TransactionType.EXPENSE -> {
                        expenseTotal += tx.amount
                        expenseCount += 1
                        expenseTxs.add(tx)
                    }
                    TransactionType.INCOME -> incomeTotal += tx.amount
                    TransactionType.SAVINGS -> savingsTotal += tx.amount
                }
            }

            MonthlySpendingPoint(
                yearMonth = key,
                shortMonthLabel = shortLabel,
                fullMonthLabel = fullLabel,
                expenseTotal = expenseTotal,
                incomeTotal = incomeTotal,
                savingsTotal = savingsTotal,
                expenseCount = expenseCount,
                topCategories = summarizeExpenseCategories(expenseTxs, expenseTotal)
            )
        }

        val totalSixMonthExpense = monthlyPoints.sumOf { it.expenseTotal }
        val totalSixMonthIncome = monthlyPoints.sumOf { it.incomeTotal }
        val averageMonthlyExpense = if (monthlyPoints.isNotEmpty()) totalSixMonthExpense / monthlyPoints.size else 0L
        val activeExpenseMonths = monthlyPoints.filter { it.expenseTotal > 0L }
        val highestMonth = activeExpenseMonths.maxByOrNull { it.expenseTotal }
        val lowestActiveMonth = activeExpenseMonths.minByOrNull { it.expenseTotal }

        val currentMonthExpense = monthlyPoints.lastOrNull()?.expenseTotal ?: 0L
        val previousMonthExpense = monthlyPoints.getOrNull(monthlyPoints.size - 2)?.expenseTotal ?: 0L
        val momDeltaPercent = if (previousMonthExpense > 0L) {
            (((currentMonthExpense - previousMonthExpense) * 100.0) / previousMonthExpense).roundToInt()
        } else {
            null
        }

        val allSixMonthExpenses = transactions.filter { tx ->
            tx.date.length >= 7 &&
                tx.date.substring(0, 7) in validKeySet &&
                TransactionType.fromKey(tx.type) == TransactionType.EXPENSE
        }

        return MonthlySpendingReport(
            months = monthlyPoints,
            totalSixMonthExpense = totalSixMonthExpense,
            totalSixMonthIncome = totalSixMonthIncome,
            averageMonthlyExpense = averageMonthlyExpense,
            highestMonth = highestMonth,
            lowestActiveMonth = lowestActiveMonth,
            monthOverMonthDeltaPercent = momDeltaPercent,
            topCategories = summarizeExpenseCategories(allSixMonthExpenses, totalSixMonthExpense),
            hasAnyExpenseData = totalSixMonthExpense > 0L || totalSixMonthIncome > 0L
        )
    }

    private fun summarizeExpenseCategories(
        expenses: List<TransactionModel>,
        totalExpense: Long,
    ): List<CategorySpendingSummary> {
        if (expenses.isEmpty() || totalExpense <= 0L) return emptyList()
        return expenses
            .groupBy { it.categoryName.ifBlank { "Lainnya" } }
            .map { (name, items) ->
                val sum = items.sumOf { it.amount }
                val representative = items.first()
                val pct = ((sum * 100.0) / totalExpense).roundToInt().coerceIn(0, 100)
                CategorySpendingSummary(
                    categoryName = name,
                    iconKey = representative.categoryIcon,
                    colorHex = representative.categoryColorHex,
                    amount = sum,
                    sharePercent = pct
                )
            }
            .sortedByDescending { it.amount }
    }

    private fun parseYearMonth(isoDate: String): Pair<Int, Int> {
        val parts = isoDate.split("-")
        if (parts.size >= 2) {
            val year = parts[0].toIntOrNull()
            val month = parts[1].toIntOrNull()
            if (year != null && month != null && month in 1..12) {
                return year to (month - 1)
            }
        }
        val now = Calendar.getInstance()
        return now.get(Calendar.YEAR) to now.get(Calendar.MONTH)
    }
}
