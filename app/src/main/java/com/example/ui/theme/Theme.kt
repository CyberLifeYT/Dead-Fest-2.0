package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalTerminalTheme = staticCompositionLocalOf {
    ThemeList.first()
}

object TerminalThemes {
    val all: List<TerminalThemeConfig>
        get() = ThemeList
}

object TerminalTheme {
    val current: TerminalThemeConfig
        @Composable
        @ReadOnlyComposable
        get() = LocalTerminalTheme.current
}

@Composable
fun DeadFestAppTheme(
    themeId: String = "default",
    content: @Composable () -> Unit
) {
    val themeConfig = getTerminalTheme(themeId)

    val colorScheme = darkColorScheme(
        primary = themeConfig.primary,
        onPrimary = themeConfig.bgDark,
        primaryContainer = themeConfig.primaryDim,
        onPrimaryContainer = themeConfig.primary,
        secondary = themeConfig.secondary,
        onSecondary = themeConfig.bgDark,
        secondaryContainer = themeConfig.surface2,
        onSecondaryContainer = themeConfig.secondary,
        tertiary = themeConfig.tertiary,
        onTertiary = themeConfig.bgDark,
        background = themeConfig.bgDark,
        onBackground = themeConfig.textLight,
        surface = themeConfig.surface1,
        onSurface = themeConfig.textLight,
        surfaceVariant = themeConfig.surface2,
        onSurfaceVariant = themeConfig.textGray,
        error = themeConfig.error,
        onError = themeConfig.bgDark
    )

    CompositionLocalProvider(LocalTerminalTheme provides themeConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TerminalTypography,
            content = content
        )
    }
}
