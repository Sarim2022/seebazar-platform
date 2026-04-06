package com.homeshop.seebazar.common

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeshop.seebazar.R
import com.homeshop.seebazar.data.UserOrderLineKind
import com.homeshop.seebazar.data.UserPlacedOrder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppColors {
    val ScreenBg = Color(0xFFF7F8FA)
    val PrimaryBlue = Color(0xFF2F80ED)
    val TextMuted = Color(0xFF9E9E9E)
    val ReceiptTopBarFg = Color(0xFF1F2937)
}

internal fun UserPlacedOrder.receiptAmountLabel(): String {
    val s = subtitle
    Regex("₹\\s*[\\d,.]+").find(s)?.value?.let { return it.trim() }
    Regex("(\\d+[.,]?\\d*)").find(s)?.groupValues?.get(1)?.let { return "₹$it" }
    if (subtitle.isNotBlank()) return subtitle.trim().take(56)
    return "—"
}

@Composable
fun ReceiptShareCard(order: UserPlacedOrder) {
    val df = remember {
        SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
    }
    val dateStr = if (order.placedAtMillis > 0) {
        df.format(Date(order.placedAtMillis))
    } else {
        "—"
    }
    val customer = listOf(order.buyerName, order.buyerEmail)
        .filter { it.isNotBlank() }
        .joinToString("\n")
        .ifBlank { "—" }
    val paymentLine = buildString {
        append(order.paymentStatus.ifBlank { "—" })
        if (order.paymentType.isNotBlank()) {
            append(" · ")
            append(order.paymentType)
        }
    }
    val amount = order.receiptAmountLabel()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.reciept),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Payment Total",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextMuted,
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                ),
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            ReceiptDetailRow(label = "Date", value = dateStr)
            ReceiptDetailRow(label = "Order", value = order.title.ifBlank { order.orderId })
            ReceiptDetailRow(label = "Shop", value = order.vendorShopName.ifBlank { "—" })
            ReceiptDetailRow(label = "Customer", value = customer)
            ReceiptDetailRow(label = "Pickup", value = order.pickupTime.ifBlank { "—" })
            ReceiptDetailRow(label = "Payment", value = paymentLine)
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            ReceiptDetailRow(label = "Total Payment", value = amount, isTotal = true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentReceiptScreen(
    order: UserPlacedOrder,
    onNavigateBack: () -> Unit,
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val scope = rememberCoroutineScope()
    var sharing by remember { mutableStateOf(false) }

    val receiptBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = AppColors.ScreenBg,
        scrolledContainerColor = AppColors.ScreenBg,
        navigationIconContentColor = AppColors.ReceiptTopBarFg,
        titleContentColor = AppColors.ReceiptTopBarFg,
        actionIconContentColor = AppColors.ReceiptTopBarFg,
    )
    Scaffold(
        containerColor = AppColors.ScreenBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Order Receipt",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.ReceiptTopBarFg,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.ReceiptTopBarFg,
                        )
                    }
                },
                colors = receiptBarColors,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ReceiptShareCard(order = order)

            Button(
                onClick = {
                    if (activity == null) {
                        Toast.makeText(context, "Cannot share from this screen", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        sharing = true
                        try {
                            val bitmap = captureComposableAsBitmap(activity) {
                                MaterialTheme {
                                    ReceiptShareCard(order = order)
                                }
                            }
                            val uri = saveBitmapToCachePng(
                                context,
                                bitmap,
                                "receipt_${order.orderId}_${System.currentTimeMillis()}.png",
                            )
                            sharePngImage(context, uri, "Share receipt")
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                e.message ?: "Could not share receipt",
                                Toast.LENGTH_LONG,
                            ).show()
                        } finally {
                            sharing = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !sharing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (sharing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            tint = Color.White,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share receipt",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptDetailRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = if (isTotal) {
                MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.bodySmall
            },
            color = if (isTotal) Color.Black else AppColors.TextMuted,
        )
        Text(
            text = value,
            style = if (isTotal) {
                MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            },
            color = Color.Black,
            textAlign = TextAlign.End,
        )
    }
}

@Preview(showBackground = true, name = "Receipt Screen")
@Composable
fun ReceiptScreenPreview() {
    MaterialTheme {
        PaymentReceiptScreen(
            order = UserPlacedOrder(
                orderId = "ord_preview",
                lineId = "l1",
                kind = UserOrderLineKind.Product,
                title = "Sample product",
                subtitle = "₹1,225 · 2 items",
                imageUri = null,
                vendorShopName = "Demo Shop",
                pickupTime = "Today 5:00 PM",
                paymentType = "UPI",
                paymentStatus = "Paid",
                orderStatus = "Done",
                placedAtMillis = System.currentTimeMillis(),
                qrPayload = "",
                buyerName = "Jane Buyer",
                buyerEmail = "jane@example.com",
                vendorUid = "v1",
            ),
            onNavigateBack = {},
        )
    }
}
