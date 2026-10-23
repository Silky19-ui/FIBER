package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FiberCyanPrimaryDark,
    onPrimary = FiberCyanOnPrimaryDark,
    primaryContainer = FiberCyanContainerDark,
    onPrimaryContainer = FiberCyanOnContainerDark,
    secondary = FiberBlueSecondaryDark,
    onSecondary = FiberBlueOnSecondaryDark,
    secondaryContainer = FiberBlueContainerDark,
    onSecondaryContainer = FiberBlueOnContainerDark,
    tertiary = FiberAmberTertiaryDark,
    onTertiary = FiberAmberOnTertiaryDark,
    tertiaryContainer = FiberAmberContainerDark,
    onTertiaryContainer = FiberAmberOnContainerDark,
    background = FiberBackgroundDark,
    surface = FiberSurfaceDark,
    surfaceVariant = FiberSurfaceVariantDark,
    onBackground = FiberOnBackgroundDark,
    onSurface = FiberOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = FiberCyanPrimary,
    onPrimary = FiberCyanOnPrimary,
    primaryContainer = FiberCyanContainer,
    onPrimaryContainer = FiberCyanOnContainer,
    secondary = FiberBlueSecondary,
    onSecondary = FiberBlueOnSecondary,
    secondaryContainer = FiberBlueContainer,
    onSecondaryContainer = FiberBlueOnContainer,
    tertiary = FiberAmberTertiary,
    onTertiary = FiberAmberOnTertiary,
    tertiaryContainer = FiberAmberContainer,
    onTertiaryContainer = FiberAmberOnContainer,
    background = FiberBackgroundLight,
    surface = FiberSurfaceLight,
    surfaceVariant = FiberSurfaceVariantLight,
    onBackground = FiberOnBackgroundLight,
    onSurface = FiberOnSurfaceLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded telecom colors consistent
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
