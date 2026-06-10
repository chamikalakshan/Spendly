package com.spendly.financetracker.ui.theme

enum class ThemeMode(val storageValue: String, val label: String) {
    SYSTEM("SYSTEM", "System"),
    LIGHT("LIGHT", "Light"),
    DARK("DARK", "Dark");

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.storageValue.equals(value, ignoreCase = true) } ?: SYSTEM
    }
}
