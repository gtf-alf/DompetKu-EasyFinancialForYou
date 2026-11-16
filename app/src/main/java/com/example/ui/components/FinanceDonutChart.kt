package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.TransactionType
import com.example.ui.DashboardMetrics
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.util.CurrencyUtils
import kotlin.math.atan2
import kotlin.math.hypot

@Composable
fun FinanceDistributionCard(
    metrics: DashboardMetrics,
    isPrivacyHidden: Boolean,
    modifier: Modifier = Modifier,
) {
    var selectedPillar by remember { mutableStateOf<TransactionType?>(null) }

    val volume = (metrics.totalIncome + metrics.totalExpense + metrics.totalSavings).toFloat()
    val isEmpty = volume <= 0f

    val incomeTargetSweep = if (isEmpty) 0f else (metrics.totalIncome / volume) * 360f
    val expenseTargetSweep = if (isEmpty) 0f else (metrics.totalExpense / volume) * 360f
    val savingsTargetSweep = if (isEmpty) 0f else (metrics.totalSavings / volume) * 360f

    val incomeSweep by animateFloatAsState(
        targetValue = incomeTargetSweep,
        animationSpec = spring(stiffness = 220f),
        label = "incomeSweep"
    )
    val expenseSweep by animateFloatAsState(
        targetValue = expenseTargetSweep,
        animationSpec = spring(stiffness = 220f),
        label = "expenseSweep"
    )
    val savingsSweep by animateFloatAsState(
        targetValue = savingsTargetSweep,
        animationSpec = spring(stiffness = 220f),
        label = "savingsSweep"
    )

    Surface(
        color = Slate850,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Distribusi Keuangan",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Ketuk grafik untuk detail nominal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Amber500.copy(alpha = 0.15f))
                        .border(1.dp, Amber500.copy(alpha = 0.3f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Rasio Tabungan: ${metrics.savingsRatePercent}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Amber400
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Interactive Donut Canvas
                Box(
                    modifier = Modifier.size(132.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(isEmpty, incomeSweep, expenseSweep, savingsSweep) {
                                detectTapGestures { offset ->
                                    if (isEmpty) return@detectTapGestures
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = offset.x - center.x
                                    val dy = offset.y - center.y
                                    val dist = hypot(dx, dy)
                                    val outerR = size.width / 2f
                                    if (dist < outerR * 0.45f || dist > outerR) {
                                        selectedPillar = null
                                        return@detectTapGestures
                                    }
                                    // Angle from -90 deg (12 o'clock) clockwise 0..360
                                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                                    if (angle < 0f) angle += 360f

                                    selectedPillar = when {
                                        angle < incomeSweep -> TransactionType.INCOME
                                        angle < incomeSweep + expenseSweep -> TransactionType.EXPENSE
                                        else -> TransactionType.SAVINGS
                                    }
                                }
                            }
                    ) {
                        val strokeWidth = 18.dp.toPx()
                        val inset = strokeWidth / 2f
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(inset, inset)

                        if (isEmpty) {
                            drawArc(
                                color = Slate800,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )
                        } else {
                            var start = -90f
                            val slices = listOf(
                                Triple(TransactionType.INCOME, incomeSweep, Emerald500),
                                Triple(TransactionType.EXPENSE, expenseSweep, Rose500),
                                Triple(TransactionType.SAVINGS, savingsSweep, Amber500)
                            )
                            slices.forEach { (type, sweep, color) ->
                                if (sweep > 0.5f) {
                                    val isSelected = selectedPillar == type
                                    drawArc(
                                        color = color,
                                        startAngle = start,
                                        sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(
                                            width = if (isSelected) strokeWidth + 4.dp.toPx() else strokeWidth,
                                            cap = StrokeCap.Butt
                                        )
                                    )
                                    start += sweep
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selectedPillar?.labelId ?: "Total Arus",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                        Text(
                            text = "${metrics.totalCount} Tx",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Legend Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LegendRow(
                        label = "Pemasukan",
                        percent = metrics.incomePercent,
                        amount = metrics.totalIncome,
                        dotColor = Emerald500,
                        textColor = Emerald400,
                        isSelected = selectedPillar == TransactionType.INCOME,
                        isPrivacyHidden = isPrivacyHidden,
                        onClick = {
                            selectedPillar = if (selectedPillar == TransactionType.INCOME) null else TransactionType.INCOME
                        }
                    )
                    LegendRow(
                        label = "Pengeluaran",
                        percent = metrics.expensePercent,
                        amount = metrics.totalExpense,
                        dotColor = Rose500,
                        textColor = Rose400,
                        isSelected = selectedPillar == TransactionType.EXPENSE,
                        isPrivacyHidden = isPrivacyHidden,
                        onClick = {
                            selectedPillar = if (selectedPillar == TransactionType.EXPENSE) null else TransactionType.EXPENSE
                        }
                    )
                    LegendRow(
                        label = "Tabungan",
                        percent = metrics.savingsPercent,
                        amount = metrics.totalSavings,
                        dotColor = Amber500,
                        textColor = Amber400,
                        isSelected = selectedPillar == TransactionType.SAVINGS,
                        isPrivacyHidden = isPrivacyHidden,
                        onClick = {
                            selectedPillar = if (selectedPillar == TransactionType.SAVINGS) null else TransactionType.SAVINGS
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendRow(
    label: String,
    percent: Int,
    amount: Long,
    dotColor: Color,
    textColor: Color,
    isSelected: Boolean,
    isPrivacyHidden: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Slate900 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate300
                )
            }
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }
        if (isSelected) {
            Text(
                text = CurrencyUtils.formatIdr(amount, isPrivacyHidden),
                style = MaterialTheme.typography.labelSmall,
                color = Slate400,
                modifier = Modifier.padding(start = 18.dp, top = 2.dp)
            )
        }
    }
}
