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
    primary = M3PrimaryDark,
    onPrimary = M3OnPrimaryDark,
    primaryContainer = M3PrimaryContainerDark,
    onPrimaryContainer = M3OnPrimaryContainerDark,
    secondary = M3SecondaryDark,
    onSecondary = M3OnSecondaryDark,
    secondaryContainer = M3SecondaryContainerDark,
    onSecondaryContainer = M3OnSecondaryContainerDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    tertiary = PinGoldLight,
    tertiaryContainer = Color(0xFF78350F),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = M3PrimaryLight,
    onPrimary = M3OnPrimaryLight,
    primaryContainer = M3PrimaryContainerLight,
    onPrimaryContainer = M3OnPrimaryContainerLight,
    secondary = M3SecondaryLight,
    onSecondary = M3OnSecondaryLight,
    secondaryContainer = M3SecondaryContainerLight,
    onSecondaryContainer = M3OnSecondaryContainerLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    tertiary = PinGold,
    tertiaryContainer = PinGoldContainer,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
