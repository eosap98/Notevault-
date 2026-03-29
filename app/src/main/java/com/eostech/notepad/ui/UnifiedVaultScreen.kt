package com.eostech.notepad.ui

import com.eostech.notepad.ui.theme.*

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eostech.notepad.data.*
import kotlinx.coroutines.launch

enum class VaultFilter { ALL, NOTES, CHECKLISTS, PASSWORDS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedVaultScreen(db: AppDatabase, navController: NavController, sync: FirebaseSyncManager, biometricHelper: com.eostech.notepad.security.BiometricHelper, isShared: Boolean, isDoodleEnabled: Boolean) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(VaultFilter.ALL) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showAddPasswordDialog by remember { mutableStateOf(false) }
    var editingPassword by remember { mutableStateOf<PasswordEntity?>(null) }

    val noteViewModel: NoteViewModel = viewModel(factory = ViewModelFactory(db.noteDao(), sync))
    val checklistViewModel: ChecklistViewModel = viewModel(factory = ViewModelFactory(db.checklistDao(), sync))
    val passwordViewModel: PasswordViewModel = viewModel(factory = ViewModelFactory(db.passwordDao(), sync))

    // Sync modes with ViewModels
    LaunchedEffect(isShared) {
        noteViewModel.setSharedMode(isShared)
        checklistViewModel.setSharedMode(isShared)
        passwordViewModel.setSharedMode(isShared)
    }

    val notes by noteViewModel.notes.observeAsState(emptyList())
    val checklists by checklistViewModel.checklists.observeAsState(emptyList())
    val passwords by passwordViewModel.passwords.observeAsState(emptyList())

    // Apply filters and search
    val filteredNotes = notes.filter { (it.isShared == isShared || selectedFilter == VaultFilter.ALL) && (it.title.contains(searchQuery, true) || it.content.contains(searchQuery, true)) }
    val filteredChecklists = checklists.filter { (it.isShared == isShared || selectedFilter == VaultFilter.ALL) && it.title.contains(searchQuery, true) }
    val filteredPasswords = passwords.filter { (it.isShared == isShared || selectedFilter == VaultFilter.ALL) && it.serviceName.contains(searchQuery, true) }

    val accentColor = if (isShared) SageGreen else SoftBlue

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(if (isShared) "Vault Bersama".tr() else "Vault Pribadi".tr(), color = CharcoalTitle) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
                )
                Surface(tonalElevation = 2.dp, color = BackgroundLight) {
                    Column {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Cari item...".tr(), color = CharcoalBody.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CharcoalBody) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = SoftGrey,
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite,
                                focusedTextColor = CharcoalTitle,
                                unfocusedTextColor = CharcoalTitle
                            )
                        )
                        ScrollableTabRow(
                            selectedTabIndex = selectedFilter.ordinal,
                            edgePadding = 16.dp,
                            divider = {},
                            indicator = {},
                            containerColor = androidx.compose.ui.graphics.Color.Transparent
                        ) {
                            VaultFilter.values().forEach { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor.copy(alpha = 0.5f),
                                        selectedLabelColor = CharcoalTitle,
                                        containerColor = SurfaceWhite,
                                        labelColor = CharcoalBody
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            Box(contentAlignment = Alignment.BottomEnd) {
                if (showFabMenu) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 80.dp).padding(end = 8.dp)) {
                        SmallFloatingActionButton(onClick = { navController.navigate("edit_note/0") }, modifier = Modifier.padding(4.dp), containerColor = SurfaceWhite) {
                            Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = "Tambah Catatan".tr(), tint = CharcoalTitle)
                        }
                        SmallFloatingActionButton(onClick = { navController.navigate("edit_checklist/0") }, modifier = Modifier.padding(4.dp), containerColor = SurfaceWhite) {
                            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Tambah Daftar".tr(), tint = CharcoalTitle)
                        }
                        SmallFloatingActionButton(onClick = { showAddPasswordDialog = true }, modifier = Modifier.padding(4.dp), containerColor = SurfaceWhite) {
                            Icon(Icons.Default.VpnKey, contentDescription = "Tambah Sandi".tr(), tint = CharcoalTitle)
                        }
                    }
                }
                FloatingActionButton(onClick = { showFabMenu = !showFabMenu }, containerColor = accentColor, contentColor = CharcoalTitle, shape = RoundedCornerShape(16.dp)) {
                    Icon(if (showFabMenu) Icons.Default.Close else Icons.Default.Add, contentDescription = "Tambah")
                }
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isDoodleEnabled) {
                Image(
                    painter = painterResource(id = com.eostech.notepad.R.drawable.doodle_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    alpha = 0.05f
                )
            }
            if (filteredNotes.isEmpty() && filteredChecklists.isEmpty() && filteredPasswords.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (selectedFilter == VaultFilter.PASSWORDS) Icons.Default.VpnKey else Icons.AutoMirrored.Filled.SpeakerNotes,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = accentColor.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when(selectedFilter) {
                            VaultFilter.NOTES -> "Tidak ada catatan".tr()
                            VaultFilter.CHECKLISTS -> "Tidak ada daftar centang".tr()
                            VaultFilter.PASSWORDS -> "Tidak ada sandi tersimpan".tr()
                            else -> "Vault Anda kosong".tr()
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = CharcoalBody.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Ketuk tombol + untuk menambahkan item pertama Anda".tr(),
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalBody.copy(alpha = 0.3f)
                    )
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.padding(padding).padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp), // Extra bottom padding for FAB
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedFilter == VaultFilter.ALL || selectedFilter == VaultFilter.NOTES) {
                        items(filteredNotes) { note ->
                            VaultNoteCard(note, onClick = { navController.navigate("edit_note/${note.id}") })
                        }
                    }
                    if (selectedFilter == VaultFilter.ALL || selectedFilter == VaultFilter.CHECKLISTS) {
                        items(filteredChecklists) { checklist ->
                            VaultChecklistCard(checklist, onClick = { navController.navigate("edit_checklist/${checklist.id}") })
                        }
                    }
                    if (selectedFilter == VaultFilter.ALL || selectedFilter == VaultFilter.PASSWORDS) {
                        for (password in filteredPasswords) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                PasswordItem(
                                    password, 
                                    biometricHelper = biometricHelper, 
                                    onEdit = { editingPassword = password },
                                    onDelete = { passwordViewModel.delete(password) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddPasswordDialog) {
            AddPasswordDialog(onDismiss = { showAddPasswordDialog = false }, onSave = { name, user, pwd, cat, nts, flds ->
                val (encrypted, iv) = com.eostech.notepad.security.CryptoManager.encryptString(pwd)
                val logoUrl = "https://logo.clearbit.com/${name.lowercase().replace(" ", "")}.com"
                passwordViewModel.insert(PasswordEntity(serviceName = name, username = user, encryptedPassword = encrypted, iv = iv, category = cat, notes = nts, customFields = flds, logoUrl = logoUrl, isShared = isShared))
                showAddPasswordDialog = false
                showFabMenu = false
            })
        }

        if (editingPassword != null) {
            AddPasswordDialog(
                existingPassword = editingPassword,
                onDismiss = { editingPassword = null },
                onSave = { name, user, pwd, cat, nts, flds ->
                    val (encrypted, iv) = com.eostech.notepad.security.CryptoManager.encryptString(pwd)
                    val logoUrl = "https://logo.clearbit.com/${name.lowercase().replace(" ", "")}.com"
                    val updated = editingPassword!!.copy(
                        serviceName = name,
                        username = user,
                        encryptedPassword = encrypted,
                        iv = iv,
                        category = cat,
                        notes = nts,
                        customFields = flds,
                        logoUrl = logoUrl
                    )
                    passwordViewModel.insert(updated) // This will replace due to upsert logic in DAO
                    editingPassword = null
                }
            )
        }
    }
}

@Composable
fun VaultNoteCard(note: NoteEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(note.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Text(note.content, style = MaterialTheme.typography.bodySmall, maxLines = 5, overflow = TextOverflow.Ellipsis)
            
            if (note.isShared) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    CollaborationAvatar("E")
                    Spacer(modifier = Modifier.width(4.dp))
                    CollaborationAvatar("N")
                }
            }
        }
    }
}

@Composable
fun VaultChecklistCard(checklist: ChecklistEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(checklist.title, style = MaterialTheme.typography.titleMedium)
            
            if (checklist.isShared) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    CollaborationAvatar("E")
                    Spacer(modifier = Modifier.width(4.dp))
                    CollaborationAvatar("N")
                }
            }
        }
    }
}

@Composable
fun CollaborationAvatar(initial: String) {
    Surface(
        modifier = Modifier.size(24.dp),
        shape = androidx.compose.foundation.shape.CircleShape,
        color = SageGreen,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceWhite)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(initial, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = CharcoalTitle)
        }
    }
}
