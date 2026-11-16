package com.example.auth

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.UserPreferencesDataStore
import com.example.model.TransactionModel
import com.example.model.TransactionType
import com.example.ui.DashboardMetrics
import com.example.ui.UiState
import com.example.ui.screens.BiometricLockScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.DompetKuTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class BiometricLockTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun biometricLockEnabled_defaultsToTrue_andPersistsUpdates() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = UserPreferencesDataStore(context)

        assertTrue(prefs.isBiometricLockEnabled.first())
        prefs.setBiometricLockEnabled(false)
        assertFalse(prefs.isBiometricLockEnabled.first())
        prefs.setBiometricLockEnabled(true)
        assertTrue(prefs.isBiometricLockEnabled.first())
    }

    @Test
    fun sensitiveTransactions_areHiddenUntilAuthenticated() {
        var unlocked by mutableStateOf(false)
        val sampleTransactions = listOf(
            TransactionModel(
                id = "tx_secret_1",
                userId = "u1",
                type = TransactionType.INCOME.key,
                amount = 8500000L,
                categoryName = "Gaji Bulanan",
                categoryIcon = "work",
                categoryColorHex = "#10B981",
                note = "Transfer rahasia",
                date = "2026-11-16"
            )
        )
        val metrics = DashboardMetrics(
            totalBalance = 8500000L,
            totalIncome = 8500000L,
            totalCount = 1
        )

        composeRule.setContent {
            DompetKuTheme {
                if (!unlocked) {
                    BiometricLockScreen(
                        userEmail = "user@example.com",
                        onUnlocked = { unlocked = true },
                        onSuppressNextBackgroundLock = {},
                        onSignOut = {}
                    )
                } else {
                    DashboardScreen(
                        transactionsState = UiState.Success(sampleTransactions),
                        filteredTransactions = sampleTransactions,
                        metrics = metrics,
                        selectedFilter = null,
                        isPrivacyHidden = false,
                        onTogglePrivacy = {},
                        onSelectFilter = {},
                        onDeleteTransaction = {},
                        onLoadSampleData = {}
                    )
                }
            }
        }

        // Locked: lock screen and redacted balance shown; transaction details absent
        composeRule.onNodeWithTag("biometric_lock_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Rp ••••••••").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("credential_unlock_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("lock_screen_sign_out_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Gaji Bulanan").assertDoesNotExist()
        composeRule.onNodeWithText("Transfer rahasia • 16 Nov 2026").assertDoesNotExist()

        // Unlock: financial details become visible
        composeRule.runOnIdle { unlocked = true }
        composeRule.onNodeWithTag("biometric_lock_screen").assertDoesNotExist()
        composeRule.onNodeWithText("Gaji Bulanan").assertIsDisplayed()
    }
}
