package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class TransactionModel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = TransactionType.EXPENSE.key,
    val amount: Long = 0L,
    val categoryName: String = "",
    val categoryIcon: String = "receipt",
    val categoryColorHex: String = "#F43F5E",
    val note: String = "",
    val date: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class CustomCategoryModel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val type: String = TransactionType.EXPENSE.key,
    val iconKey: String = "star",
    val colorHex: String = "#10B981",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class RecurringTransactionModel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val type: String = TransactionType.EXPENSE.key,
    val amount: Long = 0L,
    val categoryName: String = "",
    val categoryIcon: String = "bolt",
    val categoryColorHex: String = "#F43F5E",
    val frequency: String = RecurrenceFrequency.MONTHLY.key,
    val nextDueDate: String = "",
    val reminderDaysBefore: Long = 1L,
    val reminderHour: Long = 9L,
    val reminderMinute: Long = 0L,
    val enabled: Boolean = true,
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)
