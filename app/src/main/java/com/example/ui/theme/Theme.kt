package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BlushRosePrimary,
    onPrimary = BlushRoseOnPrimary,
    primaryContainer = BlushRosePrimaryContainer,
    onPrimaryContainer = BlushRoseOnPrimaryContainer,
    secondary = PeachRoseSecondary,
    secondaryContainer = PeachRoseSecondaryContainer,
    onSecondaryContainer = PeachRoseOnSecondaryContainer,
    tertiary = ChampagneGoldTertiary,
    tertiaryContainer = ChampagneGoldTertiaryContainer,
    onTertiaryContainer = ChampagneGoldOnTertiaryContainer,
    background = IvoryBackground,
    surface = IvorySurface,
    surfaceVariant = IvorySurfaceVariant,
    onBackground = BerryCharcoalText,
    onSurface = BerryCharcoalText,
    onSurfaceVariant = CashmereTextSubtle,
    outline = DelicatePetalBorder,
    outlineVariant = SoftRoseDivider
)

private val DarkColorScheme = darkColorScheme(
    primary = VelvetPlumPrimary,
    onPrimary = VelvetPlumOnPrimary,
    primaryContainer = VelvetPlumPrimaryContainer,
    onPrimaryContainer = VelvetPlumOnPrimaryContainer,
    secondary = PeachRoseSecondary,
    secondaryContainer = VelvetPlumSurfaceVariant,
    tertiary = ChampagneGoldTertiary,
    background = VelvetPlumBackground,
    surface = VelvetPlumSurface,
    surfaceVariant = VelvetPlumSurfaceVariant,
    onBackground = VelvetPlumText,
    onSurface = VelvetPlumText,
    onSurfaceVariant = VelvetPlumTextSubtle,
    outline = VelvetPlumOutline
)

@Composable
fun SkinRoutineTheme(
    darkTheme: Boolean = false, // Always default to luminous, chic beauty aesthetic
    dynamicColor: Boolean = false, // Keep signature beauty palette consistent
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
