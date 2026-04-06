package com.homeshop.seebazar.servicehome

import MyWalletScreen
import android.app.Activity
import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import com.homeshop.seebazar.ui.VendorDeleteAccountDialog
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.homeshop.seebazar.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.homeshop.seebazar.data.MarketplaceData
import com.homeshop.seebazar.data.UserFirestore
import com.homeshop.seebazar.data.VendorFirestoreSync
import com.homeshop.seebazar.data.VendorLocationHelper
import com.homeshop.seebazar.data.VendorLocationPrefs
import com.homeshop.seebazar.data.VendorPrefs
import com.homeshop.seebazar.servicehome.cardsfeature.CreateReservationPlaceDialog
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
import com.homeshop.seebazar.common.PaymentReceiptScreen

private enum class VendorOnboardingTarget {
    Shop,
    Service,
    Reservation,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorHome(
    marketplace: MarketplaceData,
    onNavigateToShopDetails: () -> Unit = {},
    onLogout: () -> Unit = {},
    /** After Firestore (and best-effort Auth) deletion: clear session and go to login. */
    onVendorAccountDeleted: () -> Unit = {},
) {
    val context = LocalContext.current
    val view = LocalView.current
    // Dark status-bar icons on light vendor chrome (wallet / chats / orders top bars).
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }
    val scanViewModel = viewModel<VendorScanViewModel>()
    val ordersNavController = rememberNavController()
    val receiptNavViewModel = viewModel<VendorReceiptNavViewModel>()
    val receiptOrder by scanViewModel.receiptOrder.collectAsStateWithLifecycle()
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap == null) return@rememberLauncherForActivityResult
        scanQrFromCameraBitmap(bitmap) { raw ->
            val vUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            scanViewModel.processCameraQrText(
                raw = raw,
                vendorUid = vUid,
                onUserMessage = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                },
            )
        }
    }
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
    var vendorOnboardingTarget by remember { mutableStateOf<VendorOnboardingTarget?>(null) }
    var showVendorOfferChoice by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var locationUiTick by remember { mutableIntStateOf(0) }
    val bumpVendorLocationUi: () -> Unit = { locationUiTick += 1 }

    val hasVendorProfile =
        marketplace.shopList.isNotEmpty() ||
            marketplace.serviceProfile != null ||
            marketplace.reservationPlaceList.isNotEmpty()

    fun reopenVendorOfferIfStillEmpty() {
        if (marketplace.shopList.isEmpty() &&
            marketplace.serviceProfile == null &&
            marketplace.reservationPlaceList.isEmpty()
        ) {
            showVendorOfferChoice = true
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val fineOk = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseOk = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        VendorLocationHelper.fetchAndPersist(
            context,
            fineOk || coarseOk,
            onDone = bumpVendorLocationUi,
        )
    }

    fun requestVendorLocation() {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            VendorLocationHelper.fetchAndPersist(
                context,
                true,
                onDone = bumpVendorLocationUi,
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

    LaunchedEffect(hasVendorProfile) {
        if (!hasVendorProfile) {
            showVendorOfferChoice = true
        }
    }

    val suggestedVendorId = remember {
        val u = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (u.length >= 6) "VND-${u.takeLast(6).uppercase()}" else "VND-$u"
    }

    fun persistVendor() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        VendorFirestoreSync.pushVendorMarketplace(uid, marketplace, context)
    }

    BackHandler(
        enabled = showAddProductDialog || openCardRoute != null || showSettings ||
            vendorOnboardingTarget != null ||
            (!hasVendorProfile && showVendorOfferChoice),
    ) {
        when {
            showAddProductDialog -> {
                showAddProductDialog = false
                productBeingEdited = null
            }
            !hasVendorProfile && showVendorOfferChoice -> Unit
            vendorOnboardingTarget != null -> vendorOnboardingTarget = null
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
                            when (id) {
                                VendorSettingsMenuIds.Logout -> showLogoutDialog = true
                                VendorSettingsMenuIds.DeleteVendorAccount -> showDeleteAccountDialog = true
                                else -> Unit
                            }
                        },
                    )
                } else if (openCardRoute == null) {
                    Column(
                        modifier = contentModifier
                            .background(Color.White)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        val primaryShop = marketplace.shopList.firstOrNull()
                        val accountDisplayName = VendorPrefs.cachedDisplayName(context).ifBlank {
                            FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()
                        }
                        val topLocHeadline = remember(locationUiTick) {
                            VendorLocationPrefs.city(context).ifBlank { "Your area" }
                        }
                        val topLocSub = remember(locationUiTick) {
                            VendorLocationPrefs.displaySubtitle(context)
                        }
                        val profileLetter =
                            accountDisplayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                        HomeTopBar(
                            shop = primaryShop,
                            serviceProfile = marketplace.serviceProfile,
                            reservationBusiness = marketplace.reservationPlaceList.firstOrNull(),
                            servicePosts = marketplace.servicePostList,
                            locationHeadline = topLocHeadline,
                            locationSubtitle = topLocSub,
                            profileInitial = profileLetter,
                            onLocationClick = { requestVendorLocation() },
                            onProfileClick = { showSettings = true },
                            onShopCardClick = {
                                if (primaryShop != null) onNavigateToShopDetails()
                            },
                            onServiceCardClick = { openCardRoute = VendorCardRoute.Services },
                            onReservationCardClick = { openCardRoute = VendorCardRoute.Reservations },
                            onShareDetails = { text ->
                                copyAndShareVendorDetails(context, text)
                            },
                        )
                        if (primaryShop != null) {
                            OpenShopUI(
                                shopIsOpen = primaryShop.isOpen,
                                onToggleShop = { newValue ->
                                    val list = marketplace.shopList
                                    if (list.isNotEmpty()) {
                                        list[0] = list[0].copy(isOpen = newValue)
                                        persistVendor()
                                    }
                                },
                                onAddPost = {
                                    productBeingEdited = null
                                    showAddProductDialog = true
                                },
                            )
                        }
//                        Text("Catogories", fontSize = 16.sp,fontWeight = FontWeight.Bold,modifier= Modifier.padding(start = 14.dp,top = 12.dp, bottom = 5.dp))
                        HomeCardsVendor(
                            modifier = Modifier.fillMaxWidth(),
                            onCardClick = { openCardRoute = it },
                        )
                        HomeItemListView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
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
                                persistVendor()
                            },
                            onDeactivate = { p ->
                                val i = marketplace.productList.indexOfFirst { it.id == p.id }
                                if (i >= 0) {
                                    marketplace.productList[i] = p.copy(isActive = false)
                                }
                                persistVendor()
                            },
                            onDelete = { p ->
                                marketplace.productList.remove(p)
                                persistVendor()
                            },
                        )
                        VendorCardRoute.Services -> MyServicesScreen(
                            modifier = contentModifier,
                            marketplace = marketplace,
                            onBack = { openCardRoute = null },
                            onPersistVendor = { persistVendor() },
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
                                    onPersistVendor = { persistVendor() },
                                )
                                VendorCardRoute.Services,
                                VendorCardRoute.Products -> Unit
                            }
                        }
                    }
                }
            }
            1 -> NavHost(
                navController = ordersNavController,
                startDestination = VendorOrdersRoutes.List,
                modifier = contentModifier,
            ) {
                composable(VendorOrdersRoutes.List) {
                    OrdersScreen(
                        modifier = Modifier.fillMaxSize(),
                        vendorUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
                        onScanClick = openCameraPreview,
                        onOpenReceiptForDoneOrder = { order ->
                            receiptNavViewModel.setPendingReceiptOrder(order)
                            ordersNavController.navigate(VendorOrdersRoutes.Receipt)
                        },
                    )
                }
                composable(VendorOrdersRoutes.Receipt) {
                    val order = receiptNavViewModel.pendingReceiptOrder
                    if (order == null) {
                        LaunchedEffect(Unit) {
                            ordersNavController.popBackStack()
                        }
                        Box(modifier = Modifier.fillMaxSize())
                        return@composable
                    }
                    PaymentReceiptScreen(
                        order = order,
                        onNavigateBack = {
                            // Pop first: clearing the order while still on this route recomposes with
                            // order == null and LaunchedEffect pops again → double pop → blank Orders tab.
                            ordersNavController.popBackStack()
                            receiptNavViewModel.clearPendingReceiptOrder()
                        },
                    )
                }
            }
            2 -> ChatScreen(modifier = contentModifier)
            3 -> MyWalletScreen(modifier = contentModifier)
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

    receiptOrder?.let { order ->
        VendorOrderReceiptBottomSheet(
            order = order,
            vendorUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
            onDismiss = { scanViewModel.dismissReceipt() },
            onMarkedDone = { scanViewModel.dismissReceipt() },
        )
    }

    if (showDeleteAccountDialog) {
        VendorDeleteAccountDialog(
            onDismiss = { showDeleteAccountDialog = false },
            onConfirm = {
                showDeleteAccountDialog = false
                showSettings = false
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid.isNullOrBlank()) {
                    onVendorAccountDeleted()
                } else {
                    UserFirestore.usersCollection().document(uid).delete()
                        .addOnCompleteListener { docTask ->
                            if (!docTask.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    docTask.exception?.localizedMessage ?: "Could not delete account data",
                                    Toast.LENGTH_LONG,
                                ).show()
                                return@addOnCompleteListener
                            }
                            val user = FirebaseAuth.getInstance().currentUser
                            user?.delete()?.addOnCompleteListener { authTask ->
                                if (!authTask.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        authTask.exception?.localizedMessage
                                            ?: "Cloud data removed; sign in again if login still works.",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                }
                                onVendorAccountDeleted()
                            } ?: onVendorAccountDeleted()
                        }
                }
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
            persistVendor()
        },
    )

    if (!hasVendorProfile && showVendorOfferChoice && vendorOnboardingTarget == null) {
        VendorOfferTypeDialog(
            onPickShop = {
                showVendorOfferChoice = false
                vendorOnboardingTarget = VendorOnboardingTarget.Shop
            },
            onPickService = {
                showVendorOfferChoice = false
                vendorOnboardingTarget = VendorOnboardingTarget.Service
            },
            onPickReservation = {
                showVendorOfferChoice = false
                vendorOnboardingTarget = VendorOnboardingTarget.Reservation
            },
        )
    }

    val accountNameForForms = VendorPrefs.cachedDisplayName(context).ifBlank {
        FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()
    }
    val prefAddress = VendorLocationPrefs.addressLine(context)
    val prefCity = VendorLocationPrefs.city(context)
    val prefPostal = VendorLocationPrefs.postalCode(context)
    val cachedVendorUpi = VendorPrefs.cachedVendorUpi(context)
    val initialServiceArea = listOf(prefCity, prefAddress).filter { it.isNotBlank() }.joinToString(", ").ifBlank {
        VendorLocationPrefs.displaySubtitle(context)
    }

    CreateShopAccountDialog(
        visible = vendorOnboardingTarget == VendorOnboardingTarget.Shop,
        suggestedVendorId = suggestedVendorId,
        defaultOwnerName = accountNameForForms,
        initialAddress = prefAddress,
        initialCity = prefCity,
        initialPostalCode = prefPostal,
        initialUpiId = cachedVendorUpi,
        onDismiss = {
            vendorOnboardingTarget = null
            reopenVendorOfferIfStillEmpty()
        },
        onSubmit = { shop ->
            marketplace.shopList.clear()
            marketplace.shopList.add(shop)
            persistVendor()
            vendorOnboardingTarget = null
        },
    )

    CreateServiceProfileDialog(
        visible = vendorOnboardingTarget == VendorOnboardingTarget.Service,
        peekNextProfileId = marketplace::peekNextServiceProfileId,
        takeNextProfileId = marketplace::takeNextServiceProfileId,
        defaultProviderName = accountNameForForms,
        initialServiceArea = initialServiceArea,
        initialUpiId = cachedVendorUpi,
        onDismiss = {
            vendorOnboardingTarget = null
            reopenVendorOfferIfStillEmpty()
        },
        onSubmit = { profile ->
            marketplace.serviceProfile = profile
            persistVendor()
            vendorOnboardingTarget = null
        },
    )

    CreateReservationPlaceDialog(
        visible = vendorOnboardingTarget == VendorOnboardingTarget.Reservation,
        peekNextReservationBusinessId = marketplace::peekNextReservationBusinessId,
        takeNextReservationBusinessId = marketplace::takeNextReservationBusinessId,
        defaultOwnerName = accountNameForForms,
        initialAddress = prefAddress,
        initialCity = prefCity,
        initialPostalCode = prefPostal,
        initialUpiId = cachedVendorUpi,
        onDismiss = {
            vendorOnboardingTarget = null
            reopenVendorOfferIfStillEmpty()
        },
        onSubmit = { place ->
            marketplace.reservationPlaceList.clear()
            marketplace.reservationPlaceList.add(place)
            persistVendor()
            vendorOnboardingTarget = null
        },
    )
}

@Composable
private fun VendorOfferTypeDialog(
    onPickShop: () -> Unit,
    onPickService: () -> Unit,
    onPickReservation: () -> Unit,
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
            ) {
                Text(
                    text = "What do you offer?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = VendorUi.TextDark,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Start with one. You can add the rest anytime from the home cards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VendorUi.TextMuted,
                )
                Spacer(modifier = Modifier.height(18.dp))
                VendorOfferOptionRow(
                    icon = Icons.Outlined.Store,
                    title = "Shop — sell products",
                    subtitle = "Storefront, inventory, and product posts",
                    onClick = onPickShop,
                )
                Spacer(modifier = Modifier.height(10.dp))
                VendorOfferOptionRow(
                    icon = Icons.Outlined.HomeRepairService,
                    title = "Services on demand",
                    subtitle = "List what you do and your rates",
                    onClick = onPickService,
                )
                Spacer(modifier = Modifier.height(10.dp))
                VendorOfferOptionRow(
                    icon = Icons.Outlined.CalendarMonth,
                    title = "Reservations",
                    subtitle = "Tables, appointments, and time slots",
                    onClick = onPickReservation,
                )
            }
        }
    }
}

@Composable
private fun VendorOfferOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF8FAFC),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VendorUi.BrandBlue,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(26.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = VendorUi.TextDark,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = VendorUi.TextMuted,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = VendorUi.TextMuted,
                modifier = Modifier.size(22.dp),
            )
        }
    }
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
            .background(Color(0xFF1CA1FA))
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
                containerColor = Color(0xFFFFFFFF),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
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
@Preview(showBackground = true)
@Composable
fun SeeBazarHomeScreenPreview() {
    VendorHome(marketplace = remember { MarketplaceData() })
}
