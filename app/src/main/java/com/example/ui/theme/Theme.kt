package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.ThemeMode

@Immutable
data class SemanticColors(
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color
)

val LocalSemanticColors = staticCompositionLocalOf {
    SemanticColors(
        success = EmeraldSuccessLight,
        successContainer = EmeraldContainerLight,
        warning = AmberTertiaryLight,
        warningContainer = AmberContainerLight
    )
}

private val LightColorScheme = lightColorScheme(
    primary = TealPrimaryLight,
    onPrimary = TealOnPrimaryLight,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = TealOnContainerLight,
    secondary = CoralSecondaryLight,
    onSecondary = CoralOnSecondaryLight,
    secondaryContainer = CoralContainerLight,
    onSecondaryContainer = CoralOnContainerLight,
    tertiary = AmberTertiaryLight,
    onTertiary = AmberOnTertiaryLight,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = AmberOnContainerLight,
    background = IvoryBackgroundLight,
    onBackground = InkOnSurfaceLight,
    surface = IvorySurfaceLight,
    onSurface = InkOnSurfaceLight,
    surfaceVariant = ClaySurfaceVariantLight,
    onSurfaceVariant = SlateMutedLight,
    outline = OutlineWarmLight,
    error = CrimsonErrorLight,
    onError = Color.White,
    errorContainer = CrimsonContainerLight,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = TealOnPrimaryDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = TealOnContainerDark,
    secondary = CoralSecondaryDark,
    onSecondary = CoralOnSecondaryDark,
    secondaryContainer = CoralContainerDark,
    onSecondaryContainer = CoralOnContainerDark,
    tertiary = AmberTertiaryDark,
    onTertiary = AmberOnTertiaryDark,
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = AmberOnContainerDark,
    background = ObsidianBackgroundDark,
    onBackground = SandOnSurfaceDark,
    surface = CharcoalSurfaceDark,
    onSurface = SandOnSurfaceDark,
    surfaceVariant = SlateSurfaceVariantDark,
    onSurfaceVariant = MistMutedDark,
    outline = OutlineDark,
    error = CrimsonErrorDark,
    onError = Color(0xFF450A0A),
    errorContainer = CrimsonContainerDark,
    onErrorContainer = Color(0xFFFECACA)
)

val HablaYaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun HablaYaTheme(
    themeMode: ThemeMode = ThemeMode.AUTO,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.AUTO -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val semanticColors = if (darkTheme) {
        SemanticColors(
            success = EmeraldSuccessDark,
            successContainer = EmeraldContainerDark,
            warning = AmberTertiaryDark,
            warningContainer = AmberContainerDark
        )
    } else {
        SemanticColors(
            success = EmeraldSuccessLight,
            successContainer = EmeraldContainerLight,
            warning = AmberTertiaryLight,
            warningContainer = AmberContainerLight
        )
    }

    CompositionLocalProvider(LocalSemanticColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HablaYaTypography,
            shapes = HablaYaShapes,
            content = content
        )
    }
}
