package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object ThemeState {
    var isDarkTheme by mutableStateOf(true)
        private set

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences("eradeh_theme_prefs", Context.MODE_PRIVATE)
        isDarkTheme = prefs.getBoolean("is_dark_theme", true)
    }

    fun toggleTheme(context: Context) {
        setTheme(context, !isDarkTheme)
    }

    fun setTheme(context: Context, dark: Boolean) {
        isDarkTheme = dark
        val prefs = context.getSharedPreferences("eradeh_theme_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_dark_theme", dark).apply()
    }
}

// Dynamic theme color getters for light and dark support
val AppBgColor: Color
    @Composable get() = if (ThemeState.isDarkTheme) DarkBackground else Color(0xFFF6F8FC)

val AppCardSurfaceColor: Color
    @Composable get() = if (ThemeState.isDarkTheme) DarkSurface else Color(0xFFFFFFFF)

val AppCardSurfaceElevatedColor: Color
    @Composable get() = if (ThemeState.isDarkTheme) DarkSurfaceVariant else Color(0xFFF1F5F9)

val AppTextTitleColor: Color
    @Composable get() = if (ThemeState.isDarkTheme) TextPrimary else Color(0xFF0F172A)

val AppTextSubColor: Color
    @Composable get() = if (ThemeState.isDarkTheme) TextSecondary else Color(0xFF475569)

val AppTextMutedColor: Color
    @Composable get() = if (ThemeState.isDarkTheme) TextMuted else Color(0xFF94A3B8)

val AppCardBorderColor: Color
    @Composable get() = if (ThemeState.isDarkTheme) Color(0xFF26324A) else Color(0xFFE2E8F0)
