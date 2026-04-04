package com.homeshop.seebazar.servicehome

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.R
import com.homeshop.seebazar.data.MarketplaceData
import com.homeshop.seebazar.servicehome.cardsfeature.MyProductsScreen
import com.homeshop.seebazar.servicehome.cardsfeature.MyReservationsScreen
import com.homeshop.seebazar.servicehome.cardsfeature.MyServicesScreen
import com.homeshop.seebazar.servicehome.cardsfeature.MyShop
import com.homeshop.seebazar.servicehome.smallcompose.HomeCardsVendor
import com.homeshop.seebazar.servicehome.smallcompose.HomeItemListView
import com.homeshop.seebazar.servicehome.smallcompose.HomeTopBar
import com.homeshop.seebazar.servicehome.smallcompose.VendorBottomBar
import com.homeshop.seebazar.servicehome.smallcompose.VendorSettingsMenuIds
import com.homeshop.seebazar.servicehome.smallcompose.VendorSettingsScreen
import com.homeshop.seebazar.ui.LogoutConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorHome(
    marketplace: MarketplaceData,
    onNavigateToShopDetails: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { /* preview only — no further processing */ }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
    }
    val openCameraPreview: () -> Unit = {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> cameraLauncher.launch(null)
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var openCardRoute by remember { mutableStateOf<VendorCardRoute?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productBeingEdited by remember { mutableStateOf<VendorProduct?>(null) }

    BackHandler(enabled = showAddProductDialog || openCardRoute != null || showSettings) {
        when {
            showAddProductDialog -> {
                showAddProductDialog = false
                productBeingEdited = null
            }
            openCardRoute != null -> openCardRoute = null
            showSettings -> showSettings = false
        }
    }

    Scaffold(
        bottomBar = {
            if (!showSettings) {
                VendorBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = {
                        selectedTab = it
                        openCardRoute = null
                        showSettings = false
                    },
                )
            }
        },
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)

        when (selectedTab) {
            0 -> {
                if (showSettings) {
                    VendorSettingsScreen(
                        modifier = contentModifier,
                        onBack = { showSettings = false },
                        onMenuItemClick = { id ->
                            if (id == VendorSettingsMenuIds.Logout) {
                                showLogoutDialog = true
                            }
                        },
                    )
                } else if (openCardRoute == null) {
                    Column(
                        modifier =  contentModifier.verticalScroll(rememberScrollState())
                    ) {
                        val primaryShop = marketplace.shopList.firstOrNull()
                        if (primaryShop != null) {
                            HomeTopBar(
                                shop = primaryShop,
                                serviceProfile = marketplace.serviceProfile,
                                reservationBusiness = marketplace.reservationPlaceList.firstOrNull(),
                                servicePosts = marketplace.servicePostList,
                                onProfileClick = { showSettings = true },
                                onShopCardClick = onNavigateToShopDetails,
                                onServiceCardClick = { openCardRoute = VendorCardRoute.Services },
                                onReservationCardClick = { openCardRoute = VendorCardRoute.Reservations },
                                onShareDetails = { text ->
                                    copyAndShareVendorDetails(context, text)
                                },
                            )
                            OpenShopUI(
                                shopIsOpen = primaryShop.isOpen,
                                onToggleShop = { newValue ->
                                    val list = marketplace.shopList
                                    if (list.isNotEmpty()) {
                                        list[0] = list[0].copy(isOpen = newValue)
                                    }
                                },
                                onAddPost = {
                                    productBeingEdited = null
                                    showAddProductDialog = true
                                },
                            )
                            HomeCardsVendor(
                                modifier = Modifier.fillMaxWidth(),
                                shopIsOpen = primaryShop.isOpen,
                                onCardClick = { openCardRoute = it },
                            )
                        }
                        HomeItemListView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                } else {
                    val route = openCardRoute!!
                    when (route) {
                        VendorCardRoute.Products -> MyProductsScreen(
                            modifier = contentModifier,
                            products = marketplace.productList,
                            onBack = { openCardRoute = null },
                            onEdit = {
                                productBeingEdited = it
                                showAddProductDialog = true
                            },
                            onActivate = { p ->
                                val i = marketplace.productList.indexOfFirst { it.id == p.id }
                                if (i >= 0) {
                                    marketplace.productList[i] = p.copy(isActive = true)
                                }
                            },
                            onDeactivate = { p ->
                                val i = marketplace.productList.indexOfFirst { it.id == p.id }
                                if (i >= 0) {
                                    marketplace.productList[i] = p.copy(isActive = false)
                                }
                            },
                            onDelete = { p -> marketplace.productList.remove(p) },
                        )
                        VendorCardRoute.Services -> MyServicesScreen(
                            modifier = contentModifier,
                            marketplace = marketplace,
                            onBack = { openCardRoute = null },
                        )
                        VendorCardRoute.Shop,
                        VendorCardRoute.Reservations -> Scaffold(
                            modifier = contentModifier,
                            contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                            containerColor = VendorUi.ScreenBg,
                            topBar = {
                                TopAppBar(
                                    windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                                    title = {
                                        Text(
                                            text = route.title(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = VendorUi.TextDark,
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { openCardRoute = null }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back",
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
                            val screenModifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                            when (route) {
                                VendorCardRoute.Shop -> MyShop(screenModifier)
                                VendorCardRoute.Reservations -> MyReservationsScreen(
                                    modifier = screenModifier,
                                    marketplace = marketplace,
                                )
                                VendorCardRoute.Services,
                                VendorCardRoute.Products -> Unit
                            }
                        }
                    }
                }
            }
            1 -> OrdersScreen(modifier = contentModifier)
            2 -> VendorScanPlaceholder(
                modifier = contentModifier,
                onOpenCamera = openCameraPreview,
            )
            3 -> ChatScreen(modifier = contentModifier)
            4 -> MyWallet(modifier = contentModifier)
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

    AddProductDialog(
        visible = showAddProductDialog,
        editingProduct = productBeingEdited,
        peekNextProductId = marketplace::peekNextProductId,
        takeNextProductId = marketplace::takeNextProductId,
        currentShopName = marketplace.shopList.firstOrNull()?.shopName.orEmpty(),
        onDismiss = {
            showAddProductDialog = false
            productBeingEdited = null
        },
        onSubmit = { product, isEdit ->
            if (isEdit) {
                val i = marketplace.productList.indexOfFirst { it.id == product.id }
                if (i >= 0) {
                    marketplace.productList[i] = product
                }
            } else {
                marketplace.productList.add(product)
            }
        },
    )
}

private fun copyAndShareVendorDetails(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("SeeBazar details", text))
    Toast.makeText(context, "Details copied", Toast.LENGTH_SHORT).show()
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Share via"))
}

val gradientColors = listOf(
    Color(0xFFF0F9FF),
    Color(0xFFFFFFFF),
)
@Composable
fun OpenShopUI(
    shopIsOpen: Boolean,
    onToggleShop: (Boolean) -> Unit,
    onAddPost: () -> Unit = {},
) {
    val borderColor = Color(0xFF4EB1FF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Add New Product button (takes most space)
        OutlinedButton(
            onClick = onAddPost,
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            border = BorderStroke(1.dp, borderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF4EB1FF),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = "Add New Product",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp)) // Changed to 4 to match border shape
                .background(Color.White)
                // Trigger the callback here
                .clickable { onToggleShop(!shopIsOpen) },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.moon),
                contentDescription = "Moon",
                modifier = Modifier.size(28.dp),
                // Removed the second clickable from here to avoid conflicts
                contentScale = ContentScale.Fit,
                // Optional: change alpha or color filter based on shopIsOpen to show it's working
                alpha = if (shopIsOpen) 1f else 0.5f
            )
        }
    }
}
/** Opens camera preview only; bitmap is ignored. */
@Composable
private fun VendorScanPlaceholder(
    modifier: Modifier = Modifier,
    onOpenCamera: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VendorUi.ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Icon(
            imageVector = Icons.Outlined.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = VendorUi.BrandBlue,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Scan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = VendorUi.TextDark,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Open the camera to preview only.",
            style = MaterialTheme.typography.bodyMedium,
            color = VendorUi.TextMuted,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onOpenCamera,
            colors = ButtonDefaults.buttonColors(containerColor = VendorUi.BrandBlue),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Open camera")
        }
    }
}
@Preview(showBackground = true)
@Composable
fun SeeBazarHomeScreenPreview() {
    VendorHome(marketplace = remember { MarketplaceData() })
}
