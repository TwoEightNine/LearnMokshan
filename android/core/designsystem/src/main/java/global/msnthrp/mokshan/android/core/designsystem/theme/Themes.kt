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
            tertiary = darkTertiaryLabel,
            surface = darkSurface,
            surfaceVariant = darkSurface,
            error = darkError,
            outline = darkDivider,
        )
    } else {
        lightColorScheme(
            primary = lightAccent,
            onPrimary = Color.White,
            background = lightBackground,
            onBackground = lightPrimaryLabel,
            secondary = lightSecondaryLabel,
            tertiary = lightTertiaryLabel,
            surface = lightSurface,
            surfaceVariant = lightSurface,
            error = lightError,
            outline = lightDivider,
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