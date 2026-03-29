package com.eostech.notepad.ui

import com.eostech.notepad.ui.theme.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorScreen(navController: NavController) {
    var passwordLength by remember { mutableStateOf(16f) }
    var includeUppercase by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }
    var generatedPassword by remember { mutableStateOf("") }
    
    val clipboardManager = LocalClipboardManager.current

    fun generate() {
        val chars = mutableListOf<Char>()
        chars.addAll('a'..'z')
        if (includeUppercase) chars.addAll('A'..'Z')
        if (includeNumbers) chars.addAll('0'..'9')
        if (includeSymbols) chars.addAll("!@#$%^&*()_+-=[]{}|;:,.<>?".toList())
        
        if (chars.isEmpty()) {
            generatedPassword = ""
            return
        }
        
        generatedPassword = (1..passwordLength.toInt())
            .map { chars[Random.nextInt(chars.size)] }
            .joinToString("")
    }

    LaunchedEffect(Unit) { generate() }
    
    val passwordStrength = remember(generatedPassword) {
        when {
            generatedPassword.length < 10 -> "Weak"
            generatedPassword.length < 16 -> "Fair"
            generatedPassword.length < 20 -> "Strong"
            else -> "Very Strong"
        }
    }
    
    val strengthColor = when(passwordStrength) {
        "Weak" -> Color(0xFFE57373)
        "Fair" -> Color(0xFFFFB74D)
        "Strong" -> SageGreen
        else -> SoftBlue
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password Generator", color = CharcoalTitle) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundLight, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = generatedPassword,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CharcoalTitle,
                                letterSpacing = 1.sp
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = strengthColor
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Strength: $passwordStrength",
                            style = MaterialTheme.typography.labelMedium,
                            color = strengthColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { generate() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftGrey, contentColor = CharcoalTitle),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("New")
                        }
                        Button(
                            onClick = { 
                                clipboardManager.setText(AnnotatedString(generatedPassword))
                                // Optional: Show toast
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftBlue, contentColor = CharcoalTitle),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Length", style = MaterialTheme.typography.titleMedium, color = CharcoalTitle)
                    Text("${passwordLength.toInt()}", style = MaterialTheme.typography.titleMedium, color = SoftBlue, fontWeight = FontWeight.Bold)
                }
                
                Slider(
                    value = passwordLength,
                    onValueChange = { passwordLength = it; generate() },
                    valueRange = 8f..32f,
                    steps = 24,
                    colors = SliderDefaults.colors(
                        thumbColor = SoftBlue,
                        activeTrackColor = SoftBlue,
                        inactiveTrackColor = SoftGrey
                    )
                )

                HorizontalDivider(color = SoftGrey.copy(alpha = 0.5f))

                ToggleOption("ABC", "Uppercase Letters", includeUppercase) { includeUppercase = it; generate() }
                ToggleOption("123", "Numbers", includeNumbers) { includeNumbers = it; generate() }
                ToggleOption("#$!", "Symbols", includeSymbols) { includeSymbols = it; generate() }
            }
        }
    }
}

@Composable
fun ToggleOption(iconText: String, label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = BackgroundLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(iconText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CharcoalBody)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = CharcoalBody)
        }
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SurfaceWhite,
                checkedTrackColor = SageGreen,
                uncheckedThumbColor = SoftGrey,
                uncheckedTrackColor = BackgroundLight
            )
        )
    }
}
