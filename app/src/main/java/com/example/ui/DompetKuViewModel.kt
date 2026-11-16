package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DompetKuRepository
import com.example.data.UserPreferencesDataStore
import com.example.model.CategoryCatalog
import com.example.model.CategoryOption
import com.example.model.CustomCategoryModel
import com.example.model.RecurrenceFrequency
import com.example.model.RecurringTransactionModel
import com.example.model.TransactionModel
import com.example.model.TransactionType
import com.example.util.MonthlySpendingReport
import com.example.util.SpendingReportCalculator
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DashboardMetrics(
    val totalBalance: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val totalSavings: Long = 0L,
    val savingsRatePercent: Int = 0,
    val incomePercent: Int = 0,
    val expensePercent: Int = 0,
    val savingsPercent: Int = 0,
    val totalCount: Int = 0,
)

class DompetKuViewModel(
    private val repository: DompetKuRepository,
    private val preferences: UserPreferencesDataStore,
    private val currentUserId: String,
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<TransactionType?>(null)
    val selectedFilter: StateFlow<TransactionType?> = _selectedFilter.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    val isBiometricLockEnabled: StateFlow<Boolean> = preferences.isBiometricLockEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = true
        )

    val isAccessGranted: StateFlow<Boolean> = combine(
        preferences.isBiometricLockEnabled,
        _isVaultUnlocked
    ) { lockEnabled, unlocked ->
        !lockEnabled || unlocked
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = false
    )

    val isPrivacyHidden: StateFlow<Boolean> = preferences.isPrivacyHidden
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = false
        )

    val transactionsState: StateFlow<UiState<List<TransactionModel>>> =
        repository.observeTransactions(currentUserId)
            .map<List<TransactionModel>, UiState<List<TransactionModel>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing transactions", rootError)
                emit(UiState.Error(rootError.localizedMessage ?: "Gagal memuat transaksi"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val customCategoriesState: StateFlow<UiState<List<CustomCategoryModel>>> =
        repository.observeCustomCategories(currentUserId)
            .map<List<CustomCategoryModel>, UiState<List<CustomCategoryModel>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing categories", rootError)
                emit(UiState.Error(rootError.localizedMessage ?: "Gagal memuat kategori"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val recurringState: StateFlow<UiState<List<RecurringTransactionModel>>> =
        repository.observeRecurringTransactions(currentUserId)
            .map<List<RecurringTransactionModel>, UiState<List<RecurringTransactionModel>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing recurring transactions", rootError)
                emit(UiState.Error(rootError.localizedMessage ?: "Gagal memuat pengingat"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val allCategoryOptions: StateFlow<List<CategoryOption>> =
        customCategoriesState.map { state ->
            val customList = (state as? UiState.Success)?.data.orEmpty().map { cat ->
                CategoryOption(
                    id = cat.id,
                    name = cat.name,
                    type = TransactionType.fromKey(cat.type),
                    iconKey = cat.iconKey,
                    colorHex = cat.colorHex,
                    isCustom = true
                )
            }
            customList + CategoryCatalog.defaultCategories
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = CategoryCatalog.defaultCategories
        )

    val dashboardMetrics: StateFlow<DashboardMetrics> =
        transactionsState.map { state ->
            val list = (state as? UiState.Success)?.data.orEmpty()
            var income = 0L
            var expense = 0L
            var savings = 0L
            list.forEach { tx ->
                when (TransactionType.fromKey(tx.type)) {
                    TransactionType.INCOME -> income += tx.amount
                    TransactionType.EXPENSE -> expense += tx.amount
                    TransactionType.SAVINGS -> savings += tx.amount
                }
            }
            val balance = income - expense
            val savingsRate = if (income > 0L) ((savings * 100.0) / income).toInt() else 0
            val volume = income + expense + savings
            val pctIncome = if (volume > 0L) ((income * 100.0) / volume).toInt() else 0
            val pctExpense = if (volume > 0L) ((expense * 100.0) / volume).toInt() else 0
            val pctSavings = if (volume > 0L) ((savings * 100.0) / volume).toInt() else 0

            DashboardMetrics(
                totalBalance = balance,
                totalIncome = income,
                totalExpense = expense,
                totalSavings = savings,
                savingsRatePercent = savingsRate,
                incomePercent = pctIncome,
                expensePercent = pctExpense,
                savingsPercent = pctSavings,
                totalCount = list.size
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = DashboardMetrics()
        )

    val filteredTransactions: StateFlow<List<TransactionModel>> =
        combine(transactionsState, _selectedFilter) { state, filter ->
            val list = (state as? UiState.Success)?.data.orEmpty()
            if (filter == null) list else list.filter { it.type == filter.key }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    val monthlySpendingReport: StateFlow<MonthlySpendingReport> =
        transactionsState.map { state ->
            val list = (state as? UiState.Success)?.data.orEmpty()
            SpendingReportCalculator.calculateLastSixMonths(list)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SpendingReportCalculator.calculateLastSixMonths(emptyList())
        )

    fun setFilter(filter: TransactionType?) {
        _selectedFilter.value = filter
    }

    fun togglePrivacy() {
        viewModelScope.launch {
            preferences.setPrivacyHidden(!isPrivacyHidden.value)
        }
    }

    fun unlockVault() {
        _isVaultUnlocked.value = true
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setBiometricLockEnabled(enabled)
            if (enabled) {
                _isVaultUnlocked.value = true
                _toastMessage.tryEmit("Kunci biometrik diaktifkan")
            } else {
                _isVaultUnlocked.value = true
                _toastMessage.tryEmit("Kunci biometrik dinonaktifkan")
            }
        }
    }

    fun addTransaction(
        type: TransactionType,
        amount: Long,
        category: CategoryOption,
        note: String,
        date: String,
    ) {
        viewModelScope.launch {
            try {
                repository.addTransaction(
                    type = type,
                    amount = amount,
                    categoryName = category.name,
                    categoryIcon = category.iconKey,
                    categoryColorHex = category.colorHex,
                    note = note,
                    date = date
                )
                _toastMessage.tryEmit("Transaksi berhasil dicatat!")
            } catch (e: Exception) {
                _toastMessage.tryEmit(e.localizedMessage ?: "Gagal menyimpan transaksi")
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(id)
                _toastMessage.tryEmit("Transaksi dihapus")
            } catch (e: Exception) {
                _toastMessage.tryEmit(e.localizedMessage ?: "Gagal menghapus transaksi")
            }
        }
    }

    fun loadSampleTransactions() {
        viewModelScope.launch {
            try {
                repository.seedSampleTransactions()
                _toastMessage.tryEmit("Data contoh berhasil dimuat")
            } catch (e: Exception) {
                _toastMessage.tryEmit(e.localizedMessage ?: "Gagal memuat data contoh")
            }
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            try {
                repository.clearAllTransactions()
                _toastMessage.tryEmit("Semua riwayat transaksi dihapus")
            } catch (e: Exception) {
                _toastMessage.tryEmit(e.localizedMessage ?: "Gagal menghapus data")
            }
        }
    }

    // Custom Categories Actions
    fun saveCustomCategory(
        editingCategory: CustomCategoryModel?,
        name: String,
        type: TransactionType,
        iconKey: String,
        colorHex: String,
    ) {
        viewModelScope.launch {
            try {
                if (editingCategory == null) {
                    repository.addCustomCategory(name, type, iconKey, colorHex)
                    _toastMessage.tryEmit("Kategori \"$name\" ditambahkan")
                } else {
                    repository.updateCustomCategory(
                        editingCategory.copy(
                            name = name,
                            type = type.key,
                            iconKey = iconKey,
                            colorHex = colorHex
                        )
                    )
                    _toastMessage.tryEmit("Kategori \"$name\" diperbarui")
                }
            } catch (e: Exception) {
                _toastMessage.tryEmit(e.localizedMessage ?: "Gagal menyimpan kategori")
            }
        }
    }

    fun deleteCustomCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCustomCategory(categoryId)
                _toastMessage.tryEmit("Kategori kustom dihapus")
            } catch (e: Exception) {
                _toastMessage.tryEmit(e.localizedMessage ?: "Gagal menghapus kategori")
            }
        }
    }

    // Recurring Transactions & Reminders Actions
    fun saveRecurringTransaction(
        editingItem: RecurringTransactionModel?,
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
    ) {
        viewModelScope.launch {
            try {
                if (editingItem == null) {
                    repository.addRecurringTransaction(
                        title = title,
                        type = type,
                        amount = amount,
                        categoryName = category.name,
                        categoryIcon = category.iconKey,
                        categoryColorHex = category.colorHex,
                        frequency = frequency,
                        nextDueDate = nextDueDate,
                        reminderDaysBefore = reminderDaysBefore,
                        reminderHour = reminderHour,
                        reminderMinute = reminderMinute,
                        isEnabled = isEnabled,
                        note = note
                    )
                    _toastMessage.tryEmit("Pengingat \"$title\" dijadwalkan")
                } else {
                    repository.updateRecurringTransaction(
                        editingItem.copy(
                            title = title,
                            type = type.key,
                            amount = amount,
                            categoryName = category.name,
                            categoryIcon = category.iconKey,
                            categoryColorHex = category.colorHex,
                            frequency = frequency.key,
                            nextDueDate = nextDueDate,
                            reminderDaysBefore = reminderDaysBefore,
                            reminderHour = reminderHour,
                            reminderMinute = reminderMinute,
                            enabled = isEnabled,
                            note = note
                        )
                    )
                    _toastMessage.tryEmit("Pengingat \"$title\" diperbarui")
                }
            } catch (e: Exception) {
                _toastMessage.tryEmit(e.localizedMessage ?: "Gagal menyimpan pengingat")
            }
        }
    }

    fun toggleRecurringEnabled(item: RecurringTransactionModel, enabled: Boolean) {
        viewModelScope.launch {
            try {
                if (item.createdAt != null) {
                    repository.updateRecurringTransaction(item.copy(enabled = enabled))
                }
            } catch (e: Exception) {
                _toastMessage.tryEmit(e.localizedMessage ?: "Gagal mengubah status pengingat")
            }
        }
    }

    fun deleteRecurringTransaction(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteRecurringTransaction(id)
                _toastMessage.tryEmit("Pengingat berulang dihapus")
            } catch (e: Exception) {
                _toastMessage.tryEmit(e.localizedMessage ?: "Gagal menghapus pengingat")
            }
        }
    }

    fun recordRecurringNow(item: RecurringTransactionModel) {
        viewModelScope.launch {
            try {
                repository.recordAndAdvanceRecurring(item)
                _toastMessage.tryEmit("${item.title} dicatat & jadwal dimajukan")
            } catch (e: Exception) {
                _toastMessage.tryEmit(e.localizedMessage ?: "Gagal mencatat transaksi berulang")
            }
        }
    }

    private companion object {
        const val TAG = "DompetKuVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
