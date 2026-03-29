package com.eostech.notepad.ui

import com.eostech.notepad.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eostech.notepad.data.NoteDao
import com.eostech.notepad.data.NoteEntity
import com.eostech.notepad.data.FirebaseSyncManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.graphics.SolidColor

// We no longer use annotatedStringToMarkdown because we store the raw markdown text directly!
// But for backward compatibility with older notes loaded from DB:
fun convertLegacyToMarkdownIfNecessary(text: String): String {
    // If we wanted to keep legacy converter...
    return text
}

// =============================================================================
// Markdown Tag Injection Helpers
// =============================================================================

fun TextFieldValue.toggleBold(placeholder: String): TextFieldValue {
    val s = minOf(selection.start, selection.end)
    val e = maxOf(selection.start, selection.end)
    val txt = this.text
    if (s == e) {
        val newText = txt.substring(0, s) + "**$placeholder**" + txt.substring(s)
        return TextFieldValue(newText, selection = TextRange(s + 2, s + 2 + placeholder.length))
    }
    val newText = txt.substring(0, s) + "**" + txt.substring(s, e) + "**" + txt.substring(e)
    return TextFieldValue(newText, selection = TextRange(s, e + 4))
}

fun TextFieldValue.toggleItalic(placeholder: String): TextFieldValue {
    val s = minOf(selection.start, selection.end)
    val e = maxOf(selection.start, selection.end)
    val txt = this.text
    if (s == e) {
        val newText = txt.substring(0, s) + "_${placeholder}_" + txt.substring(s)
        return TextFieldValue(newText, selection = TextRange(s + 1, s + 1 + placeholder.length))
    }
    val newText = txt.substring(0, s) + "_" + txt.substring(s, e) + "_" + txt.substring(e)
    return TextFieldValue(newText, selection = TextRange(s, e + 2))
}

fun TextFieldValue.toggleHeading(placeholder: String): TextFieldValue {
    val s = minOf(selection.start, selection.end)
    val e = maxOf(selection.start, selection.end)
    val txt = this.text
    
    // Find sequence start line bound
    var lineStart = s
    while (lineStart > 0 && txt[lineStart - 1] != '\n') {
        lineStart--
    }

    if (s == e) {
        val newText = txt.substring(0, s) + "\n### $placeholder" + txt.substring(s)
        return TextFieldValue(newText, selection = TextRange(s + 5, s + 5 + placeholder.length))
    }
    val newText = txt.substring(0, lineStart) + "### " + txt.substring(lineStart)
    return TextFieldValue(newText, selection = TextRange(s + 4, e + 4))
}

fun TextFieldValue.toggleBullet(): TextFieldValue {
    val s = minOf(selection.start, selection.end)
    val txt = this.text
    var lineStart = s
    while (lineStart > 0 && txt[lineStart - 1] != '\n') {
        lineStart--
    }
    val newText = txt.substring(0, lineStart) + "- " + txt.substring(lineStart)
    return TextFieldValue(newText, selection = TextRange(s + 2, maxOf(selection.start, selection.end) + 2))
}

// =============================================================================
// Note Editor Screen
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(dao: NoteDao, navController: NavController, noteId: Long, sync: FirebaseSyncManager, isDoodleEnabled: Boolean) {
    val viewModel: NoteViewModel = viewModel(factory = ViewModelFactory(dao, sync))
    val notes: List<NoteEntity> by viewModel.notes.observeAsState(emptyList())
    val existingNote = notes.find { it.id == noteId }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var contentValue by remember {
        mutableStateOf(TextFieldValue(existingNote?.content ?: ""))
    }
    var tags by remember { mutableStateOf(existingNote?.tags ?: "") }
    var isShared by remember { mutableStateOf(existingNote?.isShared ?: false) }
    var selectedFont by remember { mutableStateOf(existingNote?.fontFamily ?: "Default") }
    var selectedFontSize by remember { mutableStateOf(existingNote?.fontSize ?: 16f) }
    var showTypographyMenu by remember { mutableStateOf(false) }

    LaunchedEffect(existingNote) {
        if (existingNote != null && contentValue.text.isEmpty()) {
            title = existingNote.title
            contentValue = TextFieldValue(existingNote.content)
            tags = existingNote.tags
            isShared = existingNote.isShared
            selectedFont = existingNote.fontFamily
            selectedFontSize = existingNote.fontSize
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0), // Kita handle IME manual agar TopAppBar tidak bergeser
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (noteId == 0L) "Catatan Baru".tr() else "Edit Catatan".tr(), color = CharcoalTitle)
                        Text(
                            if (isShared) "Bagikan dengan Partner".tr() else "Hanya Pribadi".tr(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isShared) SageGreen else CharcoalBody.copy(alpha = 0.5f)
                        )
                    }
                },
                actions = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        IconButton(onClick = { showTypographyMenu = !showTypographyMenu }) {
                            Icon(Icons.Default.TextFormat, contentDescription = "Typography", tint = CharcoalTitle)
                        }
                        Text("Dibagikan".tr(), style = MaterialTheme.typography.labelMedium, color = CharcoalBody)
                        Switch(checked = isShared, onCheckedChange = { isShared = it }, modifier = Modifier.scale(0.8f))
                        if (noteId != 0L) {
                            IconButton(onClick = {
                                existingNote?.let { viewModel.delete(it) }
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
            Surface(
                tonalElevation = 2.dp,
                color = SurfaceWhite,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {

                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            viewModel.insert(
                                NoteEntity(
                                    id = noteId, title = title, content = contentValue.text,
                                    tags = tags, color = 0, isShared = isShared,
                                    fontFamily = selectedFont, fontSize = selectedFontSize
                                )
                            )
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isShared) SageGreen else SoftBlue,
                            contentColor = CharcoalTitle
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) { Text("Simpan".tr()) }
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
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Title field
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(color = CharcoalTitle),
                    cursorBrush = SolidColor(SoftBlue),
                    decorationBox = { inner ->
                        Box {
                            if (title.isEmpty()) {
                                Text(
                                    "Judul Tak Bernama".tr(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = CharcoalBody.copy(alpha = 0.3f)
                                )
                            }
                            inner()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = SoftGrey.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                // Typography toolbar (font family + size)
                if (showTypographyMenu) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    TypographyToolbar(
                        selectedFont = selectedFont,
                        onFontSelected = { selectedFont = it },
                        selectedSize = selectedFontSize,
                        onSizeSelected = { selectedFontSize = it },
                        context = context
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Content editor — BasicTextField with AnnotatedString for reliable WYSIWYG
                val contextForFont = androidx.compose.ui.platform.LocalContext.current
                val baseTextStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = getFontFamily(selectedFont, contextForFont),
                    fontSize = androidx.compose.ui.unit.TextUnit(
                        selectedFontSize,
                        androidx.compose.ui.unit.TextUnitType.Sp
                    ),
                    color = CharcoalBody
                )

                BasicTextField(
                    value = contentValue,
                    onValueChange = { contentValue = it },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    textStyle = baseTextStyle,
                    cursorBrush = SolidColor(SoftBlue),
                    decorationBox = { inner ->
                        Box {
                            if (contentValue.text.isEmpty()) {
                                Text(
                                    "Mulai mengetik...".tr(),
                                    style = baseTextStyle.copy(color = CharcoalBody.copy(alpha = 0.3f))
                                )
                            }
                            inner()
                        }
                    }
                )
            }
        }
    }
}
