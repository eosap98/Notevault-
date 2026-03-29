package com.eostech.notepad.ui

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eostech.notepad.ui.theme.*

// Menggunakan font sistem lokal Android - tidak memerlukan internet atau sertifikat GMS
val OfficeFonts = mapOf(
    "Default" to FontFamily.Default,
    "Sans Serif" to FontFamily.SansSerif,
    "Serif" to FontFamily.Serif,
    "Monospace" to FontFamily.Monospace,
    "Cursive" to FontFamily.Cursive
)

val FontSizes = listOf(8f, 9f, 10f, 11f, 12f, 14f, 16f, 18f, 20f, 22f, 24f, 26f, 28f, 36f, 48f, 72f)

fun getCustomFonts(context: android.content.Context): Map<String, FontFamily> {
    val fontsDir = java.io.File(context.filesDir, "fonts")
    val map = mutableMapOf<String, FontFamily>()
    if (fontsDir.exists()) {
        fontsDir.listFiles()?.filter { it.extension.equals("ttf", true) || it.extension.equals("otf", true) }?.forEach { file ->
            try {
                val tf = android.graphics.Typeface.createFromFile(file)
                val composeFf = FontFamily(androidx.compose.ui.text.font.Typeface(tf))
                map[file.nameWithoutExtension] = composeFf
            } catch(e: Exception) {}
        }
    }
    return map
}

fun getFontFamily(name: String, context: android.content.Context? = null): FontFamily {
    if (context != null) {
        val customs = getCustomFonts(context)
        if (customs.containsKey(name)) return customs[name]!!
    }
    return OfficeFonts[name] ?: FontFamily.Default
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypographyToolbar(
    selectedFont: String,
    onFontSelected: (String) -> Unit,
    selectedSize: Float,
    onSizeSelected: (Float) -> Unit,
    context: android.content.Context
) {
    val allFonts = remember { OfficeFonts + getCustomFonts(context) }
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        tonalElevation = 2.dp,
        color = SurfaceWhite,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SoftGrey)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Font Name Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allFonts.keys.toList()) { fontName ->
                    FilterChip(
                        selected = selectedFont == fontName,
                        onClick = { onFontSelected(fontName) },
                        label = { Text(fontName, fontFamily = getFontFamily(fontName, context), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreen,
                            selectedLabelColor = CharcoalTitle
                        )
                    )
                }
            }
            // Size Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FontSizes) { size ->
                    FilterChip(
                        selected = selectedSize == size,
                        onClick = { onSizeSelected(size) },
                        label = { Text(size.toInt().toString()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SoftBlue,
                            selectedLabelColor = CharcoalTitle
                        )
                    )
                }
            }
        }
    }
}
