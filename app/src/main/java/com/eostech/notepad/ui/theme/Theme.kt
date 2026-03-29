package com.eostech.notepad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SoftColorScheme = lightColorScheme(
    primary = SoftBlue,
    secondary = SageGreen,
    tertiary = SoftLavender,
    background = BackgroundLight,
    surface = SurfaceWhite,
    onPrimary = CharcoalTitle,
    onSecondary = CharcoalTitle,
    onTertiary = CharcoalTitle,
    onBackground = CharcoalBody,
    onSurface = CharcoalBody,
    error = ErrorSoft,
    onError = CharcoalTitle,
    surfaceVariant = SoftGrey,
    onSurfaceVariant = CharcoalBody,
    outline = SoftGrey
)

@Composable
fun AppNotepadTheme(
    darkTheme: Boolean = false, // Force light mode for the soft aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = SoftColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
