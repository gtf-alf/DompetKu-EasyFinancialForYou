package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dompetku_prefs")

class UserPreferencesDataStore(private val context: Context) {
    private val privacyHiddenKey = booleanPreferencesKey("privacy_hidden")
    private val biometricLockEnabledKey = booleanPreferencesKey("biometric_lock_enabled")

    val isPrivacyHidden: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[privacyHiddenKey] ?: false
    }

    val isBiometricLockEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[biometricLockEnabledKey] ?: true
    }

    suspend fun setPrivacyHidden(hidden: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[privacyHiddenKey] = hidden
        }
    }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[biometricLockEnabledKey] = enabled
        }
    }
}
