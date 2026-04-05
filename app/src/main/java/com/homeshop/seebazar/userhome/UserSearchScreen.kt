package com.homeshop.seebazar.userhome

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.homeshop.seebazar.data.KartEntry
import com.homeshop.seebazar.data.MarketplaceData
import com.homeshop.seebazar.data.UserCommerceFirestore
import com.homeshop.seebazar.servicehome.ReservationBusiness
import com.homeshop.seebazar.servicehome.ReservationSlot

private val SearchBarBorder = Color(0xFFE8ECF4)
private val SearchBarBg = Color(0xFFF7F8F9)
private val TextMuted = Color(0xFF64748B)

@Composable
fun UserSearchScreen(
    marketplace: MarketplaceData,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onChatWithVendor: (vendorUid: String, headline: String) -> Unit = { _, _ -> },
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val trimmed = query.trim()
    // Read lists directly so SnapshotStateList changes recompose (remember(keys) would miss item updates).
    val matchingProducts =
        if (trimmed.isEmpty()) emptyList()
        else marketplace.productList.filter { it.name.contains(trimmed, ignoreCase = true) }
    val matchingServices =
        if (trimmed.isEmpty()) emptyList()
        else marketplace.servicePostList.filter { it.isActive && it.title.contains(trimmed, ignoreCase = true) }
    val matchingReservationEntries =
        if (trimmed.isEmpty()) emptyList()
        else marketplace.reservationBrowseList.filter { entry ->
            entry.slot.isActive && slotMatchesName(entry.slot, entry.business, trimmed)
        }
    val cartUid = FirebaseAuth.getInstance().currentUser?.uid
    val shopNameFallback = marketplace.shopList.firstOrNull()?.shopName.orEmpty()
    val hasAnyResults =
        matchingProducts.isNotEmpty() || matchingServices.isNotEmpty() ||
            matchingReservationEntries.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0F172A),
                )
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text("Search by name", color = TextMuted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = TextMuted,
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Voice search coming soon", Toast.LENGTH_SHORT).show()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = "Voice search",
                            tint = Color(0xFF155AC1),
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SearchBarBg,
                    unfocusedContainerColor = SearchBarBg,
                    focusedBorderColor = SearchBarBorder,
                    unfocusedBorderColor = SearchBarBorder,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { keyboard?.hide() },
                ),
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        when {
            trimmed.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Search anything",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                    )
                }
            }
            !hasAnyResults -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No products, services, or bookings match \"$trimmed\".",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 24.dp,
                    ),
                ) {
                    if (matchingProducts.isNotEmpty()) {
                        item {
                            SearchSectionTitle("Products")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        // Same two-column grid as [UserBrowseProductsPanel] on home.
                        items(
                            items = matchingProducts.chunked(2),
                            key = { row -> row.joinToString("-") { it.id.toString() } },
                        ) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                row.forEach { product ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        UserProductCard(
                                            product = product,
                                            shopNameFallback = shopNameFallback,
                                            onAddToCart = {
                                                marketplace.cartList.add(KartEntry.ProductInCart(product = product))
                                                UserCommerceFirestore.notifyCartChanged(cartUid, marketplace)
                                            },
                                        )
                                    }
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    if (matchingServices.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SearchSectionTitle("Services")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(matchingServices, key = { it.id }) { service ->
                            val vendorUid = service.id.substringBefore("_", missingDelimiterValue = "")
                            UserServiceCard(
                                service = service,
                                onChat = {
                                    if (vendorUid.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Could not find vendor for this service",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    } else {
                                        onChatWithVendor(vendorUid, service.title)
                                    }
                                },
                            )
                        }
                    }
                    if (matchingReservationEntries.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SearchSectionTitle("Bookings")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(matchingReservationEntries, key = { it.slot.id }) { entry ->
                            val reservation = reservationSlotToVendorReservation(
                                entry.business,
                                entry.slot,
                                sourceVendorId = entry.vendorUid,
                                vendorShopName = entry.business?.businessName.orEmpty(),
                                vendorUpiId = entry.vendorUpiId,
                            )
                            UserReservationCard(
                                reservation = reservation,
                                onBook = {
                                    marketplace.cartList.add(KartEntry.BookingPending(reservation = reservation))
                                    UserCommerceFirestore.notifyCartChanged(cartUid, marketplace)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun slotMatchesName(
    slot: ReservationSlot,
    business: ReservationBusiness?,
    query: String,
): Boolean {
    if (slot.title.contains(query, ignoreCase = true)) return true
    val placeName = business?.businessName.orEmpty()
    return placeName.isNotBlank() && placeName.contains(query, ignoreCase = true)
}

@Composable
private fun SearchSectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = TextMuted,
    )
}
