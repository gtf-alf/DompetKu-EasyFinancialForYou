package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CategoryCatalog
import com.example.model.TransactionModel
import com.example.ui.UiState
import com.example.ui.components.SpendingTrendChartCard
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.util.CategorySpendingSummary
import com.example.util.CurrencyUtils
import com.example.util.MonthlySpendingPoint
import com.example.util.MonthlySpendingReport
import kotlin.math.abs

@Composable
fun SpendingReportScreen(
    transactionsState: UiState<List<TransactionModel>>,
    report: MonthlySpendingReport,
    isPrivacyHidden: Boolean,
    onTogglePrivacy: () -> Unit,
    onDownloadPdf: (MonthlySpendingPoint?) -> Unit = {},
) {
    val months = report.months
    var selectedYearMonth by remember(months) {
        mutableStateOf(months.lastOrNull()?.yearMonth ?: "")
    }
    val selectedMonth = remember(months, selectedYearMonth) {
        months.firstOrNull { it.yearMonth == selectedYearMonth } ?: months.lastOrNull()
    }

    val rangeLabel = remember(months) {
        val first = months.firstOrNull()?.shortMonthLabel
        val last = months.lastOrNull()?.fullMonthLabel
        if (!first.isNullOrBlank() && !last.isNullOrBlank()) "$first – $last" else "6 Bulan Terakhir"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("spending_report_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .testTag("spending_report_list"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Laporan Pengeluaran",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Tren 6 bulan terakhir ($rangeLabel)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                    IconButton(
                        onClick = onTogglePrivacy,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate850)
                            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                            .testTag("report_privacy_toggle")
                    ) {
                        Icon(
                            imageVector = if (isPrivacyHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (isPrivacyHidden) "Tampilkan nominal" else "Sembunyikan nominal",
                            tint = if (isPrivacyHidden) Amber400 else Slate400
                        )
                    }
                }
            }

            // Download as PDF Action Button
            if (transactionsState !is UiState.Loading && transactionsState !is UiState.Error) {
                item {
                    Button(
                        onClick = { onDownloadPdf(selectedMonth) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = com.example.ui.theme.Slate950
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("download_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Download as PDF",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            if (transactionsState is UiState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Emerald500)
                    }
                }
            } else if (transactionsState is UiState.Error) {
                item {
                    Surface(
                        color = Rose500.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Rose500.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    ) {
                        Text(
                            text = transactionsState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Rose400,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                // Summary KPI Grid (2x2)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ReportKpiCard(
                                title = "Total 6 Bulan",
                                value = CurrencyUtils.formatIdr(report.totalSixMonthExpense, isPrivacyHidden),
                                subtitle = "Akumulasi pengeluaran",
                                accent = Rose400,
                                icon = Icons.Rounded.Insights,
                                modifier = Modifier.weight(1f)
                            )
                            ReportKpiCard(
                                title = "Rata-rata / Bulan",
                                value = CurrencyUtils.formatIdr(report.averageMonthlyExpense, isPrivacyHidden),
                                subtitle = "Pembagi 6 bulan",
                                accent = Amber400,
                                icon = Icons.Rounded.CalendarMonth,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val peak = report.highestMonth
                            ReportKpiCard(
                                title = "Bulan Tertinggi",
                                value = peak?.let { CurrencyUtils.formatIdr(it.expenseTotal, isPrivacyHidden) } ?: "Rp 0",
                                subtitle = peak?.fullMonthLabel ?: "Belum ada data",
                                accent = Rose400,
                                icon = Icons.Rounded.LocalFireDepartment,
                                modifier = Modifier.weight(1f)
                            )

                            val momDelta = report.monthOverMonthDeltaPercent
                            val (momValue, momSubtitle, momAccent, momIcon) = when {
                                momDelta == null -> Quad(
                                    "–",
                                    "Belum ada pembanding",
                                    Slate400,
                                    Icons.AutoMirrored.Rounded.TrendingUp
                                )
                                momDelta < 0 -> Quad(
                                    "${momDelta}%",
                                    "Turun vs bulan lalu",
                                    Emerald400,
                                    Icons.AutoMirrored.Rounded.TrendingDown
                                )
                                momDelta > 0 -> Quad(
                                    "+${momDelta}%",
                                    "Naik vs bulan lalu",
                                    Rose400,
                                    Icons.AutoMirrored.Rounded.TrendingUp
                                )
                                else -> Quad(
                                    "0%",
                                    "Sama seperti bulan lalu",
                                    Amber400,
                                    Icons.AutoMirrored.Rounded.TrendingUp
                                )
                            }
                            ReportKpiCard(
                                title = "Perubahan MoM",
                                value = momValue,
                                subtitle = momSubtitle,
                                accent = momAccent,
                                icon = momIcon,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Vico Chart Card
                item {
                    SpendingTrendChartCard(
                        report = report,
                        isPrivacyHidden = isPrivacyHidden
                    )
                }

                // Interactive Month Selector & Monthly Breakdown
                if (months.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Rincian Per Bulan",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                months.forEach { month ->
                                    val isSelected = month.yearMonth == selectedMonth?.yearMonth
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else Slate850)
                                            .then(
                                                if (!isSelected) Modifier.border(1.dp, Slate800, CircleShape) else Modifier
                                            )
                                            .clickable { selectedYearMonth = month.yearMonth }
                                            .padding(horizontal = 16.dp, vertical = 9.dp)
                                            .testTag("month_chip_${month.yearMonth}")
                                    ) {
                                        Text(
                                            text = month.shortMonthLabel,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) Slate900 else Slate300
                                        )
                                    }
                                }
                            }

                            if (selectedMonth != null) {
                                SelectedMonthDetailCard(
                                    month = selectedMonth,
                                    averageMonthlyExpense = report.averageMonthlyExpense,
                                    isPrivacyHidden = isPrivacyHidden
                                )
                            }
                        }
                    }
                }

                // Top Categories Across Last 6 Months
                item {
                    Text(
                        text = "Kategori Pengeluaran Terbesar (6 Bulan)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                if (report.topCategories.isEmpty()) {
                    item {
                        Surface(
                            color = Slate850.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Slate800, RoundedCornerShape(18.dp))
                        ) {
                            Text(
                                text = "Belum ada kategori pengeluaran dalam periode 6 bulan ini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }
                } else {
                    items(
                        items = report.topCategories,
                        key = { it.categoryName }
                    ) { category ->
                        CategorySpendBreakdownRow(
                            summary = category,
                            isPrivacyHidden = isPrivacyHidden
                        )
                    }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun ReportKpiCard(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Slate850,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.border(1.dp, Slate800, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SelectedMonthDetailCard(
    month: MonthlySpendingPoint,
    averageMonthlyExpense: Long,
    isPrivacyHidden: Boolean,
) {
    val diffFromAvg = month.expenseTotal - averageMonthlyExpense
    val vsAvgLabel = when {
        averageMonthlyExpense <= 0L -> "Belum ada rata-rata pembanding"
        diffFromAvg > 0L -> "${CurrencyUtils.formatIdr(abs(diffFromAvg), isPrivacyHidden)} di atas rata-rata"
        diffFromAvg < 0L -> "${CurrencyUtils.formatIdr(abs(diffFromAvg), isPrivacyHidden)} di bawah rata-rata"
        else -> "Tepat di rata-rata 6 bulan"
    }
    val vsAvgColor = when {
        averageMonthlyExpense <= 0L -> Slate400
        diffFromAvg > 0L -> Rose400
        else -> Emerald400
    }

    Surface(
        color = Slate850,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate700.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
            .testTag("selected_month_detail_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = month.fullMonthLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.testTag("selected_month_title")
                    )
                    Text(
                        text = "${month.expenseCount} transaksi pengeluaran",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(vsAvgColor.copy(alpha = 0.14f))
                        .border(1.dp, vsAvgColor.copy(alpha = 0.3f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isPrivacyHidden) "Mode Privasi" else vsAvgLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = vsAvgColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniFlowPill(
                    label = "Pengeluaran",
                    amount = CurrencyUtils.formatIdr(month.expenseTotal, isPrivacyHidden),
                    color = Rose400,
                    modifier = Modifier.weight(1f)
                )
                MiniFlowPill(
                    label = "Pemasukan",
                    amount = CurrencyUtils.formatIdr(month.incomeTotal, isPrivacyHidden),
                    color = Emerald400,
                    modifier = Modifier.weight(1f)
                )
            }

            if (month.topCategories.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Slate800)
                )
                Text(
                    text = "Teratas di ${month.shortMonthLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate300
                )
                month.topCategories.take(3).forEach { cat ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${cat.categoryName} (${cat.sharePercent}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate300,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = CurrencyUtils.formatIdr(cat.amount, isPrivacyHidden),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Rose400
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniFlowPill(
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Slate900)
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Slate400
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CategorySpendBreakdownRow(
    summary: CategorySpendingSummary,
    isPrivacyHidden: Boolean,
) {
    val accent = CategoryCatalog.parseColor(summary.colorHex, Rose400)
    val icon = CategoryCatalog.resolveIcon(summary.iconKey)
    val progress by animateFloatAsState(
        targetValue = (summary.sharePercent / 100f).coerceIn(0.03f, 1f),
        animationSpec = spring(stiffness = 260f),
        label = "catProgress"
    )

    Surface(
        color = Slate850,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.16f))
                        .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = summary.categoryName,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.categoryName,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${summary.sharePercent}% dari total pengeluaran",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
                Text(
                    text = CurrencyUtils.formatIdr(summary.amount, isPrivacyHidden),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Slate900)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
            }
        }
    }
}
