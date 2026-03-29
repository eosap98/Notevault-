package com.eostech.notepad.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eostech.notepad.ui.theme.*

@Composable
fun PinLockScreen(
    title: String = "Enter PIN",
    onPinComplete: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String = "Invalid PIN, try again",
    paddingValues: PaddingValues = PaddingValues(24.dp),
    backgroundColor: Color = BackgroundLight
) {
    var pinInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        // App Identity/Icon
        Surface(
            shape = CircleShape,
            color = SoftBlue.copy(alpha = 0.1f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(32.dp), tint = SoftBlue)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleLarge, 
                color = CharcoalTitle,
                fontWeight = FontWeight.Bold
            )
            
            if (isError) {
                Text(
                    text = errorMessage, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color(0xFFE57373),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // PIN Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                val isFilled = index < pinInput.length
                val color by animateColorAsState(
                    targetValue = if (isFilled) SoftBlue else CharcoalBody.copy(alpha = 0.1f),
                    label = "IndicatorColor"
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Numeric Pad
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "back")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { item ->
                        when (item) {
                            "" -> Spacer(modifier = Modifier.size(64.dp))
                            "back" -> {
                                IconButton(
                                    onClick = { if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1) },
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = CharcoalBody)
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            if (pinInput.length < 6) {
                                                pinInput += item
                                                if (pinInput.length == 6) {
                                                    onPinComplete(pinInput)
                                                    pinInput = "" // Clear for next try or after success
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item, 
                                        style = MaterialTheme.typography.headlineSmall, 
                                        color = CharcoalTitle,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
