package com.spendly.financetracker.ui.theme

import androidx.compose.ui.graphics.Color

enum class AccentColorKey(val storageValue: String, val label: String) {
    GREEN("GREEN", "Green"),
    BLUE("BLUE", "Blue"),
    PURPLE("PURPLE", "Purple"),
    ORANGE("ORANGE", "Orange"),
    RED("RED", "Red"),
    YELLOW("YELLOW", "Yellow");

    companion object {
        fun fromStorage(value: String?): AccentColorKey =
            entries.firstOrNull { it.storageValue.equals(value, ignoreCase = true) } ?: GREEN
    }
}

data class SpendlyAccentPalette(
    val key: AccentColorKey,
    val primary: Color,
    val light: Color,
    val dark: Color,
    val gradientEnd: Color
)

fun spendlyAccentPalette(value: String?): SpendlyAccentPalette =
    when (AccentColorKey.fromStorage(value)) {
        AccentColorKey.GREEN -> SpendlyAccentPalette(AccentColorKey.GREEN, SpendlyGreen, SpendlyGreenLight, SpendlyGreenDark, Color(0xFF20B89B))
        AccentColorKey.BLUE -> SpendlyAccentPalette(AccentColorKey.BLUE, Color(0xFF2D7FF9), Color(0xFFE4EEFF), Color(0xFF0E3E89), Color(0xFF22B7D8))
        AccentColorKey.PURPLE -> SpendlyAccentPalette(AccentColorKey.PURPLE, Color(0xFF7C5CFF), Color(0xFFEDE8FF), Color(0xFF39207D), Color(0xFFB65CFF))
        AccentColorKey.ORANGE -> SpendlyAccentPalette(AccentColorKey.ORANGE, Color(0xFFF28A2E), Color(0xFFFFE9D5), Color(0xFF783B08), Color(0xFFFFB020))
        AccentColorKey.RED -> SpendlyAccentPalette(AccentColorKey.RED, SpendlyRed, SpendlyRedLight, SpendlyRedDark, Color(0xFFFF7A6E))
        AccentColorKey.YELLOW -> SpendlyAccentPalette(AccentColorKey.YELLOW, Color(0xFFE0A800), Color(0xFFFFF3C4), Color(0xFF6A4A00), Color(0xFFFFD24A))
    }
