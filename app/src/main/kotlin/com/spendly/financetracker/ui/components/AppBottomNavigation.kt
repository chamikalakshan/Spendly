package com.spendly.financetracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import com.spendly.financetracker.ui.viewmodel.AppTab

typealias OnTabSelected = (AppTab) -> Unit

private data class BottomNavSpec(
    val tab: AppTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun AppBottomNavigation(
    currentTab: AppTab?,
    onTabSelected: OnTabSelected,
    addMenuExpanded: Boolean,
    onAddClick: () -> Unit
) {
    val items = listOf(
        BottomNavSpec(AppTab.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavSpec(AppTab.ANALYTICS, "Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
        null,
        BottomNavSpec(AppTab.TRANSACTIONS, "History", Icons.Filled.Receipt, Icons.Outlined.Receipt),
        BottomNavSpec(AppTab.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.Transparent)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                start = 12.dp,
                top = 4.dp,
                end = 12.dp,
                bottom = SpendlySpacing.floatingNavBottomMargin
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(SpendlySpacing.floatingNavHeight)
                .zIndex(1f)
                .shadow(18.dp, RoundedCornerShape(32.dp), clip = false),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.forEach { item ->
                    if (item == null) {
                        CenterAddButton(
                            expanded = addMenuExpanded,
                            onClick = onAddClick,
                            modifier = Modifier.weight(0.82f)
                        )
                    } else {
                        FloatingNavItem(
                            item = item,
                            selected = currentTab == item.tab,
                            onClick = { onTabSelected(item.tab) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CenterAddButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.height(64.dp), contentAlignment = Alignment.Center) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(58.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 10.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add transaction",
                    tint = Color.White,
                    modifier = Modifier.size(if (expanded) 26.dp else 30.dp)
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    item: BottomNavSpec,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "bottom nav tint"
    )
    val indicatorHeight by animateDpAsState(
        targetValue = if (selected) 42.dp else 0.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "bottom nav indicator"
    )
    Box(
        modifier = modifier
            .height(60.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 3.dp)
                    .height(indicatorHeight)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f), CircleShape)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Text(
                item.label,
                style = MaterialTheme.typography.labelSmall,
                color = tint,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
