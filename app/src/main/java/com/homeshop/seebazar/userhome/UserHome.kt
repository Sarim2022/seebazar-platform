package com.homeshop.seebazar.userhome

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.homeshop.seebazar.R
import com.homeshop.seebazar.data.ChatFirestore
import com.homeshop.seebazar.data.MarketplaceData
import com.homeshop.seebazar.data.OrderFirestore
import com.homeshop.seebazar.data.UserCommerceFirestore
import com.homeshop.seebazar.data.UserMarketplaceCatalog
import com.homeshop.seebazar.data.UserLocationPrefs
import com.homeshop.seebazar.data.UserProfilePrefs
import com.homeshop.seebazar.data.VendorLocationHelper
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
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var userLocationBump by remember { mutableIntStateOf(0) }
    var pendingChatRoomId by remember { mutableStateOf<String?>(null) }
    var pendingChatTitle by remember { mutableStateOf("") }

    val loginLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val fineOk = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseOk = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        VendorLocationHelper.fetchAndPersist(
            context,
            fineOk || coarseOk,
            onDone = { userLocationBump++ },
            prefsTarget = VendorLocationHelper.PrefsTarget.User,
        )
    }

    val userUid = FirebaseAuth.getInstance().currentUser?.uid
    LaunchedEffect(userUid) {
        if (userUid != null) {
            UserCommerceFirestore.loadCartAndOrders(userUid, marketplace) { }
        }
    }
    LaunchedEffect(userUid) {
        if (userUid == null) return@LaunchedEffect
        delay(600)
        val hasLoc = UserLocationPrefs.city(context).isNotBlank() ||
            UserLocationPrefs.displaySubtitle(context).isNotBlank()
        if (hasLoc) return@LaunchedEffect
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            VendorLocationHelper.fetchAndPersist(
                context,
                true,
                onDone = { userLocationBump++ },
                prefsTarget = VendorLocationHelper.PrefsTarget.User,
            )
        } else {
            loginLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }
    DisposableEffect(userUid) {
        if (userUid == null) {
            marketplace.myOrderList.clear()
            return@DisposableEffect onDispose { }
        }
        val reg = OrderFirestore.listenBuyerOrders(userUid) { orders ->
            marketplace.myOrderList.clear()
            marketplace.myOrderList.addAll(orders)
        }
        onDispose { reg.remove() }
    }

    BackHandler(enabled = showSettings) {
        showSettings = false
    }

    val startVendorChat: (String, String) -> Unit = { vendorUid, headline ->
        val buyer = FirebaseAuth.getInstance().currentUser?.uid
        if (buyer.isNullOrBlank()) {
            Toast.makeText(context, "Sign in to chat", Toast.LENGTH_SHORT).show()
        } else {
            ChatFirestore.ensureChatRoom(
                vendorUid = vendorUid,
                buyerUid = buyer,
                vendorLabelHint = headline,
                buyerLabelHint = FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()
                    .ifBlank { FirebaseAuth.getInstance().currentUser?.email.orEmpty() },
            ) { roomId, err ->
                if (err != null) {
                    Toast.makeText(
                        context,
                        err.localizedMessage ?: "Could not open chat",
                        Toast.LENGTH_LONG,
                    ).show()
                } else if (roomId != null) {
                    pendingChatRoomId = roomId
                    pendingChatTitle = headline
                    selectedTab = 2
                }
            }
        }
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
                            .fillMaxSize().background(Color.White)
                            .padding(paddingValues),
                        marketplace = marketplace,
                        onProfileClick = { showSettings = true },
                        remoteLocationBump = userLocationBump,
                        onChatWithVendor = startVendorChat,
                    )
                }
            }
            1 -> UserKartScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                marketplace = marketplace,
            )
            2 -> UserChatsScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                pendingOpenRoomId = pendingChatRoomId,
                pendingOpenTitle = pendingChatTitle,
                onConsumedPendingOpen = {
                    pendingChatRoomId = null
                    pendingChatTitle = ""
                },
            )
            3 -> UserOrderStatusScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                marketplace = marketplace,
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

/** Interpolates each gradient stop when switching Products / Services / Reservations. */
@Composable
private fun animatedInsightGradientColors(
    selectedInsightIndex: Int,
    product: List<Color>,
    service: List<Color>,
    reservation: List<Color>,
    animationSpec: FiniteAnimationSpec<Color> = tween(
        durationMillis = 500,
        easing = FastOutSlowInEasing,
    ),
): List<Color> {
    fun targetStop(i: Int): Color = when (selectedInsightIndex) {
        0 -> product[i]
        1 -> service[i]
        else -> reservation[i]
    }
    val s0 by animateColorAsState(targetStop(0), animationSpec, label = "insightG0")
    val s1 by animateColorAsState(targetStop(1), animationSpec, label = "insightG1")
    val s2 by animateColorAsState(targetStop(2), animationSpec, label = "insightG2")
    val s3 by animateColorAsState(targetStop(3), animationSpec, label = "insightG3")
    val s4 by animateColorAsState(targetStop(4), animationSpec, label = "insightG4")
    val s5 by animateColorAsState(targetStop(5), animationSpec, label = "insightG5")
    return listOf(s0, s1, s2, s3, s4, s5)
}

@Composable
private fun UserHomeMainContent(
    modifier: Modifier = Modifier,
    marketplace: MarketplaceData,
    onProfileClick: () -> Unit,
    remoteLocationBump: Int = 0,
    onChatWithVendor: (vendorUid: String, headline: String) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val catalogUid = FirebaseAuth.getInstance().currentUser?.uid
    LaunchedEffect(catalogUid) {
        if (catalogUid != null) {
            UserMarketplaceCatalog.refreshFromFirestore(marketplace) { _ -> }
        }
    }
    var locationRefreshTick by remember { mutableIntStateOf(0) }
    val bumpLocationUi: () -> Unit = { locationRefreshTick += 1 }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val fineOk = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseOk = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        VendorLocationHelper.fetchAndPersist(
            context,
            fineOk || coarseOk,
            onDone = bumpLocationUi,
            prefsTarget = VendorLocationHelper.PrefsTarget.User,
        )
    }
    val requestUserLocation: () -> Unit = {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            VendorLocationHelper.fetchAndPersist(
                context,
                true,
                onDone = bumpLocationUi,
                prefsTarget = VendorLocationHelper.PrefsTarget.User,
            )
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }
    val userLocHeadline = remember(locationRefreshTick, remoteLocationBump) {
        UserLocationPrefs.city(context).ifBlank { "Home" }
    }
    val userLocSubtitle = remember(locationRefreshTick, remoteLocationBump) {
        UserLocationPrefs.displaySubtitle(context)
    }
    val accountDisplayName = UserProfilePrefs.cachedDisplayName(context).ifBlank {
        FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()
    }
    val userProfileLetter =
        accountDisplayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    var showUserSearch by remember { mutableStateOf(false) }
    var selectedInsightIndex by remember { mutableIntStateOf(0) }

    if (showUserSearch) {
        UserSearchScreen(
            marketplace = marketplace,
            onBack = { showUserSearch = false },
            modifier = modifier,
            onChatWithVendor = { uid, title ->
                showUserSearch = false
                onChatWithVendor(uid, title)
            },
        )
        return
    }

    // Six stops; last is Color.White so the fade matches tab + panel backgrounds below.
    val productGradient = listOf(
        Color(0xFF2781F5),
        Color(0xFF7CB2F8),
        Color(0xFF9FC4FA),
        Color(0xFFC8E6FD),
        Color(0xFFE3F2FD),
        Color.White,
    )
    val serviceGradient = listOf(
        Color(0xFF16A34A),
        Color(0xFF4ADE80),
        Color(0xFF86EFAC),
        Color(0xFFBBF7D0),
        Color(0xFFDCFCE7),
        Color.White,
    )
    val reservationGradient = listOf(
        Color(0xFFEAB308),
        Color(0xFFFACC15),
        Color(0xFFFDE047),
        Color(0xFFFEF08A),
        Color(0xFFFEF9C3),
        Color.White,
    )
    val gradientColors = animatedInsightGradientColors(
        selectedInsightIndex = selectedInsightIndex,
        product = productGradient,
        service = serviceGradient,
        reservation = reservationGradient,
    )

    // Single scroll: header (gradient + top bar + search + banner) moves with the insight tabs
    // and list — not fixed while only "Available …" scrolls.
    val homeScroll = rememberScrollState()
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(homeScroll),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.verticalGradient(colors = gradientColors)),
            ) {
                UserHomeTopBar(
                    locationHeadline = userLocHeadline,
                    locationSubtitle = userLocSubtitle,
                    profileInitial = userProfileLetter,
                    onLocationClick = requestUserLocation,
                    onProfileClick = onProfileClick,
                )
                UserSearchBar(
                    onClick = { showUserSearch = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                BannerSlider()
            }
            UserHomeInsightSection(
                modifier = Modifier.fillMaxWidth(),
                marketplace = marketplace,
                selectedIndex = selectedInsightIndex,
                onTabSelected = { selectedInsightIndex = it },
                onChatWithVendor = onChatWithVendor,
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
    onChatWithVendor: (vendorUid: String, headline: String) -> Unit = { _, _ -> },
) {
    val category = userInsightTabs[selectedIndex].category

    Column(modifier = modifier.fillMaxWidth()) {
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
                    modifier = Modifier.fillMaxWidth(),
                )
                UserBrowseCategory.Reservations -> UserBrowseReservationsPanel(
                    marketplace = marketplace,
                    modifier = Modifier.fillMaxWidth(),
                )
                UserBrowseCategory.Services -> UserBrowseServicesPanel(
                    marketplace = marketplace,
                    modifier = Modifier.fillMaxWidth(),
                    onChatWithVendor = onChatWithVendor,
                )
            }
        }
    }
}

@Composable
private fun UserSearchBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val border = Color(0xFFE8ECF4)
    val bg = Color(0xFFF7F8F9)

    // 1. Define your list of items
    val searchItems = listOf("Milk", "Electrician", "Drink", "Wheat","Fruits","Electrician","Medicine","Library")
    var currentIndex by remember { mutableIntStateOf(0) }

    // 2. Timer Logic: Change index every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000) // 2 seconds
            currentIndex = (currentIndex + 1) % searchItems.size
        }
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color.Gray, // Replaced TextMuted with Gray for standalone code
            )
            Spacer(modifier = Modifier.width(12.dp))

            // 3. Layout for "Search by" + Dynamic Text
            Row {
                Text(
                    text = "Search by ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                )

                // 4. Animation for the changing word
                AnimatedContent(
                    targetState = searchItems[currentIndex],
                    transitionSpec = {
                        // Fade in and slide up, Fade out and slide up
                        (fadeIn(animationSpec = tween(500)) + slideInVertically { it })
                            .togetherWith(fadeOut(animationSpec = tween(500)) + slideOutVertically { -it })
                    },
                    label = "SearchTextAnimation"
                ) { targetWord ->
                    Text(
                        text = targetWord,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                    )
                }
            }
        }
    }
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
