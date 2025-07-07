package java.com.serenity.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Colors - Serene Blues and Purples
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Serenity Theme Colors - Beautiful and Calming
val SerenityPrimary = Color(0xFF6B73FF) // Vibrant blue-purple
val SerenitySecondary = Color(0xFF8B5CF6) // Soft purple
val SerenityTertiary = Color(0xFFEC4899) // Pink accent
val SerenityQuaternary = Color(0xFF10B981) // Emerald green

// Background Colors
val SerenityBackground = Color(0xFFF8FAFC) // Very light blue-gray
val SerenitySurface = Color(0xFFFFFFFF) // Pure white
val SerenitySurfaceVariant = Color(0xFFF1F5F9) // Light gray-blue

// Text Colors
val SerenityOnPrimary = Color(0xFFFFFFFF)
val SerenityOnSecondary = Color(0xFFFFFFFF)
val SerenityOnSurface = Color(0xFF1E293B) // Dark slate
val SerenityOnSurfaceVariant = Color(0xFF64748B) // Medium slate

// Accent Colors
val SerenityAccent1 = Color(0xFFFF6B6B) // Coral red
val SerenityAccent2 = Color(0xFF4ECDC4) // Turquoise
val SerenityAccent3 = Color(0xFFFFE66D) // Warm yellow
val SerenityAccent4 = Color(0xFFA8E6CF) // Mint green

// Gradient Brushes
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(SerenityPrimary, SerenitySecondary)
)

val SecondaryGradient = Brush.linearGradient(
    colors = listOf(SerenitySecondary, SerenityTertiary)
)

val AccentGradient = Brush.linearGradient(
    colors = listOf(SerenityAccent1, SerenityAccent2)
)

val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(SerenityBackground, SerenitySurfaceVariant)
)

val CardGradient = Brush.linearGradient(
    colors = listOf(SerenitySurface, SerenitySurfaceVariant)
)

// Mood Colors for Journal Analysis
val HappyColor = Color(0xFFFFD93D) // Bright yellow
val CalmColor = Color(0xFF6BCF7F) // Soft green
val ExcitedColor = Color(0xFFFF6B6B) // Vibrant red
val PeacefulColor = Color(0xFF4ECDC4) // Turquoise
val MelancholyColor = Color(0xFF9B59B6) // Purple
val EnergeticColor = Color(0xFFFF8A65) // Orange

// Status Colors
val SuccessColor = Color(0xFF10B981) // Green
val WarningColor = Color(0xFFF59E0B) // Amber
val ErrorColor = Color(0xFFEF4444) // Red
val InfoColor = Color(0xFF3B82F6) // Blue

// --- Dark Theme Colors ---
val DarkAppBarBackground = Color(0xFF111827) // Very dark blue-gray (almost black)
val DarkAppBarContent = Color(0xFFF8FAFC) // Very light gray (for text/icons on app bar)
val DarkOnSurfacePrimary = Color(0xFFFFFFFF) // Pure white for main text
val DarkOnSurfaceSecondary = Color(0xFFCBD5E1) // Light gray for secondary text
val DarkOnSurfaceTertiary = Color(0xFF94A3B8) // Even lighter gray for hints/disabled