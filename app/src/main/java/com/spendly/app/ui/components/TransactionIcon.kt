package com.spendly.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spendly.app.ui.theme.SpendlyGreen
import com.spendly.app.ui.theme.SpendlyGreenLight
import com.spendly.app.ui.theme.SpendlyRed
import com.spendly.app.ui.theme.SpendlyRedLight

@Composable
fun TransactionIcon(
    category: String,
    isIncome: Boolean,
    size: Dp = 40.dp
) {
    val icon = when (category.uppercase()) {
        "FOOD" -> Icons.Outlined.Restaurant
        "TRANSPORT", "PICKME" -> Icons.Outlined.DirectionsCar
        "RENT" -> Icons.Outlined.Home
        "SUBSCRIPTIONS", "UBEREATS" -> Icons.Outlined.Subscriptions
        "ENTERTAINMENT" -> Icons.Outlined.Movie
        "GYM" -> Icons.Outlined.FitnessCenter
        "SALARY" -> Icons.Outlined.Work
        "FREELANCE" -> Icons.Outlined.Code
        "ADSENSE" -> Icons.Outlined.Language
        "CRYPTO" -> Icons.Outlined.CurrencyBitcoin
        else -> Icons.Outlined.AccountBalanceWallet
    }

    val bgColor = if (isIncome) SpendlyGreenLight else SpendlyRedLight
    val iconColor = if (isIncome) SpendlyGreen else SpendlyRed

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
