package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CategoryCatalog
import com.example.model.CategoryOption
import com.example.model.CustomCategoryModel
import com.example.model.TransactionType
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

@Composable
fun CategoriesScreen(
    customCategoriesState: UiState<List<CustomCategoryModel>>,
    onSaveCustomCategory: (CustomCategoryModel?, String, TransactionType, String, String) -> Unit,
    onDeleteCustomCategory: (String) -> Unit,
) {
    var filterType by remember { mutableStateOf<TransactionType?>(null) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CustomCategoryModel?>(null) }

    val customCategories = (customCategoriesState as? UiState.Success)?.data.orEmpty()
    val filteredCustom = remember(customCategories, filterType) {
        if (filterType == null) customCategories else customCategories.filter { it.type == filterType?.key }
    }
    val filteredDefaults = remember(filterType) {
        if (filterType == null) CategoryCatalog.defaultCategories
        else CategoryCatalog.defaultCategories.filter { it.type == filterType }
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
                            text = "Kategori Transaksi",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Buat kategori kustom dengan ikon & warna pilihan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                    Button(
                        onClick = {
                            editingCategory = null
                            showCategorySheet = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Slate950
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("add_category_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "Buat Baru",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Filter pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryFilterPill("Semua", filterType == null) { filterType = null }
                    TransactionType.entries.forEach { type ->
                        CategoryFilterPill(type.labelId, filterType == type) { filterType = type }
                    }
                }
            }

            // Custom categories section
            item {
                Text(
                    text = "Kategori Kustom (${filteredCustom.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = Emerald400,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            when (customCategoriesState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Emerald500)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Text(
                            text = customCategoriesState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Rose400
                        )
                    }
                }
                is UiState.Success -> {
                    if (filteredCustom.isEmpty()) {
                        item {
                            Surface(
                                color = Slate850.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Slate800, RoundedCornerShape(20.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Category,
                                        contentDescription = null,
                                        tint = Slate500,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Belum ada kategori kustom",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Slate300
                                    )
                                    Text(
                                        text = "Ketuk \"Buat Baru\" untuk menambahkan kategori pribadi Anda.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredCustom, key = { it.id }) { cat ->
                            CustomCategoryRow(
                                category = cat,
                                onEdit = {
                                    editingCategory = cat
                                    showCategorySheet = true
                                },
                                onDelete = { onDeleteCustomCategory(cat.id) }
                            )
                        }
                    }
                }
            }

            // Default categories section
            item {
                Text(
                    text = "Kategori Bawaan (${filteredDefaults.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = Slate400,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(filteredDefaults, key = { it.id }) { cat ->
                DefaultCategoryRow(cat)
            }
        }
    }

    if (showCategorySheet) {
        CategoryEditorSheet(
            editingCategory = editingCategory,
            onDismiss = { showCategorySheet = false },
            onSave = { name, type, iconKey, colorHex ->
                onSaveCustomCategory(editingCategory, name, type, iconKey, colorHex)
                showCategorySheet = false
            }
        )
    }
}

@Composable
private fun CategoryFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Color.White else Slate850)
            .then(if (!selected) Modifier.border(1.dp, Slate800, CircleShape) else Modifier)
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
private fun CustomCategoryRow(
    category: CustomCategoryModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val color = CategoryCatalog.parseColor(category.colorHex)
    val type = TransactionType.fromKey(category.type)
    val typeAccent = when (type) {
        TransactionType.INCOME -> Emerald400
        TransactionType.EXPENSE -> Rose400
        TransactionType.SAVINGS -> Amber400
    }

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
                    .background(color.copy(alpha = 0.18f))
                    .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CategoryCatalog.resolveIcon(category.iconKey),
                    contentDescription = category.name,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${type.labelId} • Kustom",
                    style = MaterialTheme.typography.labelSmall,
                    color = typeAccent
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(42.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit kategori",
                        tint = Slate300,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(42.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Hapus kategori",
                        tint = Rose400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultCategoryRow(cat: CategoryOption) {
    val color = CategoryCatalog.parseColor(cat.colorHex)
    Surface(
        color = Slate850.copy(alpha = 0.65f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CategoryCatalog.resolveIcon(cat.iconKey),
                    contentDescription = cat.name,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cat.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Slate300
                )
                Text(
                    text = cat.type.labelId,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Slate800)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Bawaan",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditorSheet(
    editingCategory: CustomCategoryModel?,
    onDismiss: () -> Unit,
    onSave: (String, TransactionType, String, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(editingCategory?.name ?: "") }
    var selectedType by remember {
        mutableStateOf(
            editingCategory?.let { TransactionType.fromKey(it.type) } ?: TransactionType.EXPENSE
        )
    }
    var selectedIconKey by remember { mutableStateOf(editingCategory?.iconKey ?: "coffee") }
    var selectedColorHex by remember { mutableStateOf(editingCategory?.colorHex ?: "#10B981") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val previewColor = CategoryCatalog.parseColor(selectedColorHex)

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
                    text = if (editingCategory == null) "Kategori Kustom Baru" else "Edit Kategori Kustom",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = Slate400)
                }
            }

            // Live Preview Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Slate950)
                    .border(1.dp, Slate800, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(previewColor.copy(alpha = 0.2f))
                        .border(1.dp, previewColor, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryCatalog.resolveIcon(selectedIconKey),
                        contentDescription = null,
                        tint = previewColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = name.ifBlank { "Pratinjau Kategori" },
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Text(
                        text = "${selectedType.labelId} • Warna $selectedColorHex",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            }

            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.take(50)
                    errorMsg = null
                },
                label = { Text("Nama Kategori") },
                placeholder = { Text("Contoh: Kopi & Camilan") },
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
                    .testTag("category_name_input")
            )

            // Type Selector
            Column {
                Text(
                    text = "Jenis Arus Kas",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
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
                                .border(
                                    1.dp,
                                    if (isSelected) accent else Slate800,
                                    RoundedCornerShape(14.dp)
                                )
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
            }

            // Color Picker
            Column {
                Text(
                    text = "Pilih Warna",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryCatalog.colorOptions.forEach { option ->
                        val color = CategoryCatalog.parseColor(option.hex)
                        val isSelected = selectedColorHex.equals(option.hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColorHex = option.hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = option.label,
                                    tint = Slate950,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Icon Picker
            Column {
                Text(
                    text = "Pilih Ikon",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryCatalog.iconOptions.forEach { option ->
                        val isSelected = selectedIconKey == option.key
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) previewColor.copy(alpha = 0.22f) else Slate950
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) previewColor else Slate800,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedIconKey = option.key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.label,
                                tint = if (isSelected) previewColor else Slate400,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Rose400
                )
            }

            Button(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) {
                        errorMsg = "Nama kategori tidak boleh kosong"
                        return@Button
                    }
                    onSave(trimmed, selectedType, selectedIconKey, selectedColorHex)
                },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald500,
                    contentColor = Slate950
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_category_button")
            ) {
                Text(
                    text = if (editingCategory == null) "Simpan Kategori" else "Perbarui Kategori",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
