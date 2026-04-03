package com.homeshop.seebazar.userhome

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.R
import com.homeshop.seebazar.data.MarketplaceData
import com.homeshop.seebazar.servicehome.smallcompose.InsightTabItem
import com.homeshop.seebazar.ui.LogoutConfirmationDialog
import kotlinx.coroutines.delay

private val ScreenBg = Color(0xFFF8FAFC)
private val TextMuted = Color(0xFF64748B)

private enum class UserBrowseCategory {
    Products,
    Services,
    Reservations,
}

private data class UserInsightTabDef(
    val title: String,
    val iconRes: Int,
    val category: UserBrowseCategory,
)

private val userInsightTabs = listOf(
    UserInsightTabDef("Products", R.drawable.product, UserBrowseCategory.Products),
    UserInsightTabDef("Services", R.drawable.services, UserBrowseCategory.Services),
    UserInsightTabDef("Reservations", R.drawable.reservations, UserBrowseCategory.Reservations),
)

private fun UserBrowseCategory.sectionTitle(): String = when (this) {
    UserBrowseCategory.Products -> "Available products"
    UserBrowseCategory.Services -> "Available services"
    UserBrowseCategory.Reservations -> "Available reservations"
}

private val bannerIds = listOf(
    R.drawable.banner1,
    R.drawable.banner2,
    R.drawable.banner3,
    R.drawable.banner4,
    R.drawable.banner5,
    R.drawable.banner6,
    R.drawable.banner7,
    R.drawable.banner8,
)

@Composable
fun UserHome(
    marketplace: MarketplaceData,
    onLogout: () -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = showSettings) {
        showSettings = false
    }

    Scaffold(
        bottomBar = {
            if (!showSettings) {
                UserBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            }
        },
    ) { paddingValues ->
        when (selectedTab) {
            0 -> {
                if (showSettings) {
                    UserSettingsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        onBack = { showSettings = false },
                        onMenuItemClick = { id ->
                            when (id) {
                                UserSettingsMenuIds.Logout -> showLogoutDialog = true
                                else -> { /* wire screens later */ }
                            }
                        },
                    )
                } else {
                    UserHomeMainContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        marketplace = marketplace,
                        onProfileClick = { showSettings = true },
                    )
                }
            }
            1 -> UserKartScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                marketplace = marketplace,
            )
            2 -> UserPlaceholderScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                title = "Chat",
            )
            3 -> UserPlaceholderScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                title = "Order Status",
            )
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                showSettings = false
                onLogout()
            },
        )
    }
}

@Composable
private fun UserHomeMainContent(
    modifier: Modifier = Modifier,
    marketplace: MarketplaceData,
    onProfileClick: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedInsightIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        UserHomeTopBar(onProfileClick = onProfileClick)
        UserSearchBar(
            value = searchQuery,
            onValueChange = { searchQuery = it },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(ScreenBg),
        ) {
            BannerSlider()
            UserHomeInsightSection(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                marketplace = marketplace,
                selectedIndex = selectedInsightIndex,
                onTabSelected = { selectedInsightIndex = it },
            )
        }
    }
}

/** Same interaction pattern as vendor [com.homeshop.seebazar.servicehome.smallcompose.HomeItemListView]: icon + label + underline; panel below updates. */
@Composable
private fun UserHomeInsightSection(
    marketplace: MarketplaceData,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val category = userInsightTabs[selectedIndex].category

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            userInsightTabs.forEachIndexed { index, tab ->
                InsightTabItem(
                    title = tab.title,
                    iconRes = tab.iconRes,
                    selected = index == selectedIndex,
                    onClick = { onTabSelected(index) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth()
                .padding(start = 2.dp, end = 2.dp, bottom = 2.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = category.sectionTitle(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
            )
            Spacer(modifier = Modifier.height(10.dp))
            when (category) {
                UserBrowseCategory.Products -> UserBrowseProductsPanel(
                    marketplace = marketplace,
                    modifier = Modifier.weight(1f, fill = true).fillMaxWidth(),
                )
                UserBrowseCategory.Reservations -> UserBrowseReservationsPanel(
                    marketplace = marketplace,
                    modifier = Modifier.weight(1f, fill = true).fillMaxWidth(),
                )
                UserBrowseCategory.Services -> UserBrowseServicesPanel(
                    marketplace = marketplace,
                    modifier = Modifier.weight(1f, fill = true).fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun UserSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val border = Color(0xFFE8ECF4)
    val bg = Color(0xFFF7F8F9)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = {
            Text("Search 'toys'", color = TextMuted)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = TextMuted,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = bg,
            unfocusedContainerColor = bg,
            focusedBorderColor = border,
            unfocusedBorderColor = border,
        ),
    )
}

@Composable
private fun BannerSlider() {
    val pagerState = rememberPagerState(pageCount = { bannerIds.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(2_000)
            val next = (pagerState.currentPage + 1) % bannerIds.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            pageSpacing = 12.dp,
        ) { page ->
            Image(
                painter = painterResource(id = bannerIds[page]),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(bannerIds.size) { i ->
                val active = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (active) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) Color(0xFF155AC1) else Color(0xFFCBD5E1),
                        ),
                )
            }
        }
    }
}

@Composable
private fun UserPlaceholderScreen(
    modifier: Modifier = Modifier,
    title: String,
) {
    Box(
        modifier = modifier.background(ScreenBg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserHomePreview() {
    UserHome(marketplace = remember { MarketplaceData() })
}
