package com.eostech.notepad.ui

import com.eostech.notepad.ui.theme.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.animation.animateColorAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eostech.notepad.data.*
import com.eostech.notepad.data.FirebaseSyncManager
import com.eostech.notepad.security.CryptoManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateContentSize
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

data class CustomField(val label: String, var value: String)

private fun getServiceColor(name: String): Color {
    val normalizedName = name.lowercase()
    return when {
        normalizedName.contains("google") -> Color(0xFF4285F4)
        normalizedName.contains("facebook") -> Color(0xFF1877F2)
        normalizedName.contains("twitter") || normalizedName.contains("x") -> Color(0xFF000000)
        normalizedName.contains("netflix") -> Color(0xFFE50914)
        normalizedName.contains("spotify") -> Color(0xFF1DB954)
        normalizedName.contains("instagram") -> Color(0xFFE4405F)
        normalizedName.contains("github") -> Color(0xFF181717)
        normalizedName.contains("microsoft") -> Color(0xFF00A4EF)
        normalizedName.contains("apple") -> Color(0xFF555555)
        else -> SoftBlue
    }
}

@Composable
fun PasswordItem(
    entry: PasswordEntity, 
    biometricHelper: com.eostech.notepad.security.BiometricHelper? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    
    val decryptedPassword = remember(entry, passwordVisible) {
        if (passwordVisible) {
            try {
                if (entry.encryptedPassword.isNotBlank() && entry.iv.isNotBlank()) {
                    CryptoManager.decryptString(entry.encryptedPassword, entry.iv)
                } else {
                    "No password data"
                }
            } catch (e: Exception) {
                "Error: Decryption failed"
            }
        } else "••••••••"
    }

    val customFields = remember(entry.customFields) {
        try {
            if (entry.customFields.isNullOrBlank()) emptyList<CustomField>()
            else {
                val type = object : TypeToken<List<CustomField>>() {}.type
                Gson().fromJson<List<CustomField>>(entry.customFields, type)
            }
        } catch (e: Exception) {
            emptyList<CustomField>()
        }
    }

    val bgColor = getServiceColor(entry.serviceName)

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Konfirmasi Hapus".tr(), color = CharcoalTitle) },
            text = { Text("Yakin ingin menghapus kredensial untuk ${entry.serviceName}?".tr(), color = CharcoalBody) },
            confirmButton = {
                Button(
                    onClick = { 
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                ) { Text("Hapus".tr(), color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal".tr(), color = CharcoalBody) }
            },
            containerColor = SurfaceWhite
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo or Icon
                Surface(
                    shape = CircleShape,
                    color = bgColor,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        var isImageSuccess by remember { mutableStateOf(false) }
                        if (entry.serviceName.isNotBlank()) {
                            val logoUrl = remember(entry.serviceName) {
                                val s = entry.serviceName.lowercase().trim().replace(" ", "")
                                if (s.contains(".")) "https://logo.clearbit.com/$s"
                                else "https://logo.clearbit.com/$s.com"
                            }
                            AsyncImage(
                                model = logoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                onSuccess = { isImageSuccess = true },
                                onError = { isImageSuccess = false },
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            
                            if (!isImageSuccess) {
                                Text(
                                    entry.serviceName.take(1).uppercase(), 
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), 
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.serviceName, 
                        style = MaterialTheme.typography.titleMedium, 
                        color = CharcoalTitle,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = entry.username, 
                        style = MaterialTheme.typography.bodySmall, 
                        color = CharcoalBody.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CharcoalBody.copy(alpha = 0.4f)
                )
            }

            if (expanded) {
                Column(modifier = Modifier.padding(start = 76.dp, end = 16.dp, bottom = 16.dp)) {
                    // Divider
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = CharcoalBody.copy(alpha = 0.05f)
                    )

                    // Details
                    DetailRow(
                        label = "Nama Pengguna".tr(), 
                        value = entry.username,
                        onCopy = { clipboardManager.setText(AnnotatedString(entry.username)) }
                    )
                    
                    DetailRow(
                        label = "Sandi".tr(), 
                        value = decryptedPassword,
                        isPassword = true,
                        isRevealed = passwordVisible,
                        onCopy = { 
                            if (passwordVisible) {
                                clipboardManager.setText(AnnotatedString(decryptedPassword))
                            }
                        },
                        onToggleVisibility = {
                            try {
                                if (passwordVisible) {
                                    passwordVisible = false
                                } else {
                                    if (biometricHelper?.canAuthenticate() == true) {
                                        biometricHelper.showBiometricPrompt(
                                            "Reveal Password",
                                            "Review secret for ${entry.serviceName}",
                                            onSuccess = { passwordVisible = true },
                                            onError = { /* Log error */ }
                                        )
                                    } else {
                                        passwordVisible = true
                                    }
                                }
                            } catch (e: Exception) {
                                passwordVisible = true 
                            }
                        }
                    )
                    
                    if (entry.notes.isNotEmpty()) {
                        DetailRow(label = "Catatan".tr(), value = entry.notes)
                    }

                    customFields.forEach { field ->
                        DetailRow(label = field.label, value = field.value)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = bgColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = entry.category, 
                                style = MaterialTheme.typography.labelSmall,
                                color = bgColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onEdit,
                                colors = IconButtonDefaults.iconButtonColors(contentColor = SoftBlue)
                            ) { 
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp)) 
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFE57373))
                            ) { 
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp)) 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String, 
    value: String, 
    isPassword: Boolean = false, 
    isRevealed: Boolean = false,
    onCopy: (() -> Unit)? = null, 
    onToggleVisibility: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label.uppercase(), 
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), 
            color = CharcoalBody.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value, 
                style = MaterialTheme.typography.bodyMedium, 
                color = CharcoalTitle, 
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            
            if (onCopy != null) {
                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.ContentCopy, 
                        contentDescription = "Copy", 
                        tint = SoftBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            if (isPassword && onToggleVisibility != null) {
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility, 
                        contentDescription = null,
                        tint = CharcoalBody.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddPasswordDialog(
    existingPassword: PasswordEntity? = null,
    onDismiss: () -> Unit, 
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(existingPassword?.serviceName ?: "") }
    var user by remember { mutableStateOf(existingPassword?.username ?: "") }
    var pass by remember { mutableStateOf("") } // We don't pre-fill password for security, or we decrypt it if editing?
    
    // For editing, we might want to decrypt the password to show it.
    LaunchedEffect(existingPassword) {
        if (existingPassword != null) {
            try {
                pass = CryptoManager.decryptString(existingPassword.encryptedPassword, existingPassword.iv)
            } catch (e: Exception) {}
        }
    }

    var category by remember { mutableStateOf(existingPassword?.category ?: "General") }
    var notes by remember { mutableStateOf(existingPassword?.notes ?: "") }
    val customFields = remember { 
        val list = mutableStateListOf<CustomField>()
        existingPassword?.customFields?.let { json ->
            try {
                val type = object : TypeToken<List<CustomField>>() {}.type
                val fields: List<CustomField> = Gson().fromJson(json, type)
                list.addAll(fields)
            } catch (e: Exception) {}
        }
        list
    }
    
    val categories = listOf("Umum", "Pribadi", "Pekerjaan", "Keuangan", "Sosial").map { it.tr() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingPassword == null) "Tambah Kredensial Baru".tr() else "Edit Kredensial".tr(), color = CharcoalTitle) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Smart Logo Preview
                if (name.length > 2) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        val bgColor = getServiceColor(name)
                        val previewUrl = remember(name) {
                            val s = name.lowercase().trim().replace(" ", "")
                            if (s.contains(".")) "https://logo.clearbit.com/$s"
                            else "https://logo.clearbit.com/$s.com"
                        }
                        AsyncImage(
                            model = previewUrl,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(bgColor.copy(alpha = 0.1f))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Fetching logo for ${name}...", style = MaterialTheme.typography.bodySmall, color = CharcoalBody.copy(alpha = 0.5f))
                    }
                }

                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Layanan (misal Netflix)".tr()) }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftBlue)
                )
                OutlinedTextField(
                    value = user, 
                    onValueChange = { user = it }, 
                    label = { Text("Nama Pengguna/Email".tr()) }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftBlue)
                )
                OutlinedTextField(
                    value = pass, 
                    onValueChange = { pass = it }, 
                    label = { Text("Kata Sandi".tr()) }, 
                    visualTransformation = PasswordVisualTransformation(), 
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftBlue)
                )
                
                Text("Kategori".tr(), style = MaterialTheme.typography.labelSmall, color = CharcoalBody)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SoftBlue)
                        )
                    }
                }

                // Custom Fields Section
                Text("Kolom Tambahan".tr(), style = MaterialTheme.typography.labelSmall, color = CharcoalBody)
                customFields.forEachIndexed { index, field ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = field.label,
                            onValueChange = { customFields[index] = field.copy(label = it) },
                            placeholder = { Text("Label") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = field.value,
                            onValueChange = { customFields[index] = field.copy(value = it) },
                            placeholder = { Text("Nilai".tr()) },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(onClick = { customFields.removeAt(index) }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color(0xFFE57373))
                        }
                    }
                }
                
                TextButton(onClick = { customFields.add(CustomField("", "")) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Kolom Kustom".tr())
                }

                OutlinedTextField(
                    value = notes, 
                    onValueChange = { notes = it }, 
                    label = { Text("Catatan Tambahan (Opsional)".tr()) }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftBlue)
                )
            }
        },
        containerColor = SurfaceWhite,
        confirmButton = {
            Button(
                onClick = { 
                    val fieldsJson = Gson().toJson(customFields.filter { it.label.isNotEmpty() })
                    onSave(name, user, pass, category, notes, fieldsJson) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoftBlue, contentColor = CharcoalTitle)
            ) { Text("Simpan Aman".tr()) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal".tr(), color = CharcoalBody) }
        }
    )
}
