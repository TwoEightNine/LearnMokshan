package global.msnthrp.mokshan.android.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


internal fun LeMokColorScheme(
    isDarkTheme: Boolean,
): ColorScheme {
    return if (isDarkTheme) {
        darkColorScheme(
            primary = darkAccent,
            onPrimary = Color.White,
            background = darkBackground,
            onBackground = darkPrimaryLabel,
            secondary = darkSecondaryLabel,
            onSecondary = Color.Black,
            tertiary = darkTertiaryLabel,
            onTertiary = Color.Black,
            surface = darkSurface,
            onSurface = darkPrimaryLabel,
            surfaceVariant = darkSurfaceAccent,
            onSurfaceVariant = darkSecondaryLabel,
            surfaceContainer = darkSurface,
            error = darkError,
            onError = Color.White,
            outline = darkDivider,
            outlineVariant = darkDivider,

            primaryContainer = darkSurface,
            onPrimaryContainer = darkPrimaryLabel,
            secondaryContainer = darkSurfaceVariant,
            onSecondaryContainer = darkPrimaryLabel,
            tertiaryContainer = darkSurfaceVariant,
            onTertiaryContainer = darkPrimaryLabel,

            inverseSurface = lightSurface,
            inverseOnSurface = lightPrimaryLabel,
            inversePrimary = lightAccent,

            surfaceTint = darkSurface,
            surfaceBright = darkSurface,
            surfaceContainerLow = darkSurface,
            surfaceContainerHigh = darkSurface,
            surfaceContainerLowest = darkSurface,
            surfaceContainerHighest = darkSurface,
            surfaceDim = darkSurfaceDim,

            scrim = Color.White,
        )
    } else {
        lightColorScheme(
            primary = lightAccent,
            onPrimary = Color.White,
            background = lightBackground,
            onBackground = lightPrimaryLabel,
            secondary = lightSecondaryLabel,
            onSecondary = Color.White,
            tertiary = lightTertiaryLabel,
            onTertiary = Color.White,
            surface = lightSurface,
            onSurface = lightPrimaryLabel,
            surfaceVariant = lightSurfaceAccent,
            onSurfaceVariant = lightSecondaryLabel,
            surfaceContainer = lightSurface,
            error = lightError,
            onError = Color.White,
            errorContainer = lightError,
            onErrorContainer = Color.White,
            outline = lightDivider,
            outlineVariant = lightDivider,

            primaryContainer = lightSurface,
            onPrimaryContainer = lightPrimaryLabel,
            secondaryContainer = lightSurfaceVariant,
            onSecondaryContainer = lightPrimaryLabel,
            tertiaryContainer = lightSurfaceVariant,
            onTertiaryContainer = lightPrimaryLabel,

            inverseSurface = darkSurface,
            inverseOnSurface = darkPrimaryLabel,
            inversePrimary = darkAccent,

            surfaceTint = lightSurface,
            surfaceBright = lightSurface,
            surfaceContainerLow = lightSurface,
            surfaceContainerHigh = lightSurface,
            surfaceContainerLowest = lightSurface,
            surfaceContainerHighest = lightSurface,
            surfaceDim = lightSurfaceDim,

            scrim = Color.Black,
        )
    }
}


@Composable
fun LeMokTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LeMokColorScheme(isDarkTheme),
        typography = typography,
        content = content,
    )
}

object SpecialColors {

    val correctGreenSurface: Color
        @Composable
        get() = if (isSystemInDarkTheme()) darkGreenPale else lightGreenPale

    val correctGreen: Color
        @Composable
        get() = if (isSystemInDarkTheme()) darkGreen else lightGreen

    val incorrectRedSurface: Color
        @Composable
        get() = if (isSystemInDarkTheme()) darkRedPale else lightRedPale

    val incorrectRed: Color
        @Composable
        get() = if (isSystemInDarkTheme()) darkRed else lightRed
}