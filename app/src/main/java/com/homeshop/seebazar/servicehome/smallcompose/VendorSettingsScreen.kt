package com.homeshop.seebazar.servicehome.smallcompose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeshop.seebazar.servicehome.VendorUi
import com.homeshop.seebazar.ui.theme.SeebazarTheme

private data class VendorSettingsRow(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val isDestructive: Boolean = false,
)

private data class VendorSettingsSectionModel(
    val title: String,
    val rows: List<VendorSettingsRow>,
)

/** Stable ids for [VendorSettingsScreen] menu rows (for navigation / analytics). */
object VendorSettingsMenuIds {
    const val MyProfile = "my_profile"
    const val BusinessProfiles = "business_profiles"
    /** @deprecated Use [BusinessProfiles]. Kept for any legacy handlers. */
    @Deprecated("Use BusinessProfiles", ReplaceWith("VendorSettingsMenuIds.BusinessProfiles"))
    const val ShopProfile = BusinessProfiles

    const val OrdersRequests = "orders_requests"
    const val Wallet = "wallet"
    const val Notifications = "notifications"
    const val AvailabilityBusinessStatus = "availability_business_status"
    const val History = "history"

    const val HelpSupport = "help_support"
    const val Terms = "terms"
    const val PrivacyPolicy = "privacy"
    const val ShareApp = "share_app"
    const val AboutApp = "about_app"

    const val Logout = "logout"
    const val DeleteVendorAccount = "delete_vendor_account"
}

private val vendorSettingsSections = listOf(
    VendorSettingsSectionModel(
        title = "Account",
        rows = listOf(
            VendorSettingsRow(VendorSettingsMenuIds.MyProfile, "My Profile", Icons.Outlined.Person),
            VendorSettingsRow(
                VendorSettingsMenuIds.BusinessProfiles,
                "Business Profiles",
                Icons.Outlined.Storefront,
            ),
        ),
    ),
    VendorSettingsSectionModel(
        title = "Business",
        rows = listOf(
            VendorSettingsRow(
                VendorSettingsMenuIds.OrdersRequests,
                "Orders & Requests",
                Icons.Outlined.ReceiptLong,
            ),
            VendorSettingsRow(VendorSettingsMenuIds.Wallet, "Wallet", Icons.Outlined.AccountBalanceWallet),
            VendorSettingsRow(
                VendorSettingsMenuIds.Notifications,
                "Notifications",
                Icons.Outlined.Notifications,
            ),
            VendorSettingsRow(
                VendorSettingsMenuIds.AvailabilityBusinessStatus,
                "Availability / Business Status",
                Icons.Outlined.EventAvailable,
            ),
            VendorSettingsRow(VendorSettingsMenuIds.History, "History", Icons.Outlined.History),
        ),
    ),
    VendorSettingsSectionModel(
        title = "Support & Legal",
        rows = listOf(
            VendorSettingsRow(
                VendorSettingsMenuIds.HelpSupport,
                "Help & Support",
                Icons.Outlined.SupportAgent,
            ),
            VendorSettingsRow(
                VendorSettingsMenuIds.Terms,
                "Terms & Conditions",
                Icons.Outlined.Description,
            ),
            VendorSettingsRow(
                VendorSettingsMenuIds.PrivacyPolicy,
                "Privacy Policy",
                Icons.Outlined.PrivacyTip,
            ),
            VendorSettingsRow(VendorSettingsMenuIds.ShareApp, "Share App", Icons.Outlined.Share),
            VendorSettingsRow(VendorSettingsMenuIds.AboutApp, "About App", Icons.Outlined.Info),
        ),
    ),
)

private val RowTextBlack = Color(0xFF000000)

/** Parent [Scaffold] already applies status-bar padding (edge-to-edge); avoid double insets here. */
private val WindowInsetsZero = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)

private val IconTintSurface = Color(0xFFE8F2FC)

/**
 * Flat settings row: label only, no leading icon. Tap invokes [onClick].
 * Shared with user settings; kept API-stable.
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

@Composable
private fun VendorSettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title.uppercase(),
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = VendorUi.TextMuted,
        letterSpacing = 0.6.sp,
    )
}

@Composable
private fun VendorSettingsRowItem(
    row: VendorSettingsRow,
    showDividerBelow: Boolean,
    onClick: () -> Unit,
) {
    val titleColor = when {
        row.isDestructive -> Color(0xFFDC2626)
        else -> VendorUi.TextDark
    }
    val iconColor = when {
        row.isDestructive -> Color(0xFFDC2626)
        else -> VendorUi.BrandBlue
    }
    val iconBg = when {
        row.isDestructive -> Color(0xFFFEE2E2)
        else -> IconTintSurface
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = iconBg,
            ) {
                BoxWithCenteredIcon(row.icon, iconColor)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = row.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                fontWeight = FontWeight.Medium,
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = VendorUi.TextMuted.copy(alpha = 0.55f),
                modifier = Modifier.size(22.dp),
            )
        }
        if (showDividerBelow) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 68.dp),
                color = VendorUi.CardStroke,
                thickness = 1.dp,
            )
        }
    }
}

@Composable
private fun BoxWithCenteredIcon(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun VendorSettingsGroupedCard(
    rows: List<VendorSettingsRow>,
    onRowClick: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, VendorUi.CardStroke),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            rows.forEachIndexed { index, row ->
                VendorSettingsRowItem(
                    row = row,
                    showDividerBelow = index < rows.lastIndex,
                    onClick = { onRowClick(row.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onMenuItemClick: (menuId: String) -> Unit = {},
) {
    val logoutRow = VendorSettingsRow(
        id = VendorSettingsMenuIds.Logout,
        title = "Logout",
        icon = Icons.AutoMirrored.Outlined.Logout,
        isDestructive = true,
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VendorUi.ScreenBg,
        contentWindowInsets = WindowInsetsZero,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsetsZero,
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = VendorUi.TextDark,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VendorUi.TextDark,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VendorUi.TopBarBg,
                    titleContentColor = VendorUi.TextDark,
                    navigationIconContentColor = VendorUi.TextDark,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(VendorUi.ScreenBg)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            vendorSettingsSections.forEachIndexed { sectionIndex, section ->
                item {
                    VendorSettingsSectionHeader(
                        title = section.title,
                        modifier = Modifier.padding(top = if (sectionIndex == 0) 0.dp else 22.dp),
                    )
                }
                item {
                    VendorSettingsGroupedCard(
                        rows = section.rows,
                        onRowClick = onMenuItemClick,
                    )
                }
            }

            item {
                VendorSettingsSectionHeader(
                    title = "Account Actions",
                    modifier = Modifier.padding(top = 22.dp),
                )
            }
            item {
                VendorSettingsGroupedCard(
                    rows = listOf(
                        VendorSettingsRow(
                            id = VendorSettingsMenuIds.DeleteVendorAccount,
                            title = "Delete vendor account",
                            icon = Icons.Outlined.DeleteForever,
                            isDestructive = true,
                        ),
                        logoutRow,
                    ),
                    onRowClick = onMenuItemClick,
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
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
