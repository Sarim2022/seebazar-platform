package com.homeshop.seebazar.userhome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.homeshop.seebazar.data.MarketplaceData
import com.homeshop.seebazar.data.OrderFirestore
import com.homeshop.seebazar.data.OrderQrPayload
import com.homeshop.seebazar.data.UserLocationPrefs
import com.homeshop.seebazar.data.UserPlacedOrder
import com.homeshop.seebazar.servicehome.VendorUi
import com.homeshop.seebazar.ui.rememberDecodedBitmap
import com.homeshop.seebazar.ui.rememberOrderQrBitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ScreenBg = Color(0xFFF8FAFC)

@Composable
fun UserOrderStatusScreen(
    modifier: Modifier = Modifier,
    marketplace: MarketplaceData,
) {
    val buyerUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    var historyOrders by remember { mutableStateOf<List<UserPlacedOrder>>(emptyList()) }
    var showHistory by remember { mutableStateOf(false) }
    var zoomOrder by remember { mutableStateOf<UserPlacedOrder?>(null) }

    DisposableEffect(buyerUid) {
        if (buyerUid.isBlank()) {
            historyOrders = emptyList()
            return@DisposableEffect onDispose { }
        }
        val reg = OrderFirestore.listenBuyerOrderHistory(buyerUid) { historyOrders = it }
        onDispose { reg.remove() }
    }

    val displayOrders = OrderFirestore.dedupeOrdersNewestFirst(marketplace.myOrderList.toList())
    val context = LocalContext.current
    val cityLine = UserLocationPrefs.city(context)
    val subLine = UserLocationPrefs.displaySubtitle(context)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = VendorUi.BrandBlue,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cityLine.ifBlank { "Location" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = VendorUi.TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = subLine.ifBlank { "Set in Home" },
                            style = MaterialTheme.typography.labelSmall,
                            color = VendorUi.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                IconButton(
                    onClick = { showHistory = true },
                    enabled = buyerUid.isNotBlank(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = "Order history",
                        tint = VendorUi.BrandBlue,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Order status",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = VendorUi.TextDark,
            )
            Text(
                text = "Track pickups, payment, and your pickup QR codes.",
                style = MaterialTheme.typography.bodyMedium,
                color = VendorUi.TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            if (displayOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No orders yet — place one from My Kart.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VendorUi.TextMuted,
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(
                        items = displayOrders,
                        key = { index, order -> "${order.orderId}-$index" },
                    ) { _, order ->
                        UserOrderCard(
                            order = order,
                            onQrClick = { zoomOrder = order },
                        )
                    }
                }
            }
        }

        if (showHistory && buyerUid.isNotBlank()) {
            OrderHistoryFullScreen(
                title = "Completed orders",
                orders = historyOrders,
                onBack = { showHistory = false },
                modifier = Modifier.fillMaxSize(),
                useVendorCardStyle = false,
            )
        }
    }

    OrderQrZoomDialog(order = zoomOrder, onDismiss = { zoomOrder = null })
}

@Composable
private fun OrderQrZoomDialog(
    order: UserPlacedOrder?,
    onDismiss: () -> Unit,
) {
    if (order == null) return
    val density = LocalDensity.current
    val qrScanContent = OrderQrPayload.scannablePayload(order)
    val largePx = remember(qrScanContent, density) {
        with(density) { 280.dp.roundToPx() }.coerceIn(256, 640)
    }
    val qrBitmap = rememberOrderQrBitmap(qrScanContent, largePx)
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Pickup QR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VendorUi.TextDark,
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Order QR enlarged",
                        modifier = Modifier.size(280.dp),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE2E8F0)),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text("Close", color = VendorUi.BrandBlue)
                }
            }
        }
    }
}

@Composable
private fun UserOrderCard(
    order: UserPlacedOrder,
    onQrClick: () -> Unit,
) {
    val dateStr = formatOrderDate(order.placedAtMillis)
    val thumb = rememberDecodedBitmap(order.imageUri)
    val qrScanContent = OrderQrPayload.scannablePayload(order)
    val qrSize = 88.dp
    val density = LocalDensity.current
    val qrPx = remember(qrScanContent, density) {
        with(density) { qrSize.roundToPx() }.coerceAtLeast(64)
    }
    val qrBitmap = rememberOrderQrBitmap(qrScanContent, qrPx)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        thumb != null -> {
                            Image(
                                bitmap = thumb.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        else -> {
                            Text(
                                text = order.title.take(1).uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = VendorUi.BrandBlue,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VendorUi.TextDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = order.vendorShopName.ifBlank { "Vendor" },
                        style = MaterialTheme.typography.bodySmall,
                        color = VendorUi.TextMuted,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = order.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = VendorUi.TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(onClick = onQrClick),
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Order QR",
                            modifier = Modifier.size(qrSize),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(qrSize)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE2E8F0)),
                        )
                    }
                    Text(
                        text = "QR",
                        style = MaterialTheme.typography.labelSmall,
                        color = VendorUi.TextMuted,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OrderMetaChip(label = "Pickup", value = order.pickupTime.ifBlank { "—" })
                OrderMetaChip(label = "Payment", value = order.paymentType)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OrderMetaChip(label = "Pay status", value = order.paymentStatus)
                OrderMetaChip(label = "Order", value = order.orderStatus)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Placed: $dateStr",
                style = MaterialTheme.typography.bodySmall,
                color = VendorUi.TextMuted,
            )
            Text(
                text = "Order ID: ${order.orderId}",
                style = MaterialTheme.typography.labelSmall,
                color = VendorUi.TextMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun OrderMetaChip(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VendorUi.TextMuted,
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = VendorUi.TextDark,
        )
    }
}

private fun formatOrderDate(millis: Long): String =
    if (millis <= 0L) {
        "—"
    } else {
        SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(millis))
    }
