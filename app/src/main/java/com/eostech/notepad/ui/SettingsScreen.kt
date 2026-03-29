package com.eostech.notepad.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eostech.notepad.data.CollaborationManager
import com.eostech.notepad.data.FirebaseSyncManager
import com.eostech.notepad.security.BiometricHelper
import com.eostech.notepad.security.PinManager
import com.eostech.notepad.ui.theme.*
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.Alignment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.eostech.notepad.data.AppBackupManager
import android.widget.Toast
import androidx.compose.ui.window.Dialog
import android.content.Context
import android.view.WindowManager
import androidx.compose.ui.platform.LocalView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController, 
    biometricHelper: BiometricHelper, 
    pinManager: PinManager,
    sync: FirebaseSyncManager,
    database: com.eostech.notepad.data.AppDatabase,
    isDoodleEnabled: Boolean,
    onDoodleToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val collabManager = remember { CollaborationManager(context) }
    val scope = rememberCoroutineScope()
    
    val partnerId by collabManager.partnerId.collectAsState(initial = null)
    val vaultId by collabManager.vaultId.collectAsState(initial = null)
    val hostToken by collabManager.hostToken.collectAsState(initial = null)
    
    val localUserId by collabManager.localUserId.collectAsState(initial = "user_loading")
    
    var inputCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isGeneratingCode by remember { mutableStateOf(false) }

    var isPinEnabled by remember { mutableStateOf(pinManager.isPinSet()) }
    var showPinSetup by remember { mutableStateOf(false) }
    var pinStep by remember { mutableStateOf(1) } // 1: Enter, 2: Confirm
    var firstPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    // Persistent preferences for security settings
    val securityPrefs = remember { context.getSharedPreferences("security_settings", Context.MODE_PRIVATE) }
    var isBiometricEnabled by remember { mutableStateOf(securityPrefs.getBoolean("biometric_enabled", false)) }
    var isScreenshotPrevEnabled by remember { mutableStateOf(securityPrefs.getBoolean("screenshot_prevention", true)) }

    val isConnected = partnerId != null
    val scrollState = rememberScrollState()

    val backupManager = remember { 
        AppBackupManager(
            context, 
            database.noteDao(), 
            database.checklistDao(), 
            database.passwordDao()
        ) 
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { 
            scope.launch {
                val result = backupManager.exportData(it)
                if (result.isSuccess) {
                    Toast.makeText(context, "Data exported successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Export failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val result = backupManager.importData(it)
                if (result.isSuccess) {
                    Toast.makeText(context, "Data imported successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Import failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan".tr(), color = CharcoalTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back".tr(), tint = CharcoalTitle)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text("Kolaborasi".tr(), style = MaterialTheme.typography.titleLarge, color = CharcoalTitle)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!isConnected) {
                        Text("Pasangkan dengan partner untuk membagikan Vault Anda secara aman.".tr(), style = MaterialTheme.typography.bodyMedium, color = CharcoalBody)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (!com.eostech.notepad.NotepadApplication.isFirebaseReady()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                                    Column {
                                        Text("Firebase belum dikonfigurasi".tr(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFE65100))
                                        Text("Paste token dari teman Anda di bawah ini untuk langsung terhubung, atau buat server Anda sendiri di bagian Firebase Setup.".tr(), style = MaterialTheme.typography.bodySmall, color = Color(0xFFBF360C))
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { 
                                    scope.launch {
                                        isGeneratingCode = true
                                        try {
                                            collabManager.generatePairingCode(localUserId)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                        } finally {
                                            isGeneratingCode = false
                                        }
                                    }
                                }, 
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isGeneratingCode,
                                colors = ButtonDefaults.buttonColors(containerColor = SoftBlue, contentColor = CharcoalTitle),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                if (isGeneratingCode) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = CharcoalTitle)
                                } else {
                                    Text("Generate Token")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = SoftGrey.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { inputCode = it },
                            label = { Text("Masukkan Token".tr()) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftBlue,
                                unfocusedBorderColor = SoftGrey,
                                focusedTextColor = CharcoalTitle,
                                unfocusedTextColor = CharcoalTitle
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = { 
                                scope.launch {
                                    isLoading = true
                                    try {
                                        val success = collabManager.joinVault(localUserId, inputCode.trim())
                                        if (success) {
                                            android.widget.Toast.makeText(context, "Terhubung! Memulai ulang aplikasi...", android.widget.Toast.LENGTH_LONG).show()
                                            kotlinx.coroutines.delay(1000)
                                            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                                            intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(intent)
                                            kotlin.system.exitProcess(0)
                                        } else {
                                            android.widget.Toast.makeText(context, "❌ Token pasangan cacat atau format tidak valid.", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Kesalahan koneksi: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }, 
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            enabled = inputCode.isNotEmpty() && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen, contentColor = CharcoalTitle),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = CharcoalTitle) else Text("Hubungkan ke Partner".tr())
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = SageGreen, modifier = Modifier.padding(top = 4.dp))
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (hostToken != null) "Connected Host" else "Connected", color = CharcoalTitle, style = MaterialTheme.typography.titleMedium)
                                Text("ID Ruangan: $vaultId", color = CharcoalBody, style = MaterialTheme.typography.bodyMedium)
                            }
                            TextButton(
                                onClick = { scope.launch { collabManager.disconnect() } },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("Akhiri Sesi".tr(), color = Color(0xFFE57373))
                            }
                        }

                        if (hostToken != null) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Token Anda".tr(), 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = CharcoalBody.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .background(
                                            color = SoftBlue.copy(alpha = 0.1f),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (hostToken!!.length > 20) "${hostToken!!.take(12)}...${hostToken!!.takeLast(12)}" else hostToken!!, 
                                        style = MaterialTheme.typography.titleMedium, 
                                        color = SoftBlue, 
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("Pairing Token", hostToken)
                                            clipboard.setPrimaryClip(clip)
                                            android.widget.Toast.makeText(context, "Token berhasil disalin!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Salin Token",
                                            tint = SoftBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Personalisasi".tr(), style = MaterialTheme.typography.titleLarge, color = CharcoalTitle)
            Spacer(modifier = Modifier.height(8.dp))
            ListItem(
                headlineContent = { Text("Tema Kanvas: Doodle Art".tr(), color = CharcoalTitle) },
                supportingContent = { Text("Terapkan pola latar belakang coretan tangan halus ke ruang kerja Anda.".tr(), color = CharcoalBody) },
                trailingContent = { Switch(checked = isDoodleEnabled, onCheckedChange = onDoodleToggle, colors = SwitchDefaults.colors(checkedTrackColor = SoftBlue)) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            var expandedLanguage by remember { mutableStateOf(false) }
            val currentLanguage = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE).getString("language", "id") ?: "id"
            
            ListItem(
                headlineContent = { Text("Bahasa / Language".tr(), color = CharcoalTitle) },
                supportingContent = { Text("Pilih bahasa aplikasi".tr(), color = CharcoalBody) },
                leadingContent = { Icon(androidx.compose.material.icons.Icons.Default.Language, contentDescription = null, tint = SoftBlue) },
                trailingContent = {
                    Box {
                        TextButton(onClick = { expandedLanguage = true }) {
                            Text(if (currentLanguage == "id") "Indonesia" else "English", color = SoftBlue)
                            Icon(androidx.compose.material.icons.Icons.Default.ArrowDropDown, contentDescription = null, tint = SoftBlue)
                        }
                        DropdownMenu(
                            expanded = expandedLanguage,
                            onDismissRequest = { expandedLanguage = false },
                            modifier = Modifier.background(SurfaceWhite)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Indonesia", color = CharcoalTitle) },
                                onClick = {
                                    context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE).edit().putString("language", "id").apply()
                                    expandedLanguage = false
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
                                        localeManager.applicationLocales = android.os.LocaleList(java.util.Locale.forLanguageTag("id"))
                                    } else {
                                        val activity = context as? android.app.Activity
                                        activity?.recreate()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("English", color = CharcoalTitle) },
                                onClick = {
                                    context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE).edit().putString("language", "en").apply()
                                    expandedLanguage = false
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
                                        localeManager.applicationLocales = android.os.LocaleList(java.util.Locale.forLanguageTag("en"))
                                    } else {
                                        val activity = context as? android.app.Activity
                                        activity?.recreate()
                                    }
                                }
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Keamanan".tr(), style = MaterialTheme.typography.titleLarge, color = CharcoalTitle)
            Spacer(modifier = Modifier.height(8.dp))
            ListItem(
                headlineContent = { Text("Kunci PIN Aplikasi".tr(), color = CharcoalTitle) },
                supportingContent = { Text("Memerlukan PIN 6 digit untuk membuka aplikasi.".tr(), color = CharcoalBody) },
                trailingContent = { 
                    Switch(
                        checked = isPinEnabled, 
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                showPinSetup = true
                                pinStep = 1
                            } else {
                                pinManager.clearPin()
                                isPinEnabled = false
                            }
                        }, 
                        colors = SwitchDefaults.colors(checkedTrackColor = SoftBlue)
                    ) 
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            if (isPinEnabled) {
                TextButton(onClick = { 
                    showPinSetup = true
                    pinStep = 1
                }, modifier = Modifier.padding(start = 16.dp)) {
                    Text("Ganti PIN".tr(), color = SoftBlue)
                }
            }

            ListItem(
                headlineContent = { Text("Kunci Biometrik".tr(), color = CharcoalTitle) },
                supportingContent = { Text("Wajibkan sidik jari saat buka Aplikasi/Vault".tr(), color = CharcoalBody) },
                trailingContent = {
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !biometricHelper.canAuthenticate()) {
                                Toast.makeText(context, "Biometric not available or not set up on this device", Toast.LENGTH_LONG).show()
                            } else {
                                isBiometricEnabled = enabled
                                securityPrefs.edit().putBoolean("biometric_enabled", enabled).apply()
                                Toast.makeText(
                                    context,
                                    if (enabled) "Biometric Lock enabled" else "Biometric Lock disabled",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = SoftBlue)
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            ListItem(
                headlineContent = { Text("Tangkapan & Rekaman Layar".tr(), color = CharcoalTitle) },
                supportingContent = { Text("Izinkan sistem merekam layar aplikasi".tr(), color = CharcoalBody) },
                trailingContent = {
                    Switch(
                        checked = isScreenshotPrevEnabled,
                        onCheckedChange = { enabled ->
                            isScreenshotPrevEnabled = enabled
                            securityPrefs.edit().putBoolean("screenshot_prevention", enabled).apply()
                            Toast.makeText(
                                context,
                                if (enabled) "Screenshot prevention ON" else "Screenshot prevention OFF",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = SoftBlue)
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Firebase Setup", style = MaterialTheme.typography.titleLarge, color = CharcoalTitle)
            Spacer(modifier = Modifier.height(8.dp))
            FirebaseSetupCard(context = context, isConnected = isConnected)

            Spacer(modifier = Modifier.height(24.dp))
            Text("Data Management", style = MaterialTheme.typography.titleLarge, color = CharcoalTitle)

            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Backup & Restore", style = MaterialTheme.typography.titleMedium, color = CharcoalTitle)
                    Text("Export your notes, checklists, and passwords to a JSON file or restore them later.", style = MaterialTheme.typography.bodySmall, color = CharcoalBody)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { exportLauncher.launch("AppNotepad_Backup_${System.currentTimeMillis()}.json") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftBlue, contentColor = CharcoalTitle),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export")
                        }
                        Button(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftLavender, contentColor = CharcoalTitle),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Made with love ", style = MaterialTheme.typography.bodySmall, color = CharcoalBody)
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                Text(" by ", style = MaterialTheme.typography.bodySmall, color = CharcoalBody)
                Text(
                    "Eos Ageng", 
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SoftBlue),
                    modifier = Modifier.clickable { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/eosnada1702")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    if (showPinSetup) {
        Dialog(onDismissRequest = { 
            showPinSetup = false
            if (!pinManager.isPinSet()) isPinEnabled = false
        }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BackgroundLight,
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .wrapContentHeight()
                    .padding(16.dp),
                shadowElevation = 8.dp
            ) {
                Box {
                    PinLockScreen(
                        title = if (pinStep == 1) "Set 6-Digit PIN" else "Confirm PIN",
                        onPinComplete = { pin ->
                            if (pinStep == 1) {
                                firstPin = pin
                                pinStep = 2
                            } else {
                                if (pin == firstPin) {
                                    pinManager.setPin(pin)
                                    isPinEnabled = true
                                    showPinSetup = false
                                    android.widget.Toast.makeText(context, "PIN Set Successfully", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    pinError = "PINs do not match. Try again."
                                    firstPin = ""
                                    pinStep = 1
                                }
                            }
                        },
                        isError = pinError != null,
                        errorMessage = pinError ?: "",
                        backgroundColor = Color.Transparent,
                        paddingValues = PaddingValues(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
                    )
                    
                    // Close button for dialog
                    IconButton(
                        onClick = { 
                            showPinSetup = false
                            if (!pinManager.isPinSet()) isPinEnabled = false
                        },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CharcoalBody)
                    }
                }
            }
        }
    }
}

// =============================================================================
// Firebase Self-Setup Card
// =============================================================================
@Composable
fun FirebaseSetupCard(context: android.content.Context, isConnected: Boolean) {
    val prefs = remember { context.getSharedPreferences("firebase_config", android.content.Context.MODE_PRIVATE) }

    var projectId  by remember { mutableStateOf(prefs.getString("project_id", "") ?: "") }
    var apiKey     by remember { mutableStateOf(prefs.getString("api_key",    "") ?: "") }
    var appId      by remember { mutableStateOf(prefs.getString("app_id",     "") ?: "") }
    var dbUrl      by remember { mutableStateOf(prefs.getString("db_url",     "") ?: "") }
    var showApiKey by remember { mutableStateOf(false) }
    var saved      by remember { mutableStateOf(false) }
    val isUsingCustom = projectId.isNotEmpty() && apiKey.isNotEmpty() && appId.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        var showGuide by remember { mutableStateOf(false) }

        if (showGuide) {
            AlertDialog(
                onDismissRequest = { showGuide = false },
                title = { Text("Cara Setup Firebase", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
                    ) {
                        Text("1. Kunjungi console.firebase.google.com dan buat Proyek baru.", style = MaterialTheme.typography.bodyMedium)
                        Text("2. Tambahkan Aplikasi Android, wajib isi Package Name: com.eostech.notepad", style = MaterialTheme.typography.bodyMedium)
                        Text("3. Buka 'Project Settings' ⚙️, salin 'Project ID' dan 'Web API Key'.", style = MaterialTheme.typography.bodyMedium)
                        Text("4. Gulir ke bawah ke kotak Your Apps untuk menyalin 'App ID'.", style = MaterialTheme.typography.bodyMedium)
                        Text("5. Buka menu Build -> 'Realtime Database', buat db (Test Mode), salin URL Penuhnya (https://...).", style = MaterialTheme.typography.bodyMedium)
                        Text("6. Buka menu Build -> 'Firestore Database', buat db (Test Mode). Ini WAJIB untuk chat & sinkronisasi data.", style = MaterialTheme.typography.bodyMedium)
                        Text("7. Masukkan 4 data kustom tersebut di bawah, lalu klik Simpan.", style = MaterialTheme.typography.bodyMedium)
                        Text("8. Tutup aplikasi secara paksa (Force Close) lalu buka kembali.", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGuide = false }) {
                        Text("Mengerti", color = SoftBlue)
                    }
                },
                containerColor = SurfaceWhite,
                titleContentColor = CharcoalTitle,
                textContentColor = CharcoalBody
            )
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = if (isUsingCustom) SageGreen else SoftBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Firebase", style = MaterialTheme.typography.titleMedium, color = CharcoalTitle)
                }
                
                IconButton(onClick = { showGuide = true }) {
                    Icon(androidx.compose.material.icons.Icons.Default.HelpOutline, contentDescription = "Tutorial Setup Firebase", tint = SoftBlue)
                }
            }

            OutlinedTextField(
                value = projectId,
                onValueChange = { projectId = it; saved = false },
                label = { Text("Project ID") },
                placeholder = { Text("contoh: my-notepad-12345") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isConnected,
                leadingIcon = { Text("🔧", modifier = Modifier.padding(start = 8.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoftBlue, unfocusedBorderColor = SoftGrey,
                    focusedTextColor = CharcoalTitle, unfocusedTextColor = CharcoalTitle,
                    disabledTextColor = CharcoalBody.copy(alpha = 0.5f),
                    disabledBorderColor = SoftGrey.copy(alpha = 0.5f),
                    disabledLabelColor = CharcoalBody.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; saved = false },
                label = { Text("API Key") },
                placeholder = { Text("AIzaSy...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isConnected,
                visualTransformation = if (showApiKey)
                    androidx.compose.ui.text.input.VisualTransformation.None
                else
                    androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "Hide" else "Show",
                            tint = SoftGrey
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoftBlue, unfocusedBorderColor = SoftGrey,
                    focusedTextColor = CharcoalTitle, unfocusedTextColor = CharcoalTitle,
                    disabledTextColor = CharcoalBody.copy(alpha = 0.5f),
                    disabledBorderColor = SoftGrey.copy(alpha = 0.5f),
                    disabledLabelColor = CharcoalBody.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = appId,
                onValueChange = { appId = it; saved = false },
                label = { Text("App ID (google-app-id)") },
                placeholder = { Text("1:1234567890:android:abc123...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isConnected,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoftBlue, unfocusedBorderColor = SoftGrey,
                    focusedTextColor = CharcoalTitle, unfocusedTextColor = CharcoalTitle,
                    disabledTextColor = CharcoalBody.copy(alpha = 0.5f),
                    disabledBorderColor = SoftGrey.copy(alpha = 0.5f),
                    disabledLabelColor = CharcoalBody.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = dbUrl,
                onValueChange = { dbUrl = it; saved = false },
                label = { Text("Database URL Lengkap") },
                placeholder = { Text("https://xxx-default-rtdb.firebaseio.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isConnected,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoftBlue, unfocusedBorderColor = SoftGrey,
                    focusedTextColor = CharcoalTitle, unfocusedTextColor = CharcoalTitle,
                    disabledTextColor = CharcoalBody.copy(alpha = 0.5f),
                    disabledBorderColor = SoftGrey.copy(alpha = 0.5f),
                    disabledLabelColor = CharcoalBody.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        prefs.edit()
                            .putString("project_id", projectId.trim())
                            .putString("api_key",    apiKey.trim())
                            .putString("app_id",     appId.trim())
                            .putString("db_url",     dbUrl.trim())
                            .apply()
                        saved = true
                        Toast.makeText(context, "Firebase config tersimpan! Restart aplikasi untuk menerapkan.", Toast.LENGTH_LONG).show()
                    },
                    enabled = projectId.isNotBlank() && apiKey.isNotBlank() && appId.isNotBlank() && dbUrl.isNotBlank() && !isConnected,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftBlue, contentColor = CharcoalTitle),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Simpan & Terapkan")
                }

                if (isUsingCustom) {
                    OutlinedButton(
                        onClick = {
                            prefs.edit().clear().apply()
                            projectId = ""; apiKey = ""; appId = ""; dbUrl = ""; saved = false
                            Toast.makeText(context, "Reset ke Firebase bawaan. Restart aplikasi.", Toast.LENGTH_LONG).show()
                        },
                        enabled = !isConnected,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (!isConnected) Color(0xFFE57373) else Color(0xFFE57373).copy(alpha = 0.3f))
                    ) {
                        Text("Reset", color = if (!isConnected) Color(0xFFE57373) else Color(0xFFE57373).copy(alpha = 0.3f))
                    }
                }
            }

            if (saved) {
                Text(
                    "⚠️ Tutup dan buka aplikasi kembali agar perubahan diterapkan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}
