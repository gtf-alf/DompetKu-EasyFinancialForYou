package com.example.data

import android.content.Context
import com.example.R
import com.example.model.CustomCategoryModel
import com.example.model.RecurrenceFrequency
import com.example.model.RecurringTransactionModel
import com.example.model.TransactionModel
import com.example.model.TransactionType
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.example.util.DateUtils

class DompetKuRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google sebelum mengakses Firestore.")
    }

    private fun transactionsRef(uid: String) =
        db.collection("users").document(uid).collection("transactions")

    private fun categoriesRef(uid: String) =
        db.collection("users").document(uid).collection("categories")

    private fun recurringRef(uid: String) =
        db.collection("users").document(uid).collection("recurringTransactions")

    // =========================================================================
    // TRANSACTIONS
    // =========================================================================

    fun observeTransactions(userId: String = requireUserId()): Flow<List<TransactionModel>> = flow {
        val path = "users/$userId/transactions"
        emitAll(
            transactionsRef(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TransactionModel::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareByDescending<TransactionModel> { it.date }
                            .thenByDescending { it.createdAt?.seconds ?: 0L }
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addTransaction(
        type: TransactionType,
        amount: Long,
        categoryName: String,
        categoryIcon: String,
        categoryColorHex: String,
        note: String,
        date: String,
    ): String {
        val uid = requireUserId()
        val docRef = transactionsRef(uid).document()
        val payload = mapOf(
            "userId" to uid,
            "type" to type.key,
            "amount" to amount,
            "categoryName" to categoryName.trim().take(60),
            "categoryIcon" to categoryIcon,
            "categoryColorHex" to categoryColorHex,
            "note" to note.trim().take(200),
            "date" to date,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteTransaction(transactionId: String) {
        val uid = requireUserId()
        require(transactionId.isNotBlank()) { "Transaction ID must not be blank" }
        val docRef = transactionsRef(uid).document(transactionId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun clearAllTransactions() {
        val uid = requireUserId()
        val path = "users/$uid/transactions"
        try {
            val docs = transactionsRef(uid).get().await()
            val batch = db.batch()
            docs.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    suspend fun seedSampleTransactions() {
        val uid = requireUserId()
        val samples = listOf(
            mapOf(
                "userId" to uid,
                "type" to TransactionType.INCOME.key,
                "amount" to 8500000L,
                "categoryName" to "Gaji Bulanan",
                "categoryIcon" to "work",
                "categoryColorHex" to "#10B981",
                "note" to "Gaji bulan berjalan",
                "date" to DateUtils.todayIso(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
            mapOf(
                "userId" to uid,
                "type" to TransactionType.SAVINGS.key,
                "amount" to 2000000L,
                "categoryName" to "Dana Darurat",
                "categoryIcon" to "shield",
                "categoryColorHex" to "#F59E0B",
                "note" to "Alokasi rutin 20% gaji",
                "date" to DateUtils.todayIso(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
            mapOf(
                "userId" to uid,
                "type" to TransactionType.EXPENSE.key,
                "amount" to 1250000L,
                "categoryName" to "Tagihan & Utilitas",
                "categoryIcon" to "bolt",
                "categoryColorHex" to "#EC4899",
                "note" to "Listrik, internet & air",
                "date" to DateUtils.daysAgoIso(1),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
            mapOf(
                "userId" to uid,
                "type" to TransactionType.EXPENSE.key,
                "amount" to 350000L,
                "categoryName" to "Makanan & Minuman",
                "categoryIcon" to "fork_knife",
                "categoryColorHex" to "#F43F5E",
                "note" to "Belanja bahan masakan mingguan",
                "date" to DateUtils.daysAgoIso(2),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
            mapOf(
                "userId" to uid,
                "type" to TransactionType.SAVINGS.key,
                "amount" to 750000L,
                "categoryName" to "Investasi Reksadana/Saham",
                "categoryIcon" to "trend_up",
                "categoryColorHex" to "#14B8A6",
                "note" to "Top up reksadana pasar uang",
                "date" to DateUtils.daysAgoIso(3),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        )

        val path = "users/$uid/transactions"
        try {
            val batch = db.batch()
            samples.forEach { payload ->
                val ref = transactionsRef(uid).document()
                batch.set(ref, payload)
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    // =========================================================================
    // CUSTOM CATEGORIES
    // =========================================================================

    fun observeCustomCategories(userId: String = requireUserId()): Flow<List<CustomCategoryModel>> = flow {
        val path = "users/$userId/categories"
        emitAll(
            categoriesRef(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CustomCategoryModel::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.name.lowercase() }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addCustomCategory(
        name: String,
        type: TransactionType,
        iconKey: String,
        colorHex: String,
    ): String {
        val uid = requireUserId()
        val docRef = categoriesRef(uid).document()
        val payload = mapOf(
            "userId" to uid,
            "name" to name.trim().take(50),
            "type" to type.key,
            "iconKey" to iconKey,
            "colorHex" to colorHex,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateCustomCategory(category: CustomCategoryModel) {
        val uid = requireUserId()
        require(category.id.isNotBlank()) { "Category ID must not be blank" }
        requireNotNull(category.createdAt) { "Category createdAt must not be null for update" }
        val docRef = categoriesRef(uid).document(category.id)
        try {
            docRef.set(
                category.copy(
                    userId = uid,
                    name = category.name.trim().take(50),
                    updatedAt = null
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCustomCategory(categoryId: String) {
        val uid = requireUserId()
        require(categoryId.isNotBlank()) { "Category ID must not be blank" }
        val docRef = categoriesRef(uid).document(categoryId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // =========================================================================
    // RECURRING TRANSACTIONS & NOTIFICATION REMINDERS
    // =========================================================================

    fun observeRecurringTransactions(userId: String = requireUserId()): Flow<List<RecurringTransactionModel>> = flow {
        val path = "users/$userId/recurringTransactions"
        emitAll(
            recurringRef(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        RecurringTransactionModel::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.nextDueDate }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addRecurringTransaction(
        title: String,
        type: TransactionType,
        amount: Long,
        categoryName: String,
        categoryIcon: String,
        categoryColorHex: String,
        frequency: RecurrenceFrequency,
        nextDueDate: String,
        reminderDaysBefore: Long,
        reminderHour: Long,
        reminderMinute: Long,
        isEnabled: Boolean,
        note: String,
    ): String {
        val uid = requireUserId()
        val docRef = recurringRef(uid).document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(80),
            "type" to type.key,
            "amount" to amount,
            "categoryName" to categoryName.trim().take(60),
            "categoryIcon" to categoryIcon,
            "categoryColorHex" to categoryColorHex,
            "frequency" to frequency.key,
            "nextDueDate" to nextDueDate,
            "reminderDaysBefore" to reminderDaysBefore.coerceIn(0L, 14L),
            "reminderHour" to reminderHour.coerceIn(0L, 23L),
            "reminderMinute" to reminderMinute.coerceIn(0L, 59L),
            "enabled" to isEnabled,
            "note" to note.trim().take(200),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateRecurringTransaction(recurring: RecurringTransactionModel) {
        val uid = requireUserId()
        require(recurring.id.isNotBlank()) { "Recurring ID must not be blank" }
        requireNotNull(recurring.createdAt) { "Recurring createdAt must not be null for update" }
        val docRef = recurringRef(uid).document(recurring.id)
        try {
            docRef.set(
                recurring.copy(
                    userId = uid,
                    title = recurring.title.trim().take(80),
                    note = recurring.note.trim().take(200),
                    reminderDaysBefore = recurring.reminderDaysBefore.coerceIn(0L, 14L),
                    reminderHour = recurring.reminderHour.coerceIn(0L, 23L),
                    reminderMinute = recurring.reminderMinute.coerceIn(0L, 59L),
                    updatedAt = null
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteRecurringTransaction(recurringId: String) {
        val uid = requireUserId()
        require(recurringId.isNotBlank()) { "Recurring ID must not be blank" }
        val docRef = recurringRef(uid).document(recurringId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun recordAndAdvanceRecurring(recurring: RecurringTransactionModel) {
        addTransaction(
            type = TransactionType.fromKey(recurring.type),
            amount = recurring.amount,
            categoryName = recurring.categoryName,
            categoryIcon = recurring.categoryIcon,
            categoryColorHex = recurring.categoryColorHex,
            note = if (recurring.note.isNotBlank()) "${recurring.title} (${recurring.note})" else recurring.title,
            date = recurring.nextDueDate
        )
        val nextDue = when (RecurrenceFrequency.fromKey(recurring.frequency)) {
            RecurrenceFrequency.MONTHLY -> DateUtils.advanceIsoDate(recurring.nextDueDate, addMonths = 1)
            RecurrenceFrequency.WEEKLY -> DateUtils.advanceIsoDate(recurring.nextDueDate, addWeeks = 1)
        }
        if (recurring.createdAt != null) {
            updateRecurringTransaction(
                recurring.copy(nextDueDate = nextDue)
            )
        }
    }
}
