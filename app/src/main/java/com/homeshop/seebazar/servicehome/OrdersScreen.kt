package com.homeshop.seebazar.servicehome

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeshop.seebazar.data.OrderFirestore
import com.homeshop.seebazar.data.UserPlacedOrder
import com.homeshop.seebazar.userhome.OrderHistoryFullScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class OrderListFilter {
    All, Done, Pending
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier,
    vendorUid: String = "",
) {
    var orders by remember { mutableStateOf<List<UserPlacedOrder>>(emptyList()) }
    var historyOrders by remember { mutableStateOf<List<UserPlacedOrder>>(emptyList()) }
    var listFilter by remember { mutableStateOf(OrderListFilter.All) }
    var showHistory by remember { mutableStateOf(false) }

    DisposableEffect(vendorUid) {
        if (vendorUid.isBlank()) {
            orders = emptyList()
            historyOrders = emptyList()
            return@DisposableEffect onDispose { }
        }
        val reg = OrderFirestore.listenVendorOrders(vendorUid) { list ->
            orders = list
        }
        val histReg = OrderFirestore.listenVendorOrderHistory(vendorUid) { historyOrders = it }
        onDispose {
            reg.remove()
            histReg.remove()
        }
    }

    val filtered = remember(orders, listFilter) {
        val base = OrderFirestore.dedupeOrdersNewestFirst(orders)
        when (listFilter) {
            OrderListFilter.All -> base
            OrderListFilter.Done -> base.filter { it.orderStatus.equals("Done", ignoreCase = true) }
            OrderListFilter.Pending -> base.filter { !it.orderStatus.equals("Done", ignoreCase = true) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = VendorUi.ScreenBg,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            Column(
                modifier = Modifier.background(VendorUi.TopBarBg),
            ) {
                VendorStandardTopBar(
                    title = "Order Status",
                    actions = {
                        IconButton(
                            onClick = { showHistory = true },
                            enabled = vendorUid.isNotBlank(),
                        ) {
                            Icon(
                                Icons.Filled.History,
                                contentDescription = "Order history",
                                tint = VendorUi.BrandBlue,
                            )
                        }
                    },
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatusFilterChip(
                        label = "All",
                        selected = listFilter == OrderListFilter.All,
                        onClick = { listFilter = OrderListFilter.All },
                    )
                    StatusFilterChip(
                        label = "Done",
                        selected = listFilter == OrderListFilter.Done,
                        onClick = { listFilter = OrderListFilter.Done },
                    )
                    StatusFilterChip(
                        label = "Pending",
                        selected = listFilter == OrderListFilter.Pending,
                        onClick = { listFilter = OrderListFilter.Pending },
                    )
                }
                HorizontalDivider(color = VendorUi.CardStroke, thickness = 1.dp)
            }
        },
    ) { paddingValues ->
        if (vendorUid.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VendorUi.ScreenBg)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Sign in to see shop orders.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = VendorUi.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        } else if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VendorUi.ScreenBg)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No orders yet",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = VendorUi.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VendorUi.ScreenBg)
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(
                    items = filtered,
                    key = { index, order -> "${order.orderId}-$index" },
                ) { _, order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        VendorOrderListRow(order = order)
                    }
                }
            }
        }
    }

    if (showHistory && vendorUid.isNotBlank()) {
        OrderHistoryFullScreen(
            title = "Completed orders",
            orders = historyOrders,
            onBack = { showHistory = false },
            modifier = Modifier.fillMaxSize(),
            useVendorCardStyle = true,
        )
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.Medium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFE3EEF9),
            selectedLabelColor = VendorUi.BrandBlue,
            containerColor = Color.White,
            labelColor = VendorUi.TextMuted,
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = VendorUi.CardStroke,
            selectedBorderColor = VendorUi.BrandBlue,
            enabled = true,
            selected = selected,
        ),
    )
}

@Composable
fun VendorOrderListRow(
    order: UserPlacedOrder,
) {
    val df = remember { SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()) }
    val isDone = order.orderStatus.equals("Done", ignoreCase = true)
    val customer = listOf(order.buyerName, order.buyerEmail).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "—" }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = if (isDone) Color(0xFF2E7D32) else Color(0xFFFF9690),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = if (isDone) "Done" else "Pending",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = order.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VendorUi.TextDark)
        Text(text = order.vendorShopName.ifBlank { "Shop" }, fontSize = 13.sp, color = VendorUi.TextMuted)
        Text(text = "Customer: $customer", fontSize = 14.sp, color = VendorUi.TextMuted)
        Text(text = if (order.placedAtMillis > 0) df.format(Date(order.placedAtMillis)) else "—", fontSize = 12.sp, color = VendorUi.TextMuted.copy(alpha = 0.8f))

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = order.subtitle, fontSize = 14.sp, color = VendorUi.TextDark.copy(alpha = 0.85f))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Pickup: ${order.pickupTime.ifBlank { "—" }}",
            fontSize = 13.sp,
            color = VendorUi.TextDark,
        )
        Text(
            text = "Payment: ${order.paymentType} · ${order.paymentStatus}",
            fontSize = 13.sp,
            color = VendorUi.TextMuted,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Order ID: ${order.orderId}",
            fontSize = 11.sp,
            color = VendorUi.TextMuted,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Use Scan → Open camera and photograph the customer's order QR to complete pickup.",
            fontSize = 12.sp,
            color = VendorUi.TextMuted,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrdersPreview() {
    MaterialTheme {
        OrdersScreen(vendorUid = "preview")
    }
}
