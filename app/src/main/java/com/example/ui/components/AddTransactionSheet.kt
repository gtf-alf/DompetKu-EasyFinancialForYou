package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.CategoryCatalog
import com.example.model.CategoryOption
import com.example.model.TransactionType
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    categories: List<CategoryOption>,
    onDismiss: () -> Unit,
    onSave: (TransactionType, Long, CategoryOption, String, String) -> Unit,
    onManageCategoriesClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var dateIso by remember { mutableStateOf(DateUtils.todayIso()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filteredCategories = remember(categories, selectedType) {
        categories.filter { it.type == selectedType }
    }
    var selectedCategory by remember(filteredCategories) {
        mutableStateOf(filteredCategories.firstOrNull())
    }

    LaunchedEffect(filteredCategories) {
        if (selectedCategory == null || filteredCategories.none { it.id == selectedCategory?.id }) {
            selectedCategory = filteredCategories.firstOrNull()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate900,
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tambah Transaksi Baru",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup",
                        tint = Slate400
                    )
                }
            }

            // Segmented Type Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Slate950)
                    .border(1.dp, Slate800, RoundedCornerShape(18.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TransactionType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    val activeColor = when (type) {
                        TransactionType.EXPENSE -> Rose500
                        TransactionType.INCOME -> Emerald500
                        TransactionType.SAVINGS -> Amber500
                    }
                    val activeText = when (type) {
                        TransactionType.EXPENSE -> Rose400
                        TransactionType.INCOME -> Emerald400
                        TransactionType.SAVINGS -> Amber400
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) activeColor.copy(alpha = 0.2f) else Color.Transparent)
                            .then(
                                if (isSelected) Modifier.border(
                                    1.dp,
                                    activeColor.copy(alpha = 0.45f),
                                    RoundedCornerShape(14.dp)
                                ) else Modifier
                            )
                            .clickable { selectedType = type }
                            .padding(vertical = 11.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (type) {
                                TransactionType.EXPENSE -> Icons.Rounded.ArrowUpward
                                TransactionType.INCOME -> Icons.Rounded.ArrowDownward
                                TransactionType.SAVINGS -> Icons.Rounded.Savings
                            },
                            contentDescription = null,
                            tint = if (isSelected) activeText else Slate400,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = type.labelId,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isSelected) activeText else Slate400
                        )
                    }
                }
            }

            // Nominal Rupiah Input with automatic grouping
            Column {
                Text(
                    text = "Nominal (IDR)",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        amountText = CurrencyUtils.formatGrouping(raw)
                        errorMsg = null
                    },
                    prefix = {
                        Text(
                            text = "Rp ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate400
                        )
                    },
                    placeholder = {
                        Text("0", color = Slate700)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate950,
                        unfocusedContainerColor = Slate950,
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_amount_input")
                )
            }

            // Category selector with custom icons & colors
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kategori",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate400
                    )
                    TextButton(onClick = {
                        onDismiss()
                        onManageCategoriesClick()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "Kelola Kategori",
                            style = MaterialTheme.typography.labelMedium,
                            color = Emerald400
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredCategories.forEach { cat ->
                        val isCatSelected = selectedCategory?.id == cat.id
                        val catColor = CategoryCatalog.parseColor(cat.colorHex)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isCatSelected) catColor.copy(alpha = 0.22f) else Slate950
                                )
                                .border(
                                    1.dp,
                                    if (isCatSelected) catColor else Slate800,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CategoryCatalog.resolveIcon(cat.iconKey),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isCatSelected) Color.White else Slate300
                            )
                        }
                    }
                }
            }

            // Date & Note
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(0.45f)) {
                    Text(
                        text = "Tanggal",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate400,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Slate950)
                            .border(1.dp, Slate700, RoundedCornerShape(18.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = "Pilih tanggal",
                            tint = Emerald400,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = DateUtils.formatShortIndonesianDate(dateIso),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                Column(modifier = Modifier.weight(0.55f)) {
                    Text(
                        text = "Catatan (Opsional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate400,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it.take(200) },
                        placeholder = { Text("Makan siang...", color = Slate700) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate950,
                            unfocusedContainerColor = Slate950,
                            focusedBorderColor = Emerald500,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_note_input")
                    )
                }
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Rose400
                )
            }

            val buttonColor = when (selectedType) {
                TransactionType.EXPENSE -> Rose500
                TransactionType.INCOME -> Emerald500
                TransactionType.SAVINGS -> Amber500
            }
            val buttonTextColor = if (selectedType == TransactionType.EXPENSE) Color.White else Slate950

            Button(
                onClick = {
                    val amount = CurrencyUtils.parseDigits(amountText)
                    val category = selectedCategory
                    if (amount <= 0L) {
                        errorMsg = "Masukkan nominal lebih dari Rp 0"
                        return@Button
                    }
                    if (category == null) {
                        errorMsg = "Pilih kategori transaksi"
                        return@Button
                    }
                    onSave(selectedType, amount, category, noteText, dateIso)
                    onDismiss()
                },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = buttonTextColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_transaction_button")
            ) {
                Text(
                    text = "Simpan Transaksi",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = remember(dateIso) { DateUtils.parseIsoToMillis(dateIso) }
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        dateIso = DateUtils.millisToIso(it)
                    }
                    showDatePicker = false
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
