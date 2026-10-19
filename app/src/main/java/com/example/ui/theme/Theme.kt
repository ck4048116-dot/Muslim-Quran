package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = SlateBgDark,
    primaryContainer = Emerald800,
    onPrimaryContainer = Emerald100,
    secondary = Gold400,
    onSecondary = SlateBgDark,
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Gold100,
    tertiary = Color(0xFF38BDF8),
    background = SlateBgDark,
    onBackground = CreamText,
    surface = SlateSurfaceDark,
    onSurface = CreamText,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = MutedTextDark
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald700,
    onPrimary = Color.White,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald900,
    secondary = Gold600,
    onSecondary = Color.White,
    secondaryContainer = Gold100,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = Color(0xFF0284C7),
    background = SandBgLight,
    onBackground = DarkText,
    surface = SandSurfaceLight,
    onSurface = DarkText,
    surfaceVariant = SandSurfaceVariant,
    onSurfaceVariant = MutedTextLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinctive emerald/gold Islamic palette by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
