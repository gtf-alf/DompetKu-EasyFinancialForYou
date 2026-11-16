package com.example.auth

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricAvailability {
    READY,
    NONE_ENROLLED,
    UNAVAILABLE
}

object BiometricAuthHelper {

    fun allowedAuthenticatorsForApi(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_WEAK
        }
    }

    fun checkAvailability(context: Context): BiometricAvailability {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(allowedAuthenticatorsForApi())) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.READY
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
            else -> BiometricAvailability.UNAVAILABLE
        }
    }

    fun statusDescription(availability: BiometricAvailability): String {
        return when (availability) {
            BiometricAvailability.READY -> "Sidik jari, pengenalan wajah, atau kunci perangkat siap digunakan"
            BiometricAvailability.NONE_ENROLLED -> "Belum ada sidik jari / wajah / PIN perangkat terdaftar"
            BiometricAvailability.UNAVAILABLE -> "Perangkat tidak mendukung biometrik; gunakan verifikasi Credential Manager"
        }
    }

    fun createEnrollIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                )
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Buka Kunci DompetKu",
        subtitle: String = "Gunakan sidik jari atau pengenalan wajah untuk melihat detail transaksi",
        onSucceeded: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {},
        onFallbackRequested: () -> Unit = {},
    ) {
        val availability = checkAvailability(activity)
        if (availability != BiometricAvailability.READY) {
            onError(statusDescription(availability))
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSucceeded()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> onCancelled()
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> onFallbackRequested()
                        else -> onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Biometrik tidak cocok. Silakan coba lagi.")
                }
            }
        )

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setConfirmationRequired(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        } else {
            builder
                .setAllowedAuthenticators(BIOMETRIC_WEAK)
                .setNegativeButtonText("Verifikasi Google")
        }

        prompt.authenticate(builder.build())
    }
}
