package com.homeshop.seebazar.userhome

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.homeshop.seebazar.data.KartEntry
import com.homeshop.seebazar.data.MarketplaceData
import com.homeshop.seebazar.data.UserCommerceFirestore
import com.homeshop.seebazar.servicehome.ReservationBusiness
import com.homeshop.seebazar.servicehome.ReservationSlot
import com.homeshop.seebazar.servicehome.VendorProduct
import com.homeshop.seebazar.servicehome.VendorReservation
import com.homeshop.seebazar.servicehome.VendorServicePost
import com.homeshop.seebazar.servicehome.VendorUi
import com.homeshop.seebazar.ui.rememberDecodedBitmap

private val CallChatGreyBg = Color(0xFFE5E7EB)
private val CallChatGreyFg = Color(0xFF374151)
private val CallChatGreenBg = Color(0xFFDCFCE7)
private val CallChatGreenFg = Color(0xFF166534)

@Composable
fun UserBrowseProductsPanel(
    marketplace: MarketplaceData,
    modifier: Modifier = Modifier,
) {
    val cartUid = FirebaseAuth.getInstance().currentUser?.uid
    val products = marketplace.productList
    if (products.isEmpty()) {
        EmptyBrowseMessage(
            text = "No products listed yet.",
            modifier = modifier,
        )
        return
    }
    val rows = products.chunked(2)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { product ->
                    Box(modifier = Modifier.weight(1f)) {
                        UserProductCard(
                            product = product,
                            shopNameFallback = marketplace.shopList.firstOrNull()?.shopName.orEmpty(),
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
}

/**
 * Buyer home uses [MarketplaceData.reservationBrowseList] (one venue + slot per row).
 * [reservationSlotToVendorReservation] builds the card model from that pair.
 */
internal fun reservationSlotToVendorReservation(
    business: ReservationBusiness?,
    slot: ReservationSlot,
    sourceVendorId: String = "",
    vendorShopName: String = "",
    vendorUpiId: String = "",
): VendorReservation {
    val venueName = when {
        business != null -> "${business.businessName} · ${slot.title}"
        else -> slot.title
    }
    val date = when {
        slot.availableDaily -> "Daily"
        else -> slot.specificDate.ifBlank { "—" }
    }
    val timeSlot = when {
        slot.startTime.isNotBlank() && slot.endTime.isNotBlank() ->
            "${slot.startTime} – ${slot.endTime}"
        slot.startTime.isNotBlank() -> slot.startTime
        else -> slot.endTime.ifBlank { "—" }
    }
    val detailLines = buildList {
        if (slot.description.isNotBlank()) add(slot.description)
        add("${slot.category.displayLabel} · ${slot.bookingType.displayLabel}")
        if (slot.totalAvailable.isNotBlank()) add("Spots: ${slot.totalAvailable}")
    }
    val shop = vendorShopName.ifBlank { business?.businessName.orEmpty() }
    return VendorReservation(
        venueName = venueName,
        date = date,
        timeSlot = timeSlot,
        numPeople = slot.capacity.ifBlank { "—" },
        instructions = detailLines.joinToString("\n"),
        price = slot.price,
        vendorShopName = shop,
        sourceVendorId = sourceVendorId,
        vendorUpiId = vendorUpiId,
    )
}

@Composable
fun UserBrowseReservationsPanel(
    marketplace: MarketplaceData,
    modifier: Modifier = Modifier,
) {
    val cartUid = FirebaseAuth.getInstance().currentUser?.uid
    val entries = marketplace.reservationBrowseList
    if (entries.isEmpty()) {
        EmptyBrowseMessage(
            text = "No reservations available yet.",
            modifier = modifier,
        )
        return
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        entries.forEach { entry ->
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

@Composable
fun UserBrowseServicesPanel(
    marketplace: MarketplaceData,
    modifier: Modifier = Modifier,
) {
    val services = marketplace.servicePostList.filter { it.isActive }
    if (services.isEmpty()) {
        EmptyBrowseMessage(
            text = "No services listed yet.",
            modifier = modifier,
        )
        return
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        services.forEach { service ->
            UserServiceCard(service = service)
        }
    }
}

@Composable
private fun EmptyBrowseMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(vertical = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = VendorUi.TextMuted,
    )
}

@Composable
internal fun UserProductCard(
    product: VendorProduct,
    shopNameFallback: String,
    onAddToCart: () -> Unit,
) {
    val shopLabel = product.vendorShopName.ifBlank { shopNameFallback }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            UserProductSquareImage(
                product = product,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            )
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = shopLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = VendorUi.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = VendorUi.TextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.mrpPrice,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = VendorUi.TextDark,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VendorUi.BrandBlue),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Add to Cart")
                }
            }
        }
    }
}

@Composable
private fun UserProductSquareImage(
    product: VendorProduct,
    modifier: Modifier = Modifier,
) {
    val resId = product.imageDrawableRes?.takeIf { it != 0 }
    val bitmap = rememberDecodedBitmap(product.imageUri)
    Box(
        modifier = modifier.background(Color(0xFFF1F5F9)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            resId != null -> {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            bitmap != null -> {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            else -> {
                Text(
                    text = product.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = VendorUi.TextMuted,
                )
            }
        }
    }
}

@Composable
internal fun UserReservationCard(
    reservation: VendorReservation,
    onBook: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = reservation.venueName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = VendorUi.TextDark,
            )
            if (reservation.price.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = reservation.price,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = VendorUi.BrandBlue,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(VendorUi.ScreenBg, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                ) {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = VendorUi.TextMuted,
                    )
                    Text(
                        text = reservation.date.ifBlank { "—" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = VendorUi.BrandBlue,
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(VendorUi.ScreenBg, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                ) {
                    Text(
                        text = "Time",
                        style = MaterialTheme.typography.labelSmall,
                        color = VendorUi.TextMuted,
                    )
                    Text(
                        text = reservation.timeSlot.ifBlank { "—" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = VendorUi.BrandBlue,
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(VendorUi.ScreenBg, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                ) {
                    Text(
                        text = "Capacity",
                        style = MaterialTheme.typography.labelSmall,
                        color = VendorUi.TextMuted,
                    )
                    Text(
                        text = reservation.numPeople.ifBlank { "—" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = VendorUi.BrandBlue,
                    )
                }
            }
            if (reservation.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = reservation.instructions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VendorUi.TextDark,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onBook,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VendorUi.BrandBlue),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Book it")
            }
        }
    }
}

@Composable
internal fun UserServiceCard(
    service: VendorServicePost,
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = service.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = VendorUi.TextDark,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = service.id,
                style = MaterialTheme.typography.labelSmall,
                color = VendorUi.TextMuted,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Price: ${service.price}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = VendorUi.BrandBlue,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${service.category.displayLabel} · ${service.estimatedTime}" +
                    if (service.emergencyAvailable) " · Emergency OK" else "",
                style = MaterialTheme.typography.bodySmall,
                color = VendorUi.TextMuted,
            )
            if (service.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VendorUi.TextMuted,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "Call", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CallChatGreyBg,
                        contentColor = CallChatGreyFg,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = "Call",
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    Text("Call")
                }
                Button(
                    onClick = {
                        Toast.makeText(context, "Chat", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CallChatGreenBg,
                        contentColor = CallChatGreenFg,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Chat",
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    Text("Chat")
                }
            }
        }
    }
}
