package com.eostech.notepad.ui

import com.eostech.notepad.ui.theme.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eostech.notepad.data.*
import com.eostech.notepad.data.FirebaseSyncManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistEditorScreen(dao: ChecklistDao, navController: NavController, checklistId: Long, sync: FirebaseSyncManager, isDoodleEnabled: Boolean) {
    val viewModel: ChecklistViewModel = viewModel(factory = ViewModelFactory(dao, sync))
    
    // Track the current ID locally so we can update it if we create a new checklist on the fly
    var activeChecklistId by remember { mutableStateOf(checklistId) }
    
    val checklists: List<ChecklistEntity> by viewModel.checklists.observeAsState(emptyList())
    val existingChecklist = checklists.find { it.id == activeChecklistId }
    val items: List<ChecklistItemEntity> by viewModel.getItems(activeChecklistId).observeAsState(emptyList())

    var title by remember { mutableStateOf(existingChecklist?.title ?: "") }
    var isShared by remember { mutableStateOf(existingChecklist?.isShared ?: false) }
    var newItemContent by remember { mutableStateOf("") }
    var selectedFont by remember { mutableStateOf(existingChecklist?.fontFamily ?: "Default") }
    var selectedFontSize by remember { mutableStateOf(existingChecklist?.fontSize ?: 16f) }
    var showTypographyMenu by remember { mutableStateOf(false) }

    LaunchedEffect(existingChecklist) {
        if (existingChecklist != null) {
            title = existingChecklist.title
            isShared = existingChecklist.isShared
            selectedFont = existingChecklist.fontFamily
            selectedFontSize = existingChecklist.fontSize
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(if (checklistId == 0L) "Daftar Centang Baru".tr() else "Edit Daftar Centang".tr(), color = CharcoalTitle)
                        Text(if (isShared) "Bagikan dengan Partner".tr() else "Hanya Pribadi".tr(), style = MaterialTheme.typography.labelSmall, color = if (isShared) SageGreen else CharcoalBody.copy(alpha = 0.5f))
                    }
                },
                actions = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        IconButton(onClick = { showTypographyMenu = !showTypographyMenu }) {
                            Icon(Icons.Default.TextFormat, contentDescription = "Typography", tint = CharcoalTitle)
                        }
                        Text("Dibagikan".tr(), style = MaterialTheme.typography.labelMedium, color = CharcoalBody)
                        Switch(checked = isShared, onCheckedChange = { isShared = it }, modifier = Modifier.scale(0.8f))
                        if (checklistId != 0L) {
                            IconButton(onClick = { 
                                existingChecklist?.let { viewModel.deleteChecklist(it) }
                                navController.popBackStack()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CharcoalBody)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp, color = SurfaceWhite) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    TextField(
                        value = newItemContent,
                        onValueChange = { newItemContent = it },
                        placeholder = { Text("Cari/tambahkan item...".tr(), color = CharcoalBody.copy(alpha = 0.3f)) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedTextColor = CharcoalBody,
                            unfocusedTextColor = CharcoalBody
                        )
                    )
                    IconButton(onClick = {
                        if (newItemContent.isNotBlank()) {
                            val contentToSave = newItemContent
                            if (activeChecklistId == 0L) {
                                viewModel.saveChecklist(0L, title, isShared, selectedFont, selectedFontSize) { savedId ->
                                    activeChecklistId = savedId
                                    viewModel.addItem(savedId, contentToSave)
                                }
                            } else {
                                viewModel.addItem(activeChecklistId, contentToSave)
                            }
                            newItemContent = ""
                        }
                    }) { Icon(Icons.Default.Add, contentDescription = "Add", tint = CharcoalTitle) }
                    Button(onClick = {
                        viewModel.saveChecklist(activeChecklistId, title, isShared, selectedFont, selectedFontSize)
                        navController.popBackStack()
                    }, colors = ButtonDefaults.buttonColors(containerColor = if (isShared) SageGreen else SoftBlue, contentColor = CharcoalTitle)) {
                        Text("Simpan".tr())
                    }
                }
            }
        },
        containerColor = SurfaceWhite
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
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                if (showTypographyMenu) {
                    val localCtx = androidx.compose.ui.platform.LocalContext.current
                    TypographyToolbar(
                        selectedFont = selectedFont,
                        onFontSelected = { selectedFont = it },
                        selectedSize = selectedFontSize,
                        onSizeSelected = { selectedFontSize = it },
                        context = localCtx
                    )
                }
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Judul Daftar".tr(), style = MaterialTheme.typography.headlineMedium, color = CharcoalBody.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedTextColor = CharcoalTitle,
                        unfocusedTextColor = CharcoalTitle
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = getFontFamily(selectedFont)
                    )
                )
            LazyColumn {
                items(items) { item ->
                    ChecklistItemRow(
                        item = item, 
                        onToggle = { viewModel.toggleItem(item) },
                        fontFamily = selectedFont,
                        fontSize = selectedFontSize
                    )
                }
        }
    }
}
}
}

@Composable
fun ChecklistItemRow(item: ChecklistItemEntity, onToggle: () -> Unit, fontFamily: String, fontSize: Float) {
    val strikeProgress by animateFloatAsState(targetValue = if (item.isChecked) 1f else 0f)
    
    ListItem(
        headlineContent = {
            Text(
                item.content,
                color = if (item.isChecked) CharcoalBody.copy(alpha = 0.5f) else CharcoalTitle,
                fontFamily = getFontFamily(fontFamily),
                fontSize = androidx.compose.ui.unit.TextUnit(fontSize, androidx.compose.ui.unit.TextUnitType.Sp),
                modifier = Modifier.drawWithContent {
                    drawContent()
                    if (strikeProgress > 0f) {
                        val y = size.height / 2f
                        drawLine(
                            color = CharcoalBody.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(size.width * strikeProgress, y),
                            strokeWidth = 2f
                        )
                    }
                }
            )
        },
        leadingContent = { 
            Checkbox(
                checked = item.isChecked, 
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = SageGreen, uncheckedColor = SoftGrey, checkmarkColor = CharcoalTitle)
            ) 
        },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
}

@Composable
fun AddChecklistDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Checklist") },
        text = { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { Button(onClick = { onSave(title) }) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
