package com.eostech.notepad

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eostech.notepad.ui.*
import com.eostech.notepad.ui.theme.AppNotepadTheme
import com.eostech.notepad.data.AppDatabase
import com.eostech.notepad.data.FirebaseSyncManager
import com.eostech.notepad.util.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.eostech.notepad.security.BiometricHelper
import com.eostech.notepad.security.PinManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts

import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            // Permission result handled
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val biometricHelper = BiometricHelper(this)
        val pinManager = PinManager(this)
        val syncManager = FirebaseSyncManager(this, database)
        val collabManager = com.eostech.notepad.data.CollaborationManager(this)
        val notificationHelper = NotificationHelper(this)

        lifecycleScope.launch {
            val uid = collabManager.getOrCreateLocalUserId()
            
            // Monitor Vault Session Status
            collabManager.vaultId.collect { vid ->
                if (vid != null) {
                    FirebaseFirestore.getInstance().collection("vaults").document(vid)
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null && snapshot.getString("status") == "ended") {
                                notificationHelper.showNotification("Sesi Berakhir", "Partner telah mengakhiri sesi kolaborasi.")
                                lifecycleScope.launch { collabManager.disconnect() }
                            }
                        }
                }
            }
        }

        lifecycleScope.launch {
            val uid = collabManager.getOrCreateLocalUserId()
            val pId = collabManager.partnerId.first()
            
            // Logic: Jika kita adalah Klien (punya Partner ID), maka sinkronisasi data PELADEN Partner (Host)
            // Jika kita adalah Host atau Standalone, sinkronisasi data kita sendiri (Local UI)
            val syncId = if (pId != null && pId != "client_connected") pId else uid
            
            if (com.eostech.notepad.NotepadApplication.isFirebaseReady()) {
                syncManager.startSync(syncId)
            }
        }

        // Apply screenshot prevention from saved settings
        val securityPrefs = getSharedPreferences("security_settings", android.content.Context.MODE_PRIVATE)
        if (securityPrefs.getBoolean("screenshot_prevention", true)) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }

        setContent {
            AppNotepadTheme {
                var isAppUnlocked by remember { mutableStateOf(!pinManager.isPinSet()) }
                var pinError by remember { mutableStateOf(false) }

                if (!isAppUnlocked) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PinLockScreen(
                            title = "App Locked",
                            onPinComplete = { input -> 
                                if (pinManager.verifyPin(input)) {
                                    isAppUnlocked = true
                                } else {
                                    pinError = true
                                }
                            },
                            isError = pinError,
                            paddingValues = PaddingValues(top = 100.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)
                        )
                    }
                } else {
                    MainApp(database, biometricHelper, pinManager, syncManager, onSecureScreenEntered = { isSecure ->
                        if (isSecure) {
                            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(database: AppDatabase, biometricHelper: BiometricHelper, pinManager: PinManager, syncManager: FirebaseSyncManager, onSecureScreenEntered: (Boolean) -> Unit) {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf("vault") }
    var isDoodleEnabled by remember { mutableStateOf(true) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val collabManager = remember { com.eostech.notepad.data.CollaborationManager(context) }
    val partnerId by collabManager.partnerId.collectAsState(initial = null)
    val vaultId by collabManager.vaultId.collectAsState(initial = null)
    val myId by collabManager.localUserId.collectAsState(initial = "user_loading")
    val isConnected = partnerId != null

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Vault".tr()) },
                    label = { Text("Vault".tr()) },
                    selected = currentRoute == "vault",
                    alwaysShowLabel = true,
                    onClick = {
                        currentRoute = "vault"
                        navController.navigate("vault") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true }
                        onSecureScreenEntered(false)
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Group, contentDescription = "Bersama".tr()) },
                    label = { Text("Bersama".tr()) },
                    selected = currentRoute == "shared",
                    alwaysShowLabel = true,
                    onClick = {
                        currentRoute = "shared"
                        navController.navigate("shared") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true }
                        onSecureScreenEntered(false)
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Build, contentDescription = "Sandi".tr()) },
                    label = { Text("Sandi".tr()) },
                    selected = currentRoute == "generator",
                    alwaysShowLabel = true,
                    onClick = {
                        currentRoute = "generator"
                        navController.navigate("generator") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true }
                        onSecureScreenEntered(false)
                    }
                )
                NavigationBarItem(
                    icon = {
                        BadgedBox(badge = {
                            if (isConnected) Badge(containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                        }) { Icon(Icons.Default.Chat, contentDescription = "Chat".tr()) }
                    },
                    label = { Text("Chat".tr()) },
                    selected = currentRoute == "chat",
                    alwaysShowLabel = true,
                    onClick = {
                        currentRoute = "chat"
                        navController.navigate("chat") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true }
                        onSecureScreenEntered(false)
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Setelan".tr()) },
                    label = { Text("Setelan".tr()) },
                    selected = currentRoute == "settings",
                    alwaysShowLabel = true,
                    onClick = {
                        currentRoute = "settings"
                        navController.navigate("settings") { popUpTo(navController.graph.startDestinationId); launchSingleTop = true }
                        onSecureScreenEntered(false)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "vault",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
        ) {
            composable("vault") { UnifiedVaultScreen(database, navController, syncManager, biometricHelper, false, isDoodleEnabled) }
            composable("shared") { UnifiedVaultScreen(database, navController, syncManager, biometricHelper, true, isDoodleEnabled) }
            composable("generator") { PasswordGeneratorScreen(navController) }
            composable("chat") { ChatScreen(navController, syncManager, vaultId, myId) }
            composable("settings") { 
                SettingsScreen(navController, biometricHelper, pinManager, syncManager, database, isDoodleEnabled, onDoodleToggle = { isDoodleEnabled = it }) 
            }
            composable("edit_note/{noteId}") { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")?.toLong() ?: 0L
                NoteEditorScreen(database.noteDao(), navController, noteId, syncManager, isDoodleEnabled)
            }
            composable("edit_checklist/{checklistId}") { backStackEntry ->
                val checklistId = backStackEntry.arguments?.getString("checklistId")?.toLong() ?: 0L
                ChecklistEditorScreen(database.checklistDao(), navController, checklistId, syncManager, isDoodleEnabled)
            }
        }
    }
}


