package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.model.CategoryCatalog
import com.example.model.CategoryOption
import com.example.model.RecurrenceFrequency
import com.example.model.RecurringTransactionModel
import com.example.model.TransactionType
import com.example.notifications.ReminderScheduler
import com.example.ui.UiState
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
import com.example.ui.theme.Slate950
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import java.util.Locale

@Composable
fun RecurringScreen(
    recurringState: UiState<List<RecurringTransactionModel>>,
    categories: List<CategoryOption>,
    isPrivacyHidden: Boolean,
    onSaveRecurring: (
        editing: RecurringTransactionModel?,
        title: String,
        type: TransactionType,
        amount: Long,
        category: CategoryOption,
        frequency: RecurrenceFrequency,
        nextDueDate: String,
        reminderDaysBefore: Long,
        reminderHour: Long,
        reminderMinute: Long,
        isEnabled: Boolean,
        note: String,
    ) -> Unit,
    onToggleEnabled: (RecurringTransactionModel, Boolean) -> Unit,
    onDeleteRecurring: (String) -> Unit,
    onRecordNow: (RecurringTransactionModel) -> Unit,
    onShowToast: (String) -> Unit,
) {
    val context = LocalContext.current
    var showEditorSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<RecurringTransactionModel?>(null) }

    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
        if (granted) {
            onShowToast("Izin notifikasi diaktifkan!")
        } else {
            onShowToast("Izin notifikasi ditolak. Aktifkan di Pengaturan perangkat.")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pengingat Berulang",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Notifikasi H-1, H-3, atau sesuai jadwal tagihan & gaji",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                    Button(
                        onClick = {
                            if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            editingItem = null
                            showEditorSheet = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Slate950
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("add_recurring_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "Tambah",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Notification permission banner on Android 13+
            if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    Surface(
                        color = Amber500.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Amber500.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = null,
                                tint = Amber400,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Izinkan Notifikasi Pengingat",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "Agar DompetKu dapat mengingatkan tagihan bulanan & gaji Anda.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate300
                                )
                            }
                            TextButton(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            ) {
                                Text("Izinkan", color = Amber400, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Test notification quick action
            item {
                OutlinedButton(
                    onClick = {
                        if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val firstActive = (recurringState as? UiState.Success)?.data?.firstOrNull { it.enabled }
                            ReminderScheduler.sendTestNotification(context, firstActive)
                            onShowToast("Notifikasi uji coba dikirim!")
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Uji Coba Kirim Notifikasi Sekarang",
                        style = MaterialTheme.typography.labelLarge,
                        color = Emerald400
                    )
                }
            }

            when (recurringState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Emerald500)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Text(
                            text = recurringState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Rose400
                        )
                    }
                }
                is UiState.Success -> {
                    val items = recurringState.data
                    if (items.isEmpty()) {
                        item {
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
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Alarm,
                                        contentDescription = null,
                                        tint = Slate500,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Belum ada pengingat berulang",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Slate300
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Atur pengingat tagihan listrik, cicilan, atau jadwal gaji rutin Anda dengan waktu H-1 hingga H-14.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }
                            }
                        }
                    } else {
                        items(items, key = { it.id }) { item ->
                            RecurringReminderCard(
                                item = item,
                                isPrivacyHidden = isPrivacyHidden,
                                onToggleEnabled = { enabled -> onToggleEnabled(item, enabled) },
                                onEdit = {
                                    editingItem = item
                                    showEditorSheet = true
                                },
                                onDelete = {
                                    ReminderScheduler.cancelReminder(context, item.id)
                                    onDeleteRecurring(item.id)
                                },
                                onRecordNow = { onRecordNow(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditorSheet) {
        RecurringEditorSheet(
            editingItem = editingItem,
            categories = categories,
            onDismiss = { showEditorSheet = false },
            onSave = { title, type, amount, category, frequency, nextDue, daysBefore, hour, minute, note ->
                onSaveRecurring(
                    editingItem,
                    title,
                    type,
                    amount,
                    category,
                    frequency,
                    nextDue,
                    daysBefore,
                    hour,
                    minute,
                    editingItem?.enabled ?: true,
                    note
                )
                showEditorSheet = false
            }
        )
    }
}

@Composable
private fun RecurringReminderCard(
    item: RecurringTransactionModel,
    isPrivacyHidden: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRecordNow: () -> Unit,
) {
    val type = TransactionType.fromKey(item.type)
    val accent = when (type) {
        TransactionType.INCOME -> Emerald400
        TransactionType.EXPENSE -> Rose400
        TransactionType.SAVINGS -> Amber400
    }
    val catColor = CategoryCatalog.parseColor(item.categoryColorHex, accent)
    val freqLabel = RecurrenceFrequency.fromKey(item.frequency).labelId
    val hLabel = if (item.reminderDaysBefore == 0L) "Hari-H" else "H-${item.reminderDaysBefore}"
    val timeFormatted = String.format(Locale.US, "%02d:%02d", item.reminderHour, item.reminderMinute)

    Surface(
        color = Slate850,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(22.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(catColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryCatalog.resolveIcon(item.categoryIcon),
                        contentDescription = item.categoryName,
                        tint = catColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.categoryName} • $freqLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Switch(
                    checked = item.enabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Slate950,
                        checkedTrackColor = Emerald500,
                        uncheckedThumbColor = Slate400,
                        uncheckedTrackColor = Slate800
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = CurrencyUtils.formatIdr(item.amount, isPrivacyHidden),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = accent
                    )
                    Text(
                        text = "Jatuh tempo: ${DateUtils.formatShortIndonesianDate(item.nextDueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Amber500.copy(alpha = 0.15f))
                        .border(1.dp, Amber500.copy(alpha = 0.35f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Notifikasi $hLabel • $timeFormatted",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Amber400
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onRecordNow,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Catat Sekarang",
                        style = MaterialTheme.typography.labelLarge,
                        color = Emerald400
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit pengingat",
                            tint = Slate300,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Hapus pengingat",
                            tint = Rose400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun RecurringEditorSheet(
    editingItem: RecurringTransactionModel?,
    categories: List<CategoryOption>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        type: TransactionType,
        amount: Long,
        category: CategoryOption,
        frequency: RecurrenceFrequency,
        nextDueDate: String,
        reminderDaysBefore: Long,
        reminderHour: Long,
        reminderMinute: Long,
        note: String,
    ) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(editingItem?.title ?: "") }
    var selectedType by remember {
        mutableStateOf(
            editingItem?.let { TransactionType.fromKey(it.type) } ?: TransactionType.EXPENSE
        )
    }
    var amountText by remember {
        mutableStateOf(
            editingItem?.let { CurrencyUtils.formatGrouping(it.amount.toString()) } ?: ""
        )
    }
    var frequency by remember {
        mutableStateOf(
            editingItem?.let { RecurrenceFrequency.fromKey(it.frequency) } ?: RecurrenceFrequency.MONTHLY
        )
    }
    var nextDueDate by remember {
        mutableStateOf(editingItem?.nextDueDate ?: DateUtils.daysAheadIso(7))
    }
    var reminderDaysBefore by remember {
        mutableIntStateOf(editingItem?.reminderDaysBefore?.toInt() ?: 1)
    }
    var reminderHour by remember { mutableIntStateOf(editingItem?.reminderHour?.toInt() ?: 9) }
    var reminderMinute by remember { mutableIntStateOf(editingItem?.reminderMinute?.toInt() ?: 0) }
    var note by remember { mutableStateOf(editingItem?.note ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filteredCategories = remember(categories, selectedType) {
        categories.filter { it.type == selectedType }
    }
    var selectedCategory by remember(filteredCategories) {
        mutableStateOf(
            filteredCategories.firstOrNull { it.name == editingItem?.categoryName }
                ?: filteredCategories.firstOrNull()
        )
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingItem == null) "Pengingat Berulang Baru" else "Edit Pengingat Berulang",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = Slate400)
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it.take(80)
                    errorMsg = null
                },
                label = { Text("Judul Pengingat") },
                placeholder = { Text("Contoh: Tagihan Listrik PLN / Gaji Bulanan") },
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
                    .testTag("recurring_title_input")
            )

            // Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    val accent = when (type) {
                        TransactionType.EXPENSE -> Rose500
                        TransactionType.INCOME -> Emerald500
                        TransactionType.SAVINGS -> Amber500
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) accent.copy(alpha = 0.2f) else Slate950)
                            .border(1.dp, if (isSelected) accent else Slate800, RoundedCornerShape(14.dp))
                            .clickable { selectedType = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.labelId,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isSelected) Color.White else Slate400
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { raw ->
                    amountText = CurrencyUtils.formatGrouping(raw)
                    errorMsg = null
                },
                label = { Text("Nominal (IDR)") },
                prefix = { Text("Rp ", color = Slate400, fontWeight = FontWeight.Bold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    .testTag("recurring_amount_input")
            )

            // Category chips
            Column {
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredCategories.forEach { cat ->
                        val isSelected = selectedCategory?.id == cat.id
                        val catColor = CategoryCatalog.parseColor(cat.colorHex)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) catColor.copy(alpha = 0.2f) else Slate950)
                                .border(1.dp, if (isSelected) catColor else Slate800, RoundedCornerShape(14.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = CategoryCatalog.resolveIcon(cat.iconKey),
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else Slate300
                            )
                        }
                    }
                }
            }

            // Frequency & Next Due Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Frekuensi",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate400,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RecurrenceFrequency.entries.forEach { freq ->
                            val isSelected = frequency == freq
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) Emerald500.copy(alpha = 0.2f) else Slate950)
                                    .border(
                                        1.dp,
                                        if (isSelected) Emerald500 else Slate800,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { frequency = freq },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = freq.labelId,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Emerald400 else Slate400
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tanggal Berikutnya",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate400,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate950)
                            .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = "Pilih tanggal jatuh tempo",
                            tint = Emerald400,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = DateUtils.formatShortIndonesianDate(nextDueDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }

            // Customizable Reminder Timing (H-0, H-1, H-3, H-7 or custom 0..14 days + Hour:Minute)
            Surface(
                color = Slate950,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate800, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Waktu Pengingat Notifikasi",
                        style = MaterialTheme.typography.titleSmall,
                        color = Amber400
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0, 1, 2, 3, 7).forEach { days ->
                            val isSelected = reminderDaysBefore == days
                            val label = if (days == 0) "Hari-H" else "H-$days"
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) Amber500 else Slate850)
                                    .clickable { reminderDaysBefore = days }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Slate950 else Slate300
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { if (reminderDaysBefore > 0) reminderDaysBefore-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Slate850)
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Kurangi hari", tint = Color.White)
                            }
                            Text(
                                text = if (reminderDaysBefore == 0) "Hari-H (0 hari)" else "$reminderDaysBefore hari sebelum",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            IconButton(
                                onClick = { if (reminderDaysBefore < 14) reminderDaysBefore++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Slate850)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Tambah hari", tint = Color.White)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate850)
                                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                .clickable { showTimePicker = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Alarm,
                                contentDescription = "Pilih jam",
                                tint = Amber400,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", reminderHour, reminderMinute),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(200) },
                label = { Text("Catatan (Opsional)") },
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
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMsg != null) {
                Text(
                    text = errorMsg ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Rose400
                )
            }

            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val amount = CurrencyUtils.parseDigits(amountText)
                    val category = selectedCategory
                    if (trimmedTitle.isEmpty()) {
                        errorMsg = "Judul pengingat tidak boleh kosong"
                        return@Button
                    }
                    if (amount <= 0L) {
                        errorMsg = "Masukkan nominal lebih dari Rp 0"
                        return@Button
                    }
                    if (category == null) {
                        errorMsg = "Pilih kategori"
                        return@Button
                    }
                    onSave(
                        trimmedTitle,
                        selectedType,
                        amount,
                        category,
                        frequency,
                        nextDueDate,
                        reminderDaysBefore.toLong(),
                        reminderHour.toLong(),
                        reminderMinute.toLong(),
                        note
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald500,
                    contentColor = Slate950
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_recurring_button")
            ) {
                Text(
                    text = if (editingItem == null) "Jadwalkan Pengingat" else "Perbarui Pengingat",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = remember(nextDueDate) { DateUtils.parseIsoToMillis(nextDueDate) }
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        nextDueDate = DateUtils.millisToIso(it)
                    }
                    showDatePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = Slate900,
            title = { Text("Pilih Jam Pengingat", color = Color.White) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    reminderHour = timePickerState.hour
                    reminderMinute = timePickerState.minute
                    showTimePicker = false
                }) {
                    Text("Simpan", color = Emerald400)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Batal", color = Slate400)
                }
            }
        )
    }
}
