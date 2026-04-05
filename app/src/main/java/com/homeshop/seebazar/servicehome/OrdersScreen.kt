package com.homeshop.seebazar.servicehome

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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

private enum class OrderListFilter {
    All, Done, Pending
}

data class OrderData(
    val id: String,
    val storeName: String,
    val customerName: String,
    val date: String,
    val items: String,
    val itemCount: Int,
    val price: String,
    val isDone: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(modifier: Modifier = Modifier) {
    var orders by remember { mutableStateOf<List<OrderData>>(emptyList()) }
    var listFilter by remember { mutableStateOf(OrderListFilter.All) }

    val filtered = remember(orders, listFilter) {
        when (listFilter) {
            OrderListFilter.All -> orders
            OrderListFilter.Done -> orders.filter { it.isDone }
            OrderListFilter.Pending -> orders.filter { !it.isDone }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VendorUi.ScreenBg,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            Column(
                modifier = Modifier.background(VendorUi.TopBarBg),
            ) {
                VendorStandardTopBar(
                    title = "Order Status",
                    actions = {
                        IconButton(onClick = { /* filter */ }) {
                            Icon(
                                Icons.Filled.Tune,
                                contentDescription = "Filter",
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
        if (filtered.isEmpty()) {
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
                contentPadding = PaddingValues(bottom = 4.dp),
            ) {
                items(filtered, key = { it.id }) { order ->
                    OrderItemCard(
                        order = order,
                        onStatusChange = { updated ->
                            orders = orders.map { if (it.id == updated.id) updated else it }
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = VendorUi.CardStroke)
                }
            }
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
fun OrderItemCard(
    order: OrderData,
    onStatusChange: (OrderData) -> Unit,
) {
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
                color = if (order.isDone) Color(0xFF2E7D32) else Color(0xFFFF9690),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = if (order.isDone) "Done" else "Pending",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "See Details", fontSize = 14.sp, color = VendorUi.TextDark)
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = VendorUi.TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = order.storeName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VendorUi.TextDark)
        Text(text = "to ${order.customerName}", fontSize = 14.sp, color = VendorUi.TextMuted)
        Text(text = order.date, fontSize = 12.sp, color = VendorUi.TextMuted.copy(alpha = 0.8f))

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = order.items, fontSize = 14.sp, color = VendorUi.TextDark.copy(alpha = 0.85f))

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${order.itemCount} Item  •  ${order.price}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = VendorUi.TextDark,
            )

            Button(
                onClick = { onStatusChange(order.copy(isDone = !order.isDone)) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = VendorUi.BrandBlue,
                ),
                border = BorderStroke(1.dp, VendorUi.BrandBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
            ) {
                Text(if (order.isDone) "Mark Pending" else "Mark Done")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrdersPreview() {
    MaterialTheme {
        OrdersScreen()
    }
}
