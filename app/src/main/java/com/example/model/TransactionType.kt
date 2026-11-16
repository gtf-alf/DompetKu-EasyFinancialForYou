package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransactionType(val key: String, val labelId: String) {
    EXPENSE("expense", "Pengeluaran"),
    INCOME("income", "Pemasukan"),
    SAVINGS("savings", "Tabungan");

    companion object {
        fun fromKey(key: String): TransactionType =
            entries.firstOrNull { it.key == key } ?: EXPENSE
    }
}

enum class RecurrenceFrequency(val key: String, val labelId: String) {
    MONTHLY("monthly", "Bulanan"),
    WEEKLY("weekly", "Mingguan");

    companion object {
        fun fromKey(key: String): RecurrenceFrequency =
            entries.firstOrNull { it.key == key } ?: MONTHLY
    }
}

data class CategoryOption(
    val id: String,
    val name: String,
    val type: TransactionType,
    val iconKey: String,
    val colorHex: String,
    val isCustom: Boolean
)

data class CategoryIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

data class CategoryColorOption(
    val hex: String,
    val label: String
)

object CategoryCatalog {
    val iconOptions: List<CategoryIconOption> = listOf(
        CategoryIconOption("work", "Kerja", Icons.Rounded.Work),
        CategoryIconOption("gift", "Bonus", Icons.Rounded.CardGiftcard),
        CategoryIconOption("laptop", "Bisnis", Icons.Rounded.LaptopMac),
        CategoryIconOption("trend_up", "Investasi", Icons.AutoMirrored.Rounded.TrendingUp),
        CategoryIconOption("coins", "Uang", Icons.Rounded.MonetizationOn),
        CategoryIconOption("fork_knife", "Makan", Icons.Rounded.Restaurant),
        CategoryIconOption("coffee", "Kopi", Icons.Rounded.LocalCafe),
        CategoryIconOption("car", "Transport", Icons.Rounded.DirectionsCar),
        CategoryIconOption("bolt", "Listrik", Icons.Rounded.Bolt),
        CategoryIconOption("shopping_bag", "Belanja", Icons.Rounded.ShoppingBag),
        CategoryIconOption("movie", "Hiburan", Icons.Rounded.Movie),
        CategoryIconOption("heartbeat", "Kesehatan", Icons.Rounded.LocalHospital),
        CategoryIconOption("receipt", "Tagihan", Icons.AutoMirrored.Rounded.ReceiptLong),
        CategoryIconOption("shield", "Darurat", Icons.Rounded.Security),
        CategoryIconOption("piggy_bank", "Tabungan", Icons.Rounded.Savings),
        CategoryIconOption("airplane", "Liburan", Icons.Rounded.AirplanemodeActive),
        CategoryIconOption("home", "Rumah", Icons.Rounded.Home),
        CategoryIconOption("school", "Edukasi", Icons.Rounded.School),
        CategoryIconOption("fitness", "Olahraga", Icons.Rounded.FitnessCenter),
        CategoryIconOption("pets", "Peliharaan", Icons.Rounded.Pets),
        CategoryIconOption("favorite", "Keluarga", Icons.Rounded.Favorite),
        CategoryIconOption("build", "Servis", Icons.Rounded.Build),
        CategoryIconOption("wallet", "Dompet", Icons.Rounded.AccountBalanceWallet),
        CategoryIconOption("star", "Lainnya", Icons.Rounded.Star),
    )

    val colorOptions: List<CategoryColorOption> = listOf(
        CategoryColorOption("#10B981", "Emerald"),
        CategoryColorOption("#F43F5E", "Rose"),
        CategoryColorOption("#F59E0B", "Amber"),
        CategoryColorOption("#38BDF8", "Sky"),
        CategoryColorOption("#6366F1", "Indigo"),
        CategoryColorOption("#A855F7", "Violet"),
        CategoryColorOption("#EC4899", "Pink"),
        CategoryColorOption("#14B8A6", "Teal"),
        CategoryColorOption("#F97316", "Orange"),
        CategoryColorOption("#84CC16", "Lime"),
        CategoryColorOption("#06B6D4", "Cyan"),
        CategoryColorOption("#94A3B8", "Slate"),
    )

    val defaultCategories: List<CategoryOption> = listOf(
        // Income
        CategoryOption("def_inc_1", "Gaji Bulanan", TransactionType.INCOME, "work", "#10B981", false),
        CategoryOption("def_inc_2", "Bonus & THR", TransactionType.INCOME, "gift", "#14B8A6", false),
        CategoryOption("def_inc_3", "Freelance / Bisnis", TransactionType.INCOME, "laptop", "#38BDF8", false),
        CategoryOption("def_inc_4", "Dividen & Imbal Hasil", TransactionType.INCOME, "trend_up", "#84CC16", false),
        CategoryOption("def_inc_5", "Pemasukan Lainnya", TransactionType.INCOME, "coins", "#10B981", false),
        // Expense
        CategoryOption("def_exp_1", "Makanan & Minuman", TransactionType.EXPENSE, "fork_knife", "#F43F5E", false),
        CategoryOption("def_exp_2", "Transportasi & BBM", TransactionType.EXPENSE, "car", "#F97316", false),
        CategoryOption("def_exp_3", "Tagihan & Utilitas", TransactionType.EXPENSE, "bolt", "#EC4899", false),
        CategoryOption("def_exp_4", "Belanja Harian", TransactionType.EXPENSE, "shopping_bag", "#A855F7", false),
        CategoryOption("def_exp_5", "Hiburan & Gaya Hidup", TransactionType.EXPENSE, "movie", "#6366F1", false),
        CategoryOption("def_exp_6", "Kesehatan", TransactionType.EXPENSE, "heartbeat", "#F43F5E", false),
        CategoryOption("def_exp_7", "Pengeluaran Lainnya", TransactionType.EXPENSE, "receipt", "#94A3B8", false),
        // Savings
        CategoryOption("def_sav_1", "Dana Darurat", TransactionType.SAVINGS, "shield", "#F59E0B", false),
        CategoryOption("def_sav_2", "Tabungan Masa Depan", TransactionType.SAVINGS, "piggy_bank", "#F59E0B", false),
        CategoryOption("def_sav_3", "Investasi Reksadana/Saham", TransactionType.SAVINGS, "trend_up", "#14B8A6", false),
        CategoryOption("def_sav_4", "Emas & Logam Mulia", TransactionType.SAVINGS, "coins", "#F59E0B", false),
        CategoryOption("def_sav_5", "Target Liburan / Impian", TransactionType.SAVINGS, "airplane", "#38BDF8", false),
    )

    fun resolveIcon(iconKey: String): ImageVector =
        iconOptions.firstOrNull { it.key == iconKey }?.icon ?: Icons.Rounded.AccountBalanceWallet

    fun parseColor(hex: String, fallback: Color = Color(0xFF10B981)): Color {
        return try {
            val clean = hex.removePrefix("#")
            if (clean.length == 6) {
                Color(("FF$clean").toLong(16))
            } else {
                fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }
}
