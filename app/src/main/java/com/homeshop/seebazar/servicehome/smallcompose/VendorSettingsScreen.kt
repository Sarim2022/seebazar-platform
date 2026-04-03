package com.homeshop.seebazar.servicehome.smallcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.ui.theme.SeebazarTheme

private data class SettingsMenuEntry(
    val id: String,
    val title: String,
    val isDestructive: Boolean = false,
)

/** Stable ids for [VendorSettingsScreen] menu rows (for navigation / analytics). */
object VendorSettingsMenuIds {
    const val MyProfile = "my_profile"
    const val ShopProfile = "shop_profile"
    const val OrderStatus = "order_status"
    const val History = "history"
    const val ProductDetails = "product_details"
    const val CurrentServices = "current_services"
    const val Reservations = "reservations"
    const val Wallet = "wallet"
    const val ShareApp = "share_app"
    const val PrivacyPolicy = "privacy"
    const val Terms = "terms"
    const val Logout = "logout"
}

private val vendorSettingsMenuEntries = listOf(
    SettingsMenuEntry(VendorSettingsMenuIds.MyProfile, "My Profile"),
    SettingsMenuEntry(VendorSettingsMenuIds.ShopProfile, "Shop Profile"),
    SettingsMenuEntry(VendorSettingsMenuIds.OrderStatus, "My Order Status"),
    SettingsMenuEntry(VendorSettingsMenuIds.History, "History"),
    SettingsMenuEntry(VendorSettingsMenuIds.ProductDetails, "Product Details"),
    SettingsMenuEntry(VendorSettingsMenuIds.CurrentServices, "Current Services"),
    SettingsMenuEntry(VendorSettingsMenuIds.Reservations, "Reservations"),
    SettingsMenuEntry(VendorSettingsMenuIds.Wallet, "Wallet"),
    SettingsMenuEntry(VendorSettingsMenuIds.ShareApp, "Share App"),
    SettingsMenuEntry(VendorSettingsMenuIds.PrivacyPolicy, "Privacy Policy"),
    SettingsMenuEntry(VendorSettingsMenuIds.Terms, "Terms & Conditions"),
    SettingsMenuEntry(VendorSettingsMenuIds.Logout, "Logout", isDestructive = true),
)

private val RowTextBlack = Color(0xFF000000)

/** Parent [Scaffold] already applies status-bar padding (edge-to-edge); avoid double insets here. */
private val WindowInsetsZero = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)

/**
 * Flat settings row: label only, no leading icon. Tap invokes [onClick].
 */
@Composable
fun SettingsRowItem(
    title: String,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {},
) {
    val textColor = if (isDestructive) Color(0xFFDC2626) else RowTextBlack
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            fontWeight = FontWeight.Normal,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onMenuItemClick: (menuId: String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        contentWindowInsets = WindowInsetsZero,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsetsZero,
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        color = RowTextBlack,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = RowTextBlack,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = RowTextBlack,
                    navigationIconContentColor = RowTextBlack,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(
                items = vendorSettingsMenuEntries,
                key = { it.id },
            ) { entry ->
                SettingsRowItem(
                    title = entry.title,
                    isDestructive = entry.isDestructive,
                    onClick = { onMenuItemClick(entry.id) },
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun VendorSettingsScreenFullPreview() {
    SeebazarTheme {
        VendorSettingsScreen(onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsRowItemPreview() {
    SeebazarTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsRowItem(title = "My Profile", onClick = {})
            SettingsRowItem(
                title = "Logout",
                isDestructive = true,
                onClick = {},
            )
        }
    }
}
