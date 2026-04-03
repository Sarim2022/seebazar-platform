package com.homeshop.seebazar.servicehome.smallcompose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BrandBlue = Color(0xFF155AC1)
private val IndicatorBlue = Color(0xFFE3EEF9)
private val MutedGray = Color(0xFF6B7280)

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
)

private val navItems = listOf(
    BottomNavItem("Home", Icons.Outlined.Home),
    BottomNavItem("Status", Icons.Outlined.Inventory2),
    BottomNavItem("Scan", Icons.Outlined.QrCodeScanner),
    BottomNavItem("Chat", Icons.Outlined.ChatBubbleOutline),
    BottomNavItem("Wallet", Icons.Outlined.AccountBalanceWallet),
)

@Composable
fun VendorBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, clip = false)
            .clip(shape),
        containerColor = Color.White,
        tonalElevation = 0.dp,
    ) {
        navItems.forEachIndexed { index, item ->
            val selected = selectedTab == index
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandBlue,
                    selectedTextColor = BrandBlue,
                    unselectedIconColor = BrandBlue.copy(alpha = 0.42f),
                    unselectedTextColor = MutedGray,
                    indicatorColor = IndicatorBlue,
                ),
            )
        }
    }
}
