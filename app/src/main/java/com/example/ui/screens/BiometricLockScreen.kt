package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.auth.BiometricAuthHelper
import com.example.auth.BiometricAvailability
import com.example.auth.GoogleAuthHelper
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
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.Teal500

@Composable
fun BiometricLockScreen(
    userEmail: String,
    onUnlocked: () -> Unit,
    onSuppressNextBackgroundLock: () -> Unit,
    onSignOut: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var availability by remember { mutableStateOf(BiometricAuthHelper.checkAvailability(context)) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var autoPrompted by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                availability = BiometricAuthHelper.checkAvailability(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun launchCredentialManagerUnlock() {
        errorMessage = null
        isAuthenticating = true
        onSuppressNextBackgroundLock()
        GoogleAuthHelper.reauthenticateWithCredentialManager(
            context = context,
            credentialManager = credentialManager,
            onSuccess = {
                isAuthenticating = false
                onUnlocked()
            },
            onError = { msg ->
                isAuthenticating = false
                errorMessage = msg
            },
            scope = scope,
            onCancelled = {
                isAuthenticating = false
            }
        )
    }

    fun launchBiometricUnlock() {
        availability = BiometricAuthHelper.checkAvailability(context)
        if (activity == null) {
            errorMessage = "Activity tidak mendukung autentikasi biometrik."
            return
        }
        if (availability != BiometricAvailability.READY) {
            errorMessage = BiometricAuthHelper.statusDescription(availability)
            return
        }
        errorMessage = null
        isAuthenticating = true
        onSuppressNextBackgroundLock()
        BiometricAuthHelper.authenticate(
            activity = activity,
            title = "Buka Kunci DompetKu",
            subtitle = "Gunakan sidik jari, pengenalan wajah, atau kunci layar",
            onSucceeded = {
                isAuthenticating = false
                onUnlocked()
            },
            onError = { msg ->
                isAuthenticating = false
                errorMessage = msg
            },
            onCancelled = {
                isAuthenticating = false
            },
            onFallbackRequested = {
                isAuthenticating = false
                launchCredentialManagerUnlock()
            }
        )
    }

    LaunchedEffect(Unit) {
        if (!autoPrompted && availability == BiometricAvailability.READY) {
            autoPrompted = true
            launchBiometricUnlock()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("biometric_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Brand Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(Emerald500, Teal500))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = "DompetKu",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "DompetKu",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Slate900)
                        .border(1.dp, Slate800, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Terkunci",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Emerald400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Center Lock Card
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Emerald500.copy(alpha = 0.25f), Slate900)
                            )
                        )
                        .border(1.5.dp, Emerald500.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fingerprint,
                        contentDescription = "Autentikasi Biometrik",
                        tint = Emerald400,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Data Keuangan Dilindungi",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Saldo, grafik, dan detail transaksi Anda disembunyikan. Autentikasi dengan sidik jari, pengenalan wajah, atau Credential Manager untuk membuka akses.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400,
                    textAlign = TextAlign.Center
                )

                if (userEmail.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Akun aktif: $userEmail",
                        style = MaterialTheme.typography.labelMedium,
                        color = Emerald400
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Redacted Sensitive Data Preview Card
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate800, RoundedCornerShape(22.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL SALDO AKTIF",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                            Icon(
                                imageVector = Icons.Rounded.VisibilityOff,
                                contentDescription = "Tersembunyi",
                                tint = Amber400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Rp ••••••••",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Slate800)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Detail riwayat transaksi & nominal disamarkan hingga terverifikasi",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate300
                            )
                        }
                    }
                }

                if (availability == BiometricAvailability.NONE_ENROLLED) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = Amber500.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Amber500.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Biometrik Belum Terdaftar",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Amber400
                                )
                                Text(
                                    text = "Daftarkan sidik jari / wajah di pengaturan perangkat atau gunakan Credential Manager di bawah.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate300
                                )
                            }
                            TextButton(
                                onClick = {
                                    onSuppressNextBackgroundLock()
                                    context.startActivity(BiometricAuthHelper.createEnrollIntent())
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "Buka pengaturan biometrik",
                                    tint = Amber400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = Rose500.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Rose500.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Rose400,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                if (availability == BiometricAvailability.READY) {
                    Button(
                        onClick = { launchBiometricUnlock() },
                        enabled = !isAuthenticating,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Slate950,
                            disabledContainerColor = Slate850,
                            disabledContentColor = Slate400
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("biometric_unlock_button")
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Slate950,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.size(10.dp))
                            Text(
                                text = "Memverifikasi...",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Buka dengan Sidik Jari / Wajah",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedButton(
                    onClick = { launchCredentialManagerUnlock() },
                    enabled = !isAuthenticating,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(1.dp, Slate700, RoundedCornerShape(18.dp))
                        .testTag("credential_unlock_button")
                ) {
                    Text(
                        text = "Verifikasi via Credential Manager (Google)",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onSignOut,
                modifier = Modifier.testTag("lock_screen_sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Keluar dari Akun Google",
                    style = MaterialTheme.typography.labelLarge,
                    color = Slate400
                )
            }
        }
    }
}
