package com.spendly.app.ui.theme

import androidx.compose.ui.graphics.Color

// Brand
val SpendlyGreen = Color(0xFF1D9E75)
val SpendlyGreenLight = Color(0xFFE1F5EE)
val SpendlyGreenDark = Color(0xFF085041)

// Expense / Error
val SpendlyRed = Color(0xFFE24B4A)
val SpendlyRedLight = Color(0xFFFCEBEB)
val SpendlyRedDark = Color(0xFF7A1F1F)

// Accent
val SpendlyAmber = Color(0xFFEF9F27)
val SpendlyAmberLight = Color(0xFFFAEEDA)
val SpendlyBlue = Color(0xFF378ADD)
val SpendlyBlueLight = Color(0xFFE6F1FB)

// Neutrals
val SpendlyGray50 = Color(0xFFF8F9FA)
val SpendlyGray100 = Color(0xFFF1F3F5)
val SpendlyGray300 = Color(0xFFDEE2E6)
val SpendlyGray500 = Color(0xFFADB5BD)
val SpendlyGray700 = Color(0xFF495057)
val SpendlyGray900 = Color(0xFF212529)

// Chart colors (7 distinct, accessible)
val ChartGreen = Color(0xFF1D9E75)
val ChartAmber = Color(0xFFEF9F27)
val ChartRed = Color(0xFFE24B4A)
val ChartBlue = Color(0xFF378ADD)
val ChartPurple = Color(0xFF7F77DD)
val ChartTeal = Color(0xFF5DCAA5)
val ChartCoral = Color(0xFFD85A30)

val ChartColors = listOf(
    ChartGreen, ChartAmber, ChartRed, ChartBlue,
    ChartPurple, ChartTeal, ChartCoral
)

// Semantic
val IncomeColor = SpendlyGreen
val ExpenseColor = SpendlyRed
val CommittedColor = SpendlyBlue
val DiscretionaryColor = SpendlyAmber
