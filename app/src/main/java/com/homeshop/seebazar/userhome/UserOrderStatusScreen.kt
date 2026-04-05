package com.homeshop.seebazar.userhome

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.data.MarketplaceData
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
    val orders = marketplace.myOrderList
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(16.dp),
    ) {
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
        if (orders.isEmpty()) {
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
                items(orders, key = { it.orderId }) { order ->
                    UserOrderCard(order = order)
                }
            }
        }
    }
}

@Composable
private fun UserOrderCard(order: UserPlacedOrder) {
    val dateStr = formatOrderDate(order.placedAtMillis)
    val thumb = rememberDecodedBitmap(order.imageUri)
    val qrSize = 88.dp
    val density = LocalDensity.current
    val qrPx = remember(order.qrPayload, density) {
        with(density) { qrSize.roundToPx() }.coerceAtLeast(64)
    }
    val qrBitmap = rememberOrderQrBitmap(order.qrPayload, qrPx)

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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
