package com.homeshop.seebazar.userhome

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.data.UserPlacedOrder
import com.homeshop.seebazar.servicehome.VendorUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryFullScreen(
    title: String,
    orders: List<UserPlacedOrder>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    useVendorCardStyle: Boolean = false,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VendorUi.ScreenBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
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
                            tint = VendorUi.BrandBlue,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VendorUi.TopBarBg,
                    titleContentColor = VendorUi.TextDark,
                    navigationIconContentColor = VendorUi.BrandBlue,
                ),
            )
        },
    ) { padding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No completed orders yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = VendorUi.TextMuted,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(
                    items = orders,
                    key = { index, o -> "${o.orderId}-hist-$index" },
                ) { _, order ->
                    if (useVendorCardStyle) {
                        HistoryVendorStyleCard(order)
                    } else {
                        HistoryUserStyleCard(order)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryUserStyleCard(order: UserPlacedOrder) {
    val df = rememberHistoryDateFormat()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = order.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = VendorUi.TextDark,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = order.vendorShopName.ifBlank { "Vendor" },
                style = MaterialTheme.typography.bodySmall,
                color = VendorUi.TextMuted,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Pickup", style = MaterialTheme.typography.labelSmall, color = VendorUi.TextMuted)
                    Text(
                        order.pickupTime.ifBlank { "—" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Payment", style = MaterialTheme.typography.labelSmall, color = VendorUi.TextMuted)
                    Text(
                        "${order.paymentType} · ${order.paymentStatus}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (order.placedAtMillis > 0) df.format(Date(order.placedAtMillis)) else "—",
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
private fun HistoryVendorStyleCard(order: UserPlacedOrder) {
    val df = rememberHistoryDateFormat()
    val customer = listOf(order.buyerName, order.buyerEmail).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "—" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = order.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = VendorUi.TextDark,
            )
            Text(
                text = order.vendorShopName.ifBlank { "Shop" },
                style = MaterialTheme.typography.bodySmall,
                color = VendorUi.TextMuted,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Customer: $customer", style = MaterialTheme.typography.bodySmall, color = VendorUi.TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = order.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = VendorUi.TextDark.copy(alpha = 0.85f),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("Pickup: ${order.pickupTime.ifBlank { "—" }}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = "Payment: ${order.paymentType} · ${order.paymentStatus}",
                style = MaterialTheme.typography.bodySmall,
                color = VendorUi.TextMuted,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (order.placedAtMillis > 0) df.format(Date(order.placedAtMillis)) else "—",
                style = MaterialTheme.typography.labelSmall,
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
private fun rememberHistoryDateFormat() = remember {
    SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
}
