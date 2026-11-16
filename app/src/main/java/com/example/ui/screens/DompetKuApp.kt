package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.auth.BiometricAuthHelper
import com.example.auth.BiometricAvailability
import com.example.auth.GoogleAuthHelper
import com.example.data.DompetKuRepository
import com.example.data.UserPreferencesDataStore
import com.example.notifications.ReminderScheduler
import com.example.ui.DompetKuViewModel
import com.example.ui.UiState
import com.example.ui.components.AddTransactionSheet
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.Teal500
import com.example.util.CsvExporter
import com.example.util.DateUtils
import com.example.util.MonthlySpendingPoint
import com.example.util.PdfReportExporter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_REPORT = "report"
const val ROUTE_CATEGORIES = "categories"
const val ROUTE_RECURRING = "recurring"

private data class NavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val navTabs = listOf(
    NavTab(ROUTE_DASHBOARD, "Beranda", Icons.Rounded.Dashboard, Icons.Outlined.Dashboard),
    NavTab(ROUTE_REPORT, "Laporan", Icons.Rounded.BarChart, Icons.Outlined.BarChart),
    NavTab(ROUTE_CATEGORIES, "Kategori", Icons.Rounded.Category, Icons.Outlined.Category),
    NavTab(ROUTE_RECURRING, "Pengingat", Icons.Rounded.NotificationsActive, Icons.Outlined.NotificationsActive),
)

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun DompetKuAppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        val uid = user.uid
        key(uid) {
            AuthenticatedDompetKuScreen(
                currentUserId = uid,
                userEmail = user.email ?: ""
            )
        }
    }
}

@Composable
private fun AuthenticatedDompetKuScreen(
    currentUserId: String,
    userEmail: String,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: DompetKuViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                DompetKuViewModel(
                    repository = DompetKuRepository(db),
                    preferences = UserPreferencesDataStore(app),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_DASHBOARD

    val transactionsState by viewModel.transactionsState.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val monthlySpendingReport by viewModel.monthlySpendingReport.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val isPrivacyHidden by viewModel.isPrivacyHidden.collectAsStateWithLifecycle()
    val isBiometricLockEnabled by viewModel.isBiometricLockEnabled.collectAsStateWithLifecycle()
    val isAccessGranted by viewModel.isAccessGranted.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategoryOptions.collectAsStateWithLifecycle()
    val customCategoriesState by viewModel.customCategoriesState.collectAsStateWithLifecycle()
    val recurringState by viewModel.recurringState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var showDataManageDialog by remember { mutableStateOf(false) }
    var suppressNextBackgroundLock by remember { mutableStateOf(false) }
    var pendingPdfMonth by remember { mutableStateOf<MonthlySpendingPoint?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isBiometricLockEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (isBiometricLockEnabled && !suppressNextBackgroundLock) {
                        viewModel.lockVault()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    suppressNextBackgroundLock = false
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Sync AlarmManager reminders whenever recurring items change
    LaunchedEffect(recurringState) {
        val items = (recurringState as? UiState.Success)?.data ?: return@LaunchedEffect
        ReminderScheduler.syncReminders(context, items)
    }

    LaunchedEffect(viewModel) {
        viewModel.toastMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val list = (transactionsState as? UiState.Success)?.data.orEmpty()
            val csv = CsvExporter.buildTransactionsCsv(list)
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(csv.toByteArray(Charsets.UTF_8))
                }
                scope.launch {
                    snackbarHostState.showSnackbar("Laporan CSV berhasil diekspor!")
                }
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Gagal mengekspor CSV: ${e.localizedMessage}")
                }
            }
        }
    }

    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            val allTransactions = (transactionsState as? UiState.Success)?.data.orEmpty()
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    PdfReportExporter.writeMonthlySummaryPdf(
                        outputStream = out,
                        report = monthlySpendingReport,
                        selectedMonth = pendingPdfMonth ?: monthlySpendingReport.months.lastOrNull(),
                        transactions = allTransactions,
                        userEmail = userEmail
                    )
                }
                scope.launch {
                    snackbarHostState.showSnackbar("Laporan PDF berhasil disimpan!")
                }
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Gagal membuat PDF: ${e.localizedMessage}")
                }
            }
        }
    }

    if (!isAccessGranted) {
        BiometricLockScreen(
            userEmail = userEmail,
            onUnlocked = viewModel::unlockVault,
            onSuppressNextBackgroundLock = { suppressNextBackgroundLock = true },
            onSignOut = {
                GoogleAuthHelper.signOut(credentialManager, scope)
            }
        )
        return
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = Slate950,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                DompetKuTopBar(
                    isBiometricLockEnabled = isBiometricLockEnabled,
                    onLockNow = viewModel::lockVault,
                    onExportCsv = {
                        val list = (transactionsState as? UiState.Success)?.data.orEmpty()
                        if (list.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Tidak ada transaksi untuk diekspor")
                            }
                        } else {
                            suppressNextBackgroundLock = true
                            csvExportLauncher.launch("Laporan_Keuangan_DompetKu_${DateUtils.todayIso()}.csv")
                        }
                    },
                    onOpenDataDialog = { showDataManageDialog = true }
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = Slate900,
                        contentColor = Color.White,
                        modifier = Modifier.border(1.dp, Slate800)
                    ) {
                        navTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Slate950,
                                    selectedTextColor = Emerald400,
                                    indicatorColor = Emerald500,
                                    unselectedIconColor = Slate400,
                                    unselectedTextColor = Slate400
                                )
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (currentRoute == ROUTE_DASHBOARD) {
                    ExtendedFloatingActionButton(
                        onClick = { showAddTransactionSheet = true },
                        containerColor = Emerald500,
                        contentColor = Slate950,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .then(if (useRail) Modifier.navigationBarsPadding() else Modifier)
                            .testTag("add_transaction_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Catat Transaksi"
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "Catat Transaksi",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = Slate900,
                        contentColor = Color.White,
                        modifier = Modifier
                            .fillMaxHeight()
                            .border(1.dp, Slate800)
                    ) {
                        Spacer(modifier = Modifier.size(16.dp))
                        navTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Slate950,
                                    selectedTextColor = Emerald400,
                                    indicatorColor = Emerald500,
                                    unselectedIconColor = Slate400,
                                    unselectedTextColor = Slate400
                                )
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_DASHBOARD,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_DASHBOARD) {
                        DashboardScreen(
                            transactionsState = transactionsState,
                            filteredTransactions = filteredTransactions,
                            metrics = metrics,
                            selectedFilter = selectedFilter,
                            isPrivacyHidden = isPrivacyHidden,
                            onTogglePrivacy = viewModel::togglePrivacy,
                            onSelectFilter = viewModel::setFilter,
                            onDeleteTransaction = viewModel::deleteTransaction,
                            onLoadSampleData = viewModel::loadSampleTransactions,
                            onOpenReport = {
                                navController.navigate(ROUTE_REPORT) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable(ROUTE_REPORT) {
                        SpendingReportScreen(
                            transactionsState = transactionsState,
                            report = monthlySpendingReport,
                            isPrivacyHidden = isPrivacyHidden,
                            onTogglePrivacy = viewModel::togglePrivacy,
                            onDownloadPdf = { month ->
                                pendingPdfMonth = month
                                suppressNextBackgroundLock = true
                                val monthTag = month?.yearMonth ?: DateUtils.todayIso()
                                pdfExportLauncher.launch("Laporan_Keuangan_DompetKu_${monthTag}.pdf")
                            }
                        )
                    }
                    composable(ROUTE_CATEGORIES) {
                        CategoriesScreen(
                            customCategoriesState = customCategoriesState,
                            onSaveCustomCategory = viewModel::saveCustomCategory,
                            onDeleteCustomCategory = viewModel::deleteCustomCategory
                        )
                    }
                    composable(ROUTE_RECURRING) {
                        RecurringScreen(
                            recurringState = recurringState,
                            categories = allCategories,
                            isPrivacyHidden = isPrivacyHidden,
                            onSaveRecurring = viewModel::saveRecurringTransaction,
                            onToggleEnabled = viewModel::toggleRecurringEnabled,
                            onDeleteRecurring = viewModel::deleteRecurringTransaction,
                            onRecordNow = viewModel::recordRecurringNow,
                            onShowToast = { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddTransactionSheet) {
        AddTransactionSheet(
            categories = allCategories,
            onDismiss = { showAddTransactionSheet = false },
            onSave = viewModel::addTransaction,
            onManageCategoriesClick = {
                navController.navigate(ROUTE_CATEGORIES) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }

    if (showDataManageDialog) {
        AlertDialog(
            onDismissRequest = { showDataManageDialog = false },
            containerColor = Slate900,
            title = {
                Text(
                    text = "Kelola Data & Akun",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (userEmail.isNotBlank()) {
                        Text(
                            text = "Masuk sebagai: $userEmail",
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald400
                        )
                    }
                    Text(
                        text = "Keamanan & Data DompetKu:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                    Surface(
                        color = Slate850,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Fingerprint,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Kunci Biometrik",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isBiometricLockEnabled) {
                                        "Aktif • Wajib sidik jari / wajah saat dibuka"
                                    } else {
                                        "Nonaktif • Detail transaksi langsung terbuka"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                            Switch(
                                checked = isBiometricLockEnabled,
                                onCheckedChange = { wantEnabled ->
                                    if (wantEnabled) {
                                        viewModel.setBiometricLockEnabled(true)
                                    } else {
                                        val activity = context as? FragmentActivity
                                        val availability = BiometricAuthHelper.checkAvailability(context)
                                        if (activity != null && availability == BiometricAvailability.READY) {
                                            suppressNextBackgroundLock = true
                                            BiometricAuthHelper.authenticate(
                                                activity = activity,
                                                title = "Nonaktifkan Kunci Biometrik",
                                                subtitle = "Konfirmasi identitas Anda terlebih dahulu",
                                                onSucceeded = {
                                                    suppressNextBackgroundLock = false
                                                    viewModel.setBiometricLockEnabled(false)
                                                },
                                                onError = { msg ->
                                                    suppressNextBackgroundLock = false
                                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                                },
                                                onCancelled = {
                                                    suppressNextBackgroundLock = false
                                                },
                                                onFallbackRequested = {
                                                    suppressNextBackgroundLock = true
                                                    GoogleAuthHelper.reauthenticateWithCredentialManager(
                                                        context = context,
                                                        credentialManager = credentialManager,
                                                        onSuccess = {
                                                            suppressNextBackgroundLock = false
                                                            viewModel.setBiometricLockEnabled(false)
                                                        },
                                                        onError = { msg ->
                                                            suppressNextBackgroundLock = false
                                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                                        },
                                                        scope = scope,
                                                        onCancelled = {
                                                            suppressNextBackgroundLock = false
                                                        }
                                                    )
                                                }
                                            )
                                        } else {
                                            suppressNextBackgroundLock = true
                                            GoogleAuthHelper.reauthenticateWithCredentialManager(
                                                context = context,
                                                credentialManager = credentialManager,
                                                onSuccess = {
                                                    suppressNextBackgroundLock = false
                                                    viewModel.setBiometricLockEnabled(false)
                                                },
                                                onError = { msg ->
                                                    suppressNextBackgroundLock = false
                                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                                },
                                                scope = scope,
                                                onCancelled = {
                                                    suppressNextBackgroundLock = false
                                                }
                                            )
                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Slate950,
                                    checkedTrackColor = Emerald500,
                                    uncheckedThumbColor = Slate400,
                                    uncheckedTrackColor = Slate800
                                ),
                                modifier = Modifier.testTag("biometric_lock_switch")
                            )
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.clearAllTransactions()
                            showDataManageDialog = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Rose500.copy(alpha = 0.2f),
                            contentColor = Rose400
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hapus Semua Transaksi (Mulai Rp 0)")
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.loadSampleTransactions()
                            showDataManageDialog = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Muat Data Contoh Awal", color = Color.White)
                    }
                    OutlinedButton(
                        onClick = {
                            showDataManageDialog = false
                            GoogleAuthHelper.signOut(credentialManager, scope)
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Keluar dari Akun Google", color = Slate400)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDataManageDialog = false }) {
                    Text("Tutup", color = Slate400)
                }
            }
        )
    }
}

@Composable
private fun DompetKuTopBar(
    isBiometricLockEnabled: Boolean,
    onLockNow: () -> Unit,
    onExportCsv: () -> Unit,
    onOpenDataDialog: () -> Unit,
) {
    val dateLabel = remember { DateUtils.formatHeaderDate() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate900.copy(alpha = 0.94f))
            .statusBarsPadding()
            .border(1.dp, Slate800.copy(alpha = 0.7f))
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
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
            Column {
                Text(
                    text = "DompetKu",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isBiometricLockEnabled) {
                IconButton(
                    onClick = onLockNow,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate850)
                        .border(1.dp, Slate700.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .testTag("lock_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Kunci Aplikasi",
                        tint = Slate400,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            IconButton(
                onClick = onExportCsv,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate850)
                    .border(1.dp, Slate700.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .testTag("export_csv_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = "Ekspor CSV",
                    tint = Emerald400,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onOpenDataDialog,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate850)
                    .border(1.dp, Slate700.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .testTag("manage_data_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.SettingsBackupRestore,
                    contentDescription = "Kelola Data & Akun",
                    tint = Slate400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
