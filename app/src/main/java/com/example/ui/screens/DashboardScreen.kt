package com.example.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CategoryCatalog
import com.example.model.TransactionModel
import com.example.model.TransactionType
import com.example.ui.DashboardMetrics
import com.example.ui.UiState
import com.example.ui.components.FinanceDistributionCard
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
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
import com.example.util.DateUtils

@Composable
fun DashboardScreen(
    transactionsState: UiState<List<TransactionModel>>,
    filteredTransactions: List<TransactionModel>,
    metrics: DashboardMetrics,
    selectedFilter: TransactionType?,
    isPrivacyHidden: Boolean,
    onTogglePrivacy: () -> Unit,
    onSelectFilter: (TransactionType?) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onLoadSampleData: () -> Unit,
    onOpenReport: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Hero Balance Card
            item {
                HeroBalanceCard(
                    metrics = metrics,
                    isPrivacyHidden = isPrivacyHidden,
                    onTogglePrivacy = onTogglePrivacy
                )
            }

            // 2. Interactive Donut Chart Card
            item {
                FinanceDistributionCard(
                    metrics = metrics,
                    isPrivacyHidden = isPrivacyHidden
                )
            }

            if (onOpenReport != null) {
                item {
                    Surface(
                        color = Slate850,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, Slate800, RoundedCornerShape(18.dp))
                            .clickable(onClick = onOpenReport)
                            .testTag("open_report_shortcut")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Rose500.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BarChart,
                                    contentDescription = null,
                                    tint = Rose400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Laporan Pengeluaran 6 Bulan",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "Lihat grafik tren pengeluaran & perbandingan bulanan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Buka laporan",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 3. History Header + Filter Pills
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat Transaksi",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "${filteredTransactions.size} catatan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChipPill(
                            label = "Semua",
                            selected = selectedFilter == null,
                            onClick = { onSelectFilter(null) }
                        )
                        TransactionType.entries.forEach { type ->
                            FilterChipPill(
                                label = type.labelId,
                                selected = selectedFilter == type,
                                onClick = { onSelectFilter(type) }
                            )
                        }
                    }
                }
            }

            // 4. Transactions List / Loading / Error / Empty State
            when (transactionsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Emerald500)
                        }
                    }
                }
                is UiState.Error -> {
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
                }
                is UiState.Success -> {
                    if (filteredTransactions.isEmpty()) {
                        item {
                            EmptyTransactionsCard(
                                hasAnyTransactions = transactionsState.data.isNotEmpty(),
                                onLoadSampleData = onLoadSampleData
                            )
                        }
                    } else {
                        items(
                            items = filteredTransactions,
                            key = { it.id }
                        ) { tx ->
                            TransactionItemCard(
                                transaction = tx,
                                isPrivacyHidden = isPrivacyHidden,
                                onDelete = { onDeleteTransaction(tx.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroBalanceCard(
    metrics: DashboardMetrics,
    isPrivacyHidden: Boolean,
    onTogglePrivacy: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Slate850,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate700.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Slate800, Slate850, Slate900)
                    )
                )
        ) {
            // Decorative ambient glows
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 32.dp, y = (-32).dp)
                    .blur(40.dp)
                    .background(Emerald500.copy(alpha = 0.14f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-32).dp, y = 32.dp)
                    .blur(40.dp)
                    .background(Amber500.copy(alpha = 0.12f), CircleShape)
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL SALDO AKTIF",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate400
                    )
                    IconButton(
                        onClick = onTogglePrivacy,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("privacy_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isPrivacyHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (isPrivacyHidden) "Tampilkan nominal" else "Sembunyikan nominal",
                            tint = if (isPrivacyHidden) Amber400 else Slate400
                        )
                    }
                }

                Text(
                    text = CurrencyUtils.formatIdr(metrics.totalBalance, isPrivacyHidden),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VerifiedUser,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Termasuk kas tersedia & akumulasi tabungan",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Slate700.copy(alpha = 0.55f))
                )
                Spacer(modifier = Modifier.height(14.dp))

                // 3-Pillar Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PillarSummaryTile(
                        title = "Pemasukan",
                        amount = metrics.totalIncome,
                        accent = Emerald400,
                        icon = Icons.Rounded.ArrowDownward,
                        isPrivacyHidden = isPrivacyHidden,
                        modifier = Modifier.weight(1f)
                    )
                    PillarSummaryTile(
                        title = "Pengeluaran",
                        amount = metrics.totalExpense,
                        accent = Rose400,
                        icon = Icons.Rounded.ArrowUpward,
                        isPrivacyHidden = isPrivacyHidden,
                        modifier = Modifier.weight(1f)
                    )
                    PillarSummaryTile(
                        title = "Tabungan",
                        amount = metrics.totalSavings,
                        accent = Amber400,
                        icon = Icons.Rounded.Savings,
                        isPrivacyHidden = isPrivacyHidden,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PillarSummaryTile(
    title: String,
    amount: Long,
    accent: Color,
    icon: ImageVector,
    isPrivacyHidden: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Slate900.copy(alpha = 0.72f))
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = CurrencyUtils.formatIdr(amount, isPrivacyHidden),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Color.White else Slate850)
            .then(
                if (!selected) Modifier.border(1.dp, Slate800, CircleShape) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) Slate900 else Slate300
        )
    }
}

@Composable
private fun TransactionItemCard(
    transaction: TransactionModel,
    isPrivacyHidden: Boolean,
    onDelete: () -> Unit,
) {
    val type = TransactionType.fromKey(transaction.type)
    val amountColor = when (type) {
        TransactionType.INCOME -> Emerald400
        TransactionType.EXPENSE -> Rose400
        TransactionType.SAVINGS -> Amber400
    }
    val prefix = when (type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.SAVINGS -> "★ "
    }
    val catColor = CategoryCatalog.parseColor(transaction.categoryColorHex, amountColor)
    val icon = CategoryCatalog.resolveIcon(transaction.categoryIcon)

    Surface(
        color = Slate850,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(catColor.copy(alpha = 0.16f))
                    .border(1.dp, catColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = transaction.categoryName,
                    tint = catColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.categoryName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val formattedDate = DateUtils.formatShortIndonesianDate(transaction.date)
                val subtitle = if (transaction.note.isNotBlank()) {
                    "${transaction.note} • $formattedDate"
                } else {
                    formattedDate
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isPrivacyHidden) "Rp •••••" else "$prefix${CurrencyUtils.formatIdr(transaction.amount)}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = amountColor
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Hapus transaksi",
                        tint = Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTransactionsCard(
    hasAnyTransactions: Boolean,
    onLoadSampleData: () -> Unit,
) {
    Surface(
        color = Slate850.copy(alpha = 0.6f),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(22.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = Slate500,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Belum ada catatan transaksi",
                style = MaterialTheme.typography.titleSmall,
                color = Slate300
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (hasAnyTransactions) {
                    "Tidak ada transaksi pada filter kategori ini."
                } else {
                    "Tekan tombol + Catat Transaksi di bawah atau muat data contoh untuk mulai."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
            if (!hasAnyTransactions) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onLoadSampleData,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate800,
                        contentColor = Emerald400
                    ),
                    modifier = Modifier.testTag("load_sample_button")
                ) {
                    Text(
                        text = "Muat Data Contoh",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
