package com.homeshop.seebazar.servicehome

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeshop.seebazar.common.VendorCommonTopBar
import com.homeshop.seebazar.data.OrderFirestore
import com.homeshop.seebazar.data.UserPlacedOrder
import com.homeshop.seebazar.userhome.OrderHistoryFullScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val OrdersScreenBg = Color(0xFFF7F8FA)
private val ScanIconBg = Color(0xFFF1F4F9)
private val ScanIconTint = Color(0xFF2F80ED)
private val TabIndicatorBlue = Color(0xFF2F80ED)
private val StatusDoneBg = Color(0xFFE6F9EF)
private val StatusDoneText = Color(0xFF009667)
private val StatusPendingBg = Color(0xFFFEF3E1)
private val StatusPendingText = Color(0xFFF2994A)
private val CardSecondaryBg = Color(0xFFFAFAFA)

private enum class OrderListFilter {
    All, Done, Pending, History
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier,
    vendorUid: String = "",
    onScanClick: () -> Unit = {},
    /** Opens the digital receipt for completed orders (vendor flow). */
    onOpenReceiptForDoneOrder: (UserPlacedOrder) -> Unit = {},
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

    val filtered = remember(orders, historyOrders, listFilter) {
        when (listFilter) {
            OrderListFilter.History ->
                OrderFirestore.dedupeOrdersNewestFirst(historyOrders)
                    .filter { it.orderStatus.equals("Done", ignoreCase = true) }
            OrderListFilter.All -> OrderFirestore.dedupeOrdersNewestFirst(orders)
            OrderListFilter.Done ->
                OrderFirestore.dedupeOrdersNewestFirst(orders)
                    .filter { it.orderStatus.equals("Done", ignoreCase = true) }
            OrderListFilter.Pending ->
                OrderFirestore.dedupeOrdersNewestFirst(orders)
                    .filter { !it.orderStatus.equals("Done", ignoreCase = true) }
        }
    }

    val orderTabFilters = listOf(
        OrderListFilter.All,
        OrderListFilter.Done,
        OrderListFilter.Pending,
        OrderListFilter.History,
    )
    val selectedTabIndex = orderTabFilters.indexOf(listFilter).takeIf { it >= 0 } ?: 0

    Box(modifier = modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = OrdersScreenBg,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            Column(
                modifier = Modifier.background(Color.White),
            ) {
                VendorCommonTopBar(
                    title = "My Order status",
                    onBackClick = { /* navController.popBackStack() */ },
                    containerColor = Color.White,
                    actions = {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = ScanIconBg,
                        ) {
                            IconButton(
                                onClick = onScanClick,
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.QrCodeScanner,
                                    contentDescription = "Scan order QR",
                                    tint = ScanIconTint,
                                )
                            }
                        }
                    },
                )
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    contentColor = VendorUi.TextDark,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = TabIndicatorBlue,
                            )
                        }
                    },
                    divider = {},
                ) {
                    orderTabFilters.forEachIndexed { index, filter ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { listFilter = filter },
                            text = {
                                Text(
                                    text = when (filter) {
                                        OrderListFilter.All -> "All"
                                        OrderListFilter.Done -> "Done"
                                        OrderListFilter.Pending -> "Pending"
                                        OrderListFilter.History -> "History"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Medium,
                                )
                            },
                            selectedContentColor = TabIndicatorBlue,
                            unselectedContentColor = VendorUi.TextMuted,
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        if (vendorUid.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OrdersScreenBg)
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
                    .background(OrdersScreenBg)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (listFilter == OrderListFilter.History) {
                        "No completed orders in history yet"
                    } else {
                        "No orders yet"
                    },
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
                    .background(OrdersScreenBg)
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(
                    items = filtered,
                    key = { index, order -> "${order.orderId}-$index" },
                ) { _, order ->
                    VendorOrderListRow(
                        order = order,
                        onReceiptClick = if (order.orderStatus.equals("Done", ignoreCase = true)) {
                            { onOpenReceiptForDoneOrder(order) }
                        } else {
                            null
                        },
                    )
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

@Composable
fun VendorOrderListRow(
    order: UserPlacedOrder,
    onReceiptClick: (() -> Unit)? = null,
) {
    val df = remember { SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()) }
    val isDone = order.orderStatus.equals("Done", ignoreCase = true)

    val customer = listOf(order.buyerName, order.buyerEmail)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
        .ifBlank { "No customer info" }

    val statusBadgeBg = if (isDone) StatusDoneBg else StatusPendingBg
    val statusLabelColor = if (isDone) StatusDoneText else StatusPendingText
    val statusLabel = if (isDone) "Done" else "Pending"

    val paymentLine = buildString {
        append(order.paymentStatus.ifBlank { "—" })
        if (order.paymentType.isNotBlank()) {
            append(" · ")
            append(order.paymentType)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onReceiptClick != null) {
                    Modifier.clickable(onClick = onReceiptClick)
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Order ID · ${order.orderId}",
                    style = MaterialTheme.typography.labelMedium,
                    color = VendorUi.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                )
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = statusBadgeBg,
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusLabelColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = order.vendorShopName.ifBlank { "Shop" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VendorUi.TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = order.title.ifBlank { "Order Item" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VendorUi.TextDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CardSecondaryBg,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OrderDetailIconRow(
                        icon = Icons.Outlined.Person,
                        label = "Customer",
                        value = customer,
                    )
                    OrderDetailIconRow(
                        icon = Icons.Outlined.Payments,
                        label = "Payment status",
                        value = paymentLine,
                    )
                    OrderDetailIconRow(
                        icon = Icons.Outlined.AccessTime,
                        label = "Pickup time",
                        value = order.pickupTime.ifBlank { "Not selected" },
                    )
                }
            }

            if (order.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = order.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF374151),
                    lineHeight = 20.sp,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = OrdersScreenBg,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (order.placedAtMillis > 0) {
                        df.format(Date(order.placedAtMillis))
                    } else {
                        "Date unavailable"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = VendorUi.TextMuted,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isDone) {
                    "Tap to view and share the digital receipt."
                } else {
                    "Scan the customer's QR code to complete pickup."
                },
                style = MaterialTheme.typography.bodySmall,
                color = VendorUi.TextMuted,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun OrderDetailIconRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = VendorUi.TextMuted,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = VendorUi.TextMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = VendorUi.TextDark,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrdersPreview() {
    MaterialTheme {
        OrdersScreen(vendorUid = "preview")
    }
}
