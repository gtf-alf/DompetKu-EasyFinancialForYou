package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.util.CurrencyUtils
import com.example.util.MonthlySpendingReport
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.point
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlin.math.roundToLong

enum class TrendChartStyle { BAR, LINE }
enum class TrendSeriesScope { EXPENSE_ONLY, INCOME_VS_EXPENSE }

@Composable
fun SpendingTrendChartCard(
    report: MonthlySpendingReport,
    isPrivacyHidden: Boolean,
    modifier: Modifier = Modifier,
) {
    var chartStyle by remember { mutableStateOf(TrendChartStyle.BAR) }
    var seriesScope by remember { mutableStateOf(TrendSeriesScope.EXPENSE_ONLY) }

    val months = report.months
    val monthLabels = remember(months) { months.map { it.shortMonthLabel } }
    val expenseValues = remember(months) { months.map { it.expenseTotal.toDouble() } }
    val incomeValues = remember(months) { months.map { it.incomeTotal.toDouble() } }
    val hasChartValues = remember(seriesScope, expenseValues, incomeValues) {
        when (seriesScope) {
            TrendSeriesScope.EXPENSE_ONLY -> expenseValues.any { it > 0.0 }
            TrendSeriesScope.INCOME_VS_EXPENSE -> expenseValues.any { it > 0.0 } || incomeValues.any { it > 0.0 }
        }
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(chartStyle, seriesScope, expenseValues, incomeValues, hasChartValues) {
        if (!hasChartValues) return@LaunchedEffect
        modelProducer.runTransaction {
            when (chartStyle) {
                TrendChartStyle.BAR -> {
                    columnSeries {
                        if (seriesScope == TrendSeriesScope.INCOME_VS_EXPENSE) {
                            series(incomeValues)
                        }
                        series(expenseValues)
                    }
                }
                TrendChartStyle.LINE -> {
                    lineSeries {
                        if (seriesScope == TrendSeriesScope.INCOME_VS_EXPENSE) {
                            series(incomeValues)
                        }
                        series(expenseValues)
                    }
                }
            }
        }
    }

    val axisLabelComponent = rememberTextComponent(
        color = Slate400,
        textSize = 11.sp
    )
    val guideline = rememberAxisGuidelineComponent(
        fill = fill(Slate800)
    )
    val bottomAxisFormatter = remember(monthLabels) {
        CartesianValueFormatter { _, x, _ ->
            monthLabels.getOrNull(x.toInt()) ?: ""
        }
    }
    val startAxisFormatter = remember(isPrivacyHidden) {
        CartesianValueFormatter { _, y, _ ->
            CurrencyUtils.formatCompactIdr(y.roundToLong(), isPrivacyHidden)
        }
    }

    val expenseColumn = rememberLineComponent(
        fill = fill(Rose500),
        thickness = 16.dp,
        shape = CorneredShape.rounded(topLeftPercent = 35, topRightPercent = 35)
    )
    val incomeColumn = rememberLineComponent(
        fill = fill(Emerald500),
        thickness = 12.dp,
        shape = CorneredShape.rounded(topLeftPercent = 35, topRightPercent = 35)
    )
    val expensePoint = rememberShapeComponent(
        fill = fill(Rose400),
        shape = CorneredShape.Pill
    )
    val incomePoint = rememberShapeComponent(
        fill = fill(Emerald400),
        shape = CorneredShape.Pill
    )

    val columnLayer = rememberColumnCartesianLayer(
        columnProvider = if (seriesScope == TrendSeriesScope.INCOME_VS_EXPENSE) {
            ColumnCartesianLayer.ColumnProvider.series(incomeColumn, expenseColumn)
        } else {
            ColumnCartesianLayer.ColumnProvider.series(expenseColumn)
        },
        columnCollectionSpacing = 14.dp
    )

    val expenseLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(Rose500)),
        areaFill = LineCartesianLayer.AreaFill.single(
            fill(
                ShaderProvider.verticalGradient(
                    Rose500.copy(alpha = 0.34f).toArgb(),
                    Color.Transparent.toArgb()
                )
            )
        ),
        pointProvider = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.point(component = expensePoint, size = 8.dp)
        )
    )
    val incomeLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(Emerald500)),
        areaFill = LineCartesianLayer.AreaFill.single(
            fill(
                ShaderProvider.verticalGradient(
                    Emerald500.copy(alpha = 0.22f).toArgb(),
                    Color.Transparent.toArgb()
                )
            )
        ),
        pointProvider = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.point(component = incomePoint, size = 7.dp)
        )
    )

    val lineLayer = rememberLineCartesianLayer(
        lineProvider = if (seriesScope == TrendSeriesScope.INCOME_VS_EXPENSE) {
            LineCartesianLayer.LineProvider.series(incomeLine, expenseLine)
        } else {
            LineCartesianLayer.LineProvider.series(expenseLine)
        }
    )

    Surface(
        color = Slate850,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(24.dp))
            .testTag("report_chart_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Grafik Tren 6 Bulan",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Visualisasi pengeluaran bulanan dengan Vico",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }

                // Style toggle: Bar vs Line
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate900)
                        .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ChartStyleToggleChip(
                        selected = chartStyle == TrendChartStyle.BAR,
                        label = "Batang",
                        icon = Icons.Rounded.BarChart,
                        onClick = { chartStyle = TrendChartStyle.BAR },
                        modifier = Modifier.testTag("chart_mode_bar")
                    )
                    ChartStyleToggleChip(
                        selected = chartStyle == TrendChartStyle.LINE,
                        label = "Garis",
                        icon = Icons.AutoMirrored.Rounded.ShowChart,
                        onClick = { chartStyle = TrendChartStyle.LINE },
                        modifier = Modifier.testTag("chart_mode_line")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scope Pills: Pengeluaran vs Masuk vs Keluar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScopePill(
                    label = "Pengeluaran",
                    dotColor = Rose500,
                    selected = seriesScope == TrendSeriesScope.EXPENSE_ONLY,
                    onClick = { seriesScope = TrendSeriesScope.EXPENSE_ONLY },
                    modifier = Modifier.testTag("scope_expense_only")
                )
                ScopePill(
                    label = "Masuk vs Keluar",
                    dotColor = Emerald500,
                    selected = seriesScope == TrendSeriesScope.INCOME_VS_EXPENSE,
                    onClick = { seriesScope = TrendSeriesScope.INCOME_VS_EXPENSE },
                    modifier = Modifier.testTag("scope_income_vs_expense")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!hasChartValues) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Slate900.copy(alpha = 0.7f))
                        .border(1.dp, Slate800, RoundedCornerShape(18.dp))
                        .padding(20.dp)
                        .testTag("report_chart_empty"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum ada pengeluaran tercatat dalam 6 bulan terakhir",
                            style = MaterialTheme.typography.titleSmall,
                            color = Slate300,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Catat transaksi pengeluaran di Beranda untuk melihat tren grafik di sini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val activeLayer = if (chartStyle == TrendChartStyle.BAR) columnLayer else lineLayer
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        activeLayer,
                        startAxis = VerticalAxis.rememberStart(
                            label = axisLabelComponent,
                            guideline = guideline,
                            valueFormatter = startAxisFormatter
                        ),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            label = axisLabelComponent,
                            guideline = null,
                            valueFormatter = bottomAxisFormatter
                        )
                    ),
                    modelProducer = modelProducer,
                    scrollState = rememberVicoScrollState(scrollEnabled = false),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(225.dp)
                        .testTag("vico_spending_chart")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Legend Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendIndicator(
                        dotColor = Rose500,
                        label = "Pengeluaran",
                        textColor = Rose400,
                        modifier = Modifier.testTag("legend_expense")
                    )
                    if (seriesScope == TrendSeriesScope.INCOME_VS_EXPENSE) {
                        LegendIndicator(
                            dotColor = Emerald500,
                            label = "Pemasukan",
                            textColor = Emerald400,
                            modifier = Modifier.testTag("legend_income")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartStyleToggleChip(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) Slate700 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Slate400,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) Color.White else Slate400
        )
    }
}

@Composable
private fun ScopePill(
    label: String,
    dotColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) Slate800 else Slate900.copy(alpha = 0.7f))
            .border(
                1.dp,
                if (selected) dotColor.copy(alpha = 0.55f) else Slate800,
                CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (selected) Color.White else Slate400
        )
    }
}

@Composable
private fun LegendIndicator(
    dotColor: Color,
    label: String,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = textColor
        )
    }
}
