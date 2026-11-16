package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.RecurrenceFrequency
import com.example.model.TransactionType
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class DompetKuRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun transactionCrud_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = DompetKuRepository(firestore)

        val txId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addTransaction(
                type = TransactionType.EXPENSE,
                amount = 125000L,
                categoryName = "Makanan & Minuman",
                categoryIcon = "fork_knife",
                categoryColorHex = "#F43F5E",
                note = "Makan malam",
                date = "2026-11-16"
            )
        }
        assertTrue(txId.isNotBlank())

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeTransactions(uid).first { list -> list.any { it.id == txId } }
        }
        assertTrue(observed.any { it.id == txId && it.amount == 125000L })

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.deleteTransaction(txId)
        }
    }

    @Test
    fun customCategoryCrud_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = DompetKuRepository(firestore)

        val catId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addCustomCategory(
                name = "Kopi Spesial",
                type = TransactionType.EXPENSE,
                iconKey = "coffee",
                colorHex = "#F59E0B"
            )
        }
        val categories = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCustomCategories(uid).first { list -> list.any { it.id == catId } }
        }
        val created = categories.first { it.id == catId }
        assertEquals("Kopi Spesial", created.name)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.updateCustomCategory(created.copy(name = "Kopi Susu", colorHex = "#10B981"))
        }
        val updatedList = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCustomCategories(uid).first { list -> list.any { it.id == catId && it.name == "Kopi Susu" } }
        }
        assertTrue(updatedList.any { it.id == catId && it.colorHex == "#10B981" })

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.deleteCustomCategory(catId)
        }
    }

    @Test
    fun recurringTransactionCrud_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = DompetKuRepository(firestore)

        val recId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addRecurringTransaction(
                title = "Tagihan Internet",
                type = TransactionType.EXPENSE,
                amount = 350000L,
                categoryName = "Tagihan & Utilitas",
                categoryIcon = "bolt",
                categoryColorHex = "#EC4899",
                frequency = RecurrenceFrequency.MONTHLY,
                nextDueDate = "2026-11-25",
                reminderDaysBefore = 3L,
                reminderHour = 9L,
                reminderMinute = 0L,
                isEnabled = true,
                note = "Bayar sebelum jatuh tempo"
            )
        }
        val items = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeRecurringTransactions(uid).first { list -> list.any { it.id == recId } }
        }
        val created = items.first { it.id == recId }
        assertEquals(3L, created.reminderDaysBefore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.updateRecurringTransaction(created.copy(reminderDaysBefore = 1L, enabled = false))
        }
        val updatedItems = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeRecurringTransactions(uid).first { list ->
                list.any { it.id == recId && it.reminderDaysBefore == 1L && !it.enabled }
            }
        }
        assertTrue(updatedItems.any { it.id == recId && !it.enabled })

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.deleteRecurringTransaction(recId)
        }
    }

    @Test
    fun observeTransactions_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = DompetKuRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.addTransaction(
                type = TransactionType.INCOME,
                amount = 5000000L,
                categoryName = "Gaji Bulanan",
                categoryIcon = "work",
                categoryColorHex = "#10B981",
                note = "Gaji",
                date = "2026-11-16"
            )
        }

        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("transactions").get().await()
            }
            fail("Expected PERMISSION_DENIED for cross-user access")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    @Test
    fun observeTransactions_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repo = DompetKuRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeTransactions(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated user")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_dompetku@test.com"
        const val BOB_EMAIL = "bob_dompetku@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
