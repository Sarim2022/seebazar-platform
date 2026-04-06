package com.homeshop.seebazar.servicehome.cardsfeature

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.homeshop.seebazar.data.MarketplaceData
import com.homeshop.seebazar.data.VendorLocationPrefs
import com.homeshop.seebazar.data.VendorPrefs
import com.homeshop.seebazar.servicehome.AddServiceDialog
import com.homeshop.seebazar.servicehome.CreateServiceProfileDialog
import com.homeshop.seebazar.servicehome.ServiceChargesType
import com.homeshop.seebazar.servicehome.ServiceProfession
import com.homeshop.seebazar.servicehome.VendorServicePost
import com.homeshop.seebazar.servicehome.VendorServiceProfile
import com.homeshop.seebazar.servicehome.VendorUi
import com.homeshop.seebazar.ui.rememberDecodedBitmap

private val MrpGreen = Color(0xFF15803D)
private val ActiveBg = Color(0xFFDCFCE7)
private val ActiveText = Color(0xFF166534)
private val InactiveBg = Color(0xFFE2E8F0)
private val InactiveText = Color(0xFF475569)
private val ProfessionBadgeBg = Color(0xFFE0F2FE)
private val ProfessionBadgeFg = Color(0xFF0369A1)

private val WindowInsetsZero = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyServicesScreen(
    modifier: Modifier = Modifier,
    marketplace: MarketplaceData,
    onBack: () -> Unit,
    onPersistVendor: () -> Unit = {},
) {
    var showCreateProfile by remember { mutableStateOf(false) }
    var showAddService by remember { mutableStateOf(false) }
    var editingPost by remember { mutableStateOf<VendorServicePost?>(null) }
    var showProfileDetails by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val accountNameForService = VendorPrefs.cachedDisplayName(context).ifBlank {
        FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()
    }
    val initialServiceAreaForDialog = run {
        val pc = VendorLocationPrefs.city(context)
        val pa = VendorLocationPrefs.addressLine(context)
        listOf(pc, pa).filter { it.isNotBlank() }.joinToString(", ").ifBlank {
            VendorLocationPrefs.displaySubtitle(context)
        }
    }

    BackHandler(enabled = showProfileDetails || showCreateProfile || showAddService) {
        when {
            showProfileDetails -> showProfileDetails = false
            showAddService -> {
                showAddService = false
                editingPost = null
            }
            showCreateProfile -> showCreateProfile = false
        }
    }

    if (showProfileDetails && marketplace.serviceProfile != null) {
        ServiceProfileDetailsScreen(
            modifier = modifier.fillMaxSize(),
            profile = marketplace.serviceProfile!!,
            onBack = { showProfileDetails = false },
            onSave = {
                marketplace.serviceProfile = it
                onPersistVendor()
            },
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = VendorUi.ScreenBg,
            contentWindowInsets = WindowInsetsZero,
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsetsZero,
                    title = {
                        Text(
                            text = "My Services",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = VendorUi.TextDark,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
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
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

            val profile = marketplace.serviceProfile
            if (profile == null) {
                MyServicesEmptyState(
                    modifier = contentModifier,
                    onCreateProfile = { showCreateProfile = true },
                )
            } else {
                MyServicesContent(
                    modifier = contentModifier,
                    profile = profile,
                    posts = marketplace.servicePostList,
                    onProfileCardClick = { showProfileDetails = true },
                    onAddService = {
                        editingPost = null
                        showAddService = true
                    },
                    onEditService = { post ->
                        editingPost = post
                        showAddService = true
                    },
                    onActivate = { p ->
                        val i = marketplace.servicePostList.indexOfFirst { it.id == p.id }
                        if (i >= 0) marketplace.servicePostList[i] = p.copy(isActive = true)
                        onPersistVendor()
                    },
                    onDeactivate = { p ->
                        val i = marketplace.servicePostList.indexOfFirst { it.id == p.id }
                        if (i >= 0) marketplace.servicePostList[i] = p.copy(isActive = false)
                        onPersistVendor()
                    },
                    onProfileAvailabilityChange = { available ->
                        marketplace.serviceProfile?.let { cur ->
                            marketplace.serviceProfile = cur.copy(isAvailable = available)
                            onPersistVendor()
                        }
                    },
                )
            }
        }
    }

    CreateServiceProfileDialog(
        visible = showCreateProfile,
        peekNextProfileId = marketplace::peekNextServiceProfileId,
        takeNextProfileId = marketplace::takeNextServiceProfileId,
        defaultProviderName = accountNameForService,
        initialServiceArea = initialServiceAreaForDialog,
        initialUpiId = VendorPrefs.cachedVendorUpi(context),
        onDismiss = { showCreateProfile = false },
        onSubmit = {
            marketplace.serviceProfile = it
            onPersistVendor()
        },
    )

    AddServiceDialog(
        visible = showAddService,
        editingPost = editingPost,
        peekNextPostId = marketplace::peekNextServicePostId,
        takeNextPostId = marketplace::takeNextServicePostId,
        onDismiss = {
            showAddService = false
            editingPost = null
        },
        onSubmit = { post, isEdit ->
            if (isEdit) {
                val i = marketplace.servicePostList.indexOfFirst { it.id == post.id }
                if (i >= 0) marketplace.servicePostList[i] = post
            } else {
                marketplace.servicePostList.add(post)
            }
            onPersistVendor()
        },
    )
}

@Composable
private fun MyServicesEmptyState(
    modifier: Modifier = Modifier,
    onCreateProfile: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(VendorUi.ScreenBg)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No service profile yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = VendorUi.TextDark,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create your service provider profile to start posting services",
            style = MaterialTheme.typography.bodyMedium,
            color = VendorUi.TextMuted,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onCreateProfile,
            colors = ButtonDefaults.buttonColors(containerColor = VendorUi.BrandBlue),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Create Service Profile")
        }
    }
}

@Composable
private fun MyServicesContent(
    modifier: Modifier = Modifier,
    profile: VendorServiceProfile,
    posts: List<VendorServicePost>,
    onProfileCardClick: () -> Unit,
    onAddService: () -> Unit,
    onEditService: (VendorServicePost) -> Unit,
    onActivate: (VendorServicePost) -> Unit,
    onDeactivate: (VendorServicePost) -> Unit,
    onProfileAvailabilityChange: (Boolean) -> Unit,
) {
    LazyColumn(
        modifier = modifier.background(VendorUi.ScreenBg),
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ServiceProfileSummaryCard(
                profile = profile,
                onClick = onProfileCardClick,
            )
        }
        if (posts.isEmpty()) {
            item {
                PrimaryServiceActionColumn(
                    profile = profile,
                    post = null,
                    onActivate = { },
                    onDeactivate = { },
                    onEditService = onAddService,
                    onProfileAvailabilityChange = onProfileAvailabilityChange,
                )
            }
        } else {
            items(posts, key = { it.id }) { post ->
                VendorServicePrimaryCard(
                    post = post,
                    profile = profile,
                    onActivate = { onActivate(post) },
                    onDeactivate = { onDeactivate(post) },
                    onEditService = { onEditService(post) },
                    onProfileAvailabilityChange = null,
                )
            }
            item {
                OutlinedButton(
                    onClick = onAddService,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, VendorUi.BrandBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VendorUi.BrandBlue),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        "Add another service",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceProfileSummaryCard(
    profile: VendorServiceProfile,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, VendorUi.CardStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            ServiceThumb(uriString = profile.imageUri)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.providerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = VendorUi.TextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                ProfessionChip(profession = profile.profession)
                Spacer(modifier = Modifier.height(6.dp))
                DetailLine("Profile ID", profile.id)
                DetailLine("Experience", "${profile.experienceYears} yrs")
                DetailLine("Area", profile.serviceArea)
                DetailLine("Contact", profile.contactNumber)
                DetailLine(
                    "Charge",
                    "${profile.baseCharge} · ${profile.chargesType.displayLabel}",
                )
                Spacer(modifier = Modifier.height(4.dp))
                AvailabilityChip(isAvailable = profile.isAvailable)
            }
        }
    }
}

@Composable
private fun ProfessionChip(profession: ServiceProfession) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ProfessionBadgeBg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = profession.displayLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = ProfessionBadgeFg,
        )
    }
}

@Composable
private fun AvailabilityChip(isAvailable: Boolean) {
    val bg = if (isAvailable) ActiveBg else InactiveBg
    val fg = if (isAvailable) ActiveText else InactiveText
    val label = if (isAvailable) "Available" else "Busy"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = fg,
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodySmall,
        color = VendorUi.TextMuted,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun VendorServicePrimaryCard(
    post: VendorServicePost,
    profile: VendorServiceProfile,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onEditService: () -> Unit,
    onProfileAvailabilityChange: ((Boolean) -> Unit)?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, VendorUi.CardStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                ServiceThumb(uriString = post.imageUri)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = VendorUi.TextDark,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusChip(isActive = post.isActive)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = post.id,
                        style = MaterialTheme.typography.labelSmall,
                        color = VendorUi.TextMuted,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = post.category.displayLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = VendorUi.BrandBlue,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = post.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = VendorUi.TextDark,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Price: ${post.price}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MrpGreen,
                    )
                    Text(
                        text = "Est. time: ${post.estimatedTime}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VendorUi.TextMuted,
                    )
                    Text(
                        text = "Emergency: ${if (post.emergencyAvailable) "Yes" else "No"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VendorUi.TextDark,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryServiceActionColumn(
                profile = profile,
                post = post,
                onActivate = onActivate,
                onDeactivate = onDeactivate,
                onEditService = onEditService,
                onProfileAvailabilityChange = onProfileAvailabilityChange,
            )
        }
    }
}

@Composable
private fun PrimaryServiceActionColumn(
    profile: VendorServiceProfile,
    post: VendorServicePost?,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onEditService: () -> Unit,
    onProfileAvailabilityChange: ((Boolean) -> Unit)?,
) {
    val context = LocalContext.current
    val hasPost = post != null
    val workActive = if (hasPost) post!!.isActive else profile.isAvailable
    val canToggleWork = hasPost || onProfileAvailabilityChange != null
    val setWorkActive: (Boolean) -> Unit = { want ->
        if (hasPost) {
            if (want) onActivate() else onDeactivate()
        } else {
            onProfileAvailabilityChange?.invoke(want)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (!canToggleWork) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, VendorUi.CardStroke),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        "Active to work",
                        style = MaterialTheme.typography.labelLarge,
                        color = VendorUi.TextMuted,
                    )
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, VendorUi.CardStroke),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        "OFF work",
                        style = MaterialTheme.typography.labelLarge,
                        color = VendorUi.TextMuted,
                    )
                }
            } else if (workActive) {
                Button(
                    onClick = { setWorkActive(true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VendorUi.BrandBlue),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        "Active to work",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                OutlinedButton(
                    onClick = { setWorkActive(false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, VendorUi.CardStroke),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        "OFF work",
                        style = MaterialTheme.typography.labelLarge,
                        color = VendorUi.TextDark,
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { setWorkActive(true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, VendorUi.BrandBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        "Active to work",
                        style = MaterialTheme.typography.labelLarge,
                        color = VendorUi.BrandBlue,
                    )
                }
                Button(
                    onClick = { setWorkActive(false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InactiveBg,
                        contentColor = InactiveText,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        "OFF work",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        OutlinedButton(
            onClick = onEditService,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, VendorUi.BrandBlue),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VendorUi.BrandBlue),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                if (hasPost) "Edit Service" else "Add / edit service",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }
        OutlinedButton(
            onClick = {
                val raw = profile.contactNumber.trim()
                if (raw.isNotEmpty()) {
                    context.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(raw)}")),
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = profile.contactNumber.isNotBlank() &&
                (if (hasPost) post!!.isActive else profile.isAvailable),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, VendorUi.CardStroke),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = VendorUi.TextDark,
                disabledContentColor = VendorUi.TextMuted,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Phone,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Call Active",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun StatusChip(isActive: Boolean) {
    val bg = if (isActive) ActiveBg else InactiveBg
    val fg = if (isActive) ActiveText else InactiveText
    val label = if (isActive) "Active" else "Inactive"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = fg,
        )
    }
}

@Composable
private fun ServiceThumb(uriString: String?) {
    val bitmap = rememberDecodedBitmap(uriString)
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(VendorUi.ScreenBg),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = "No img",
                style = MaterialTheme.typography.labelSmall,
                color = VendorUi.TextMuted,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceProfileDetailsScreen(
    modifier: Modifier = Modifier,
    profile: VendorServiceProfile,
    onBack: () -> Unit,
    onSave: (VendorServiceProfile) -> Unit,
) {
    var providerName by remember(profile.id) { mutableStateOf(profile.providerName) }
    var profession by remember(profile.id) { mutableStateOf(profile.profession) }
    var experienceYears by remember(profile.id) { mutableStateOf(profile.experienceYears) }
    var serviceArea by remember(profile.id) { mutableStateOf(profile.serviceArea) }
    var contactNumber by remember(profile.id) { mutableStateOf(profile.contactNumber) }
    var shortDescription by remember(profile.id) { mutableStateOf(profile.shortDescription) }
    var chargesType by remember(profile.id) { mutableStateOf(profile.chargesType) }
    var baseCharge by remember(profile.id) { mutableStateOf(profile.baseCharge) }
    var upiId by remember(profile.id) { mutableStateOf(profile.upiId) }
    var isAvailable by remember(profile.id) { mutableStateOf(profile.isAvailable) }
    var professionMenuExpanded by remember { mutableStateOf(false) }
    var chargesMenuExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var imageUri by remember(profile.id) { mutableStateOf(profile.imageUri) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        imageUri = uri?.toString()
    }

    LaunchedEffect(profile.id) {
        imageUri = profile.imageUri
        upiId = profile.upiId
    }

    val fieldShape = RoundedCornerShape(12.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = VendorUi.BrandBlue,
        focusedLabelColor = VendorUi.BrandBlue,
        cursorColor = VendorUi.BrandBlue,
        focusedTrailingIconColor = VendorUi.BrandBlue,
    )

    Scaffold(
        modifier = modifier,
        containerColor = VendorUi.ScreenBg,
        contentWindowInsets = WindowInsetsZero,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsetsZero,
                title = {
                    Text(
                        text = "Service profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = VendorUi.TextDark,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Profile ID: ${profile.id}",
                style = MaterialTheme.typography.labelLarge,
                color = VendorUi.TextMuted,
            )
            OutlinedTextField(
                value = providerName,
                onValueChange = { providerName = it },
                label = { Text("Provider name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
            )
            ExposedDropdownMenuBox(
                expanded = professionMenuExpanded,
                onExpandedChange = { professionMenuExpanded = !professionMenuExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = profession.displayLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Profession") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = professionMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = fieldShape,
                    colors = fieldColors,
                )
                ExposedDropdownMenu(
                    expanded = professionMenuExpanded,
                    onDismissRequest = { professionMenuExpanded = false },
                ) {
                    ServiceProfession.entries.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.displayLabel) },
                            onClick = {
                                profession = p
                                professionMenuExpanded = false
                            },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = experienceYears,
                onValueChange = { experienceYears = it },
                label = { Text("Experience (years) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
            )
            OutlinedTextField(
                value = serviceArea,
                onValueChange = { serviceArea = it },
                label = { Text("Service area / city *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
            )
            OutlinedTextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                label = { Text("Contact number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
            )
            OutlinedTextField(
                value = shortDescription,
                onValueChange = { shortDescription = it },
                label = { Text("Short description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = fieldShape,
                colors = fieldColors,
            )
            ExposedDropdownMenuBox(
                expanded = chargesMenuExpanded,
                onExpandedChange = { chargesMenuExpanded = !chargesMenuExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = chargesType.displayLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Charges type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = chargesMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = fieldShape,
                    colors = fieldColors,
                )
                ExposedDropdownMenu(
                    expanded = chargesMenuExpanded,
                    onDismissRequest = { chargesMenuExpanded = false },
                ) {
                    ServiceChargesType.entries.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.displayLabel) },
                            onClick = {
                                chargesType = c
                                chargesMenuExpanded = false
                            },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = baseCharge,
                onValueChange = { baseCharge = it },
                label = { Text("Base charge *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
            )
            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it },
                label = { Text("UPI ID * (prepaid)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
                placeholder = { Text("e.g. merchant@okicici") },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (isAvailable) "Available" else "Busy",
                    style = MaterialTheme.typography.bodyLarge,
                    color = VendorUi.TextDark,
                )
                Switch(
                    checked = isAvailable,
                    onCheckedChange = { isAvailable = it },
                )
            }
            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VendorUi.BrandBlue),
            ) {
                Text(if (imageUri.isNullOrBlank()) "Pick profile image" else "Change profile image")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ServiceThumb(uriString = imageUri)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.labelMedium,
                    color = VendorUi.TextMuted,
                )
            }
            formError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Button(
                onClick = {
                    formError = null
                    if (providerName.isBlank() || experienceYears.isBlank() ||
                        serviceArea.isBlank() || contactNumber.isBlank() ||
                        shortDescription.isBlank() || baseCharge.isBlank() ||
                        !VendorPrefs.isValidVendorUpiFormat(upiId)
                    ) {
                        formError = "Please fill all required fields (including a valid UPI ID)."
                        return@Button
                    }
                    onSave(
                        profile.copy(
                            providerName = providerName.trim(),
                            profession = profession,
                            experienceYears = experienceYears.trim(),
                            serviceArea = serviceArea.trim(),
                            contactNumber = contactNumber.trim(),
                            shortDescription = shortDescription.trim(),
                            chargesType = chargesType,
                            baseCharge = baseCharge.trim(),
                            imageUri = imageUri?.trim()?.takeIf { it.isNotBlank() },
                            isAvailable = isAvailable,
                            upiId = upiId.trim(),
                        ),
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VendorUi.BrandBlue),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Save changes")
            }
        }
    }
}
