package java.com.serenity.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SerenityPrimary,
    onPrimary = SerenityOnPrimary,
    primaryContainer = SerenityPrimary.copy(alpha = 0.1f),
    onPrimaryContainer = SerenityPrimary,
    
    secondary = SerenitySecondary,
    onSecondary = SerenityOnSecondary,
    secondaryContainer = SerenitySecondary.copy(alpha = 0.1f),
    onSecondaryContainer = SerenitySecondary,
    
    tertiary = SerenityTertiary,
    onTertiary = SerenityOnPrimary,
    tertiaryContainer = SerenityTertiary.copy(alpha = 0.1f),
    onTertiaryContainer = SerenityTertiary,
    
    error = ErrorColor,
    onError = SerenityOnPrimary,
    errorContainer = ErrorColor.copy(alpha = 0.1f),
    onErrorContainer = ErrorColor,
    
    background = SerenityBackground,
    onBackground = SerenityOnSurface,
    surface = SerenitySurface,
    onSurface = SerenityOnSurface,
    surfaceVariant = SerenitySurfaceVariant,
    onSurfaceVariant = SerenityOnSurfaceVariant,
    
    outline = SerenityOnSurfaceVariant.copy(alpha = 0.2f),
    outlineVariant = SerenityOnSurfaceVariant.copy(alpha = 0.1f),
    
    scrim = SerenityOnSurface.copy(alpha = 0.32f),
    inverseSurface = SerenityOnSurface,
    inverseOnSurface = SerenityOnSurface,
    inversePrimary = SerenityPrimary.copy(alpha = 0.8f),
    
    surfaceDim = SerenitySurfaceVariant,
    surfaceBright = SerenitySurface,
    surfaceContainerLowest = SerenitySurface,
    surfaceContainerLow = SerenitySurfaceVariant,
    surfaceContainer = SerenitySurfaceVariant,
    surfaceContainerHigh = SerenitySurface,
    surfaceContainerHighest = SerenitySurface
)

private val DarkColorScheme = darkColorScheme(
    primary = SerenityPrimary,
    onPrimary = DarkOnSurfacePrimary,
    primaryContainer = SerenityPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = SerenityPrimary.copy(alpha = 0.9f),
    
    secondary = SerenitySecondary,
    onSecondary = DarkOnSurfacePrimary,
    secondaryContainer = SerenitySecondary.copy(alpha = 0.2f),
    onSecondaryContainer = SerenitySecondary.copy(alpha = 0.9f),
    
    tertiary = SerenityTertiary,
    onTertiary = DarkOnSurfacePrimary,
    tertiaryContainer = SerenityTertiary.copy(alpha = 0.2f),
    onTertiaryContainer = SerenityTertiary.copy(alpha = 0.9f),
    
    error = ErrorColor,
    onError = DarkOnSurfacePrimary,
    errorContainer = ErrorColor.copy(alpha = 0.2f),
    onErrorContainer = ErrorColor.copy(alpha = 0.9f),
    
    background = Color(0xFF0F172A),
    onBackground = DarkOnSurfacePrimary,
    surface = Color(0xFF1E293B),
    onSurface = DarkOnSurfacePrimary,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = DarkOnSurfaceSecondary,
    
    outline = Color(0xFF64748B).copy(alpha = 0.3f),
    outlineVariant = Color(0xFF64748B).copy(alpha = 0.2f),
    
    scrim = Color(0xFF000000).copy(alpha = 0.4f),
    inverseSurface = DarkOnSurfacePrimary,
    inverseOnSurface = Color(0xFF1E293B),
    inversePrimary = SerenityPrimary.copy(alpha = 0.8f),
    
    surfaceDim = Color(0xFF0F172A),
    surfaceBright = Color(0xFF1E293B),
    surfaceContainerLowest = Color(0xFF0F172A),
    surfaceContainerLow = Color(0xFF1E293B),
    surfaceContainer = Color(0xFF334155),
    surfaceContainerHigh = Color(0xFF475569),
    surfaceContainerHighest = Color(0xFF64748B)
)

@Composable
fun SerenityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}