package com.spendly.financetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyGreenLight
import com.spendly.financetracker.ui.viewmodel.AppTab

typealias OnTabSelected = (AppTab) -> Unit

private data class BottomNavSpec(
    val tab: AppTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun AppBottomNavigation(currentTab: AppTab, onTabSelected: OnTabSelected) {
    val items = listOf(
        BottomNavSpec(AppTab.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavSpec(AppTab.TRANSACTIONS, "History", Icons.Filled.Receipt, Icons.Outlined.Receipt),
        BottomNavSpec(AppTab.ANALYTICS, "Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
        BottomNavSpec(AppTab.GOALS, "Goal", Icons.Filled.TrackChanges, Icons.Outlined.TrackChanges),
        BottomNavSpec(AppTab.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            val selected = currentTab == item.tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SpendlyGreen,
                    selectedTextColor = SpendlyGreen,
                    indicatorColor = SpendlyGreenLight,
                    unselectedIconColor = SpendlyGray500,
                    unselectedTextColor = SpendlyGray500
                )
            )
        }
    }
}
