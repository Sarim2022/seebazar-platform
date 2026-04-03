package com.homeshop.seebazar.userhome

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.homeshop.seebazar.data.KartEntry
import com.homeshop.seebazar.data.MarketplaceData
import com.homeshop.seebazar.servicehome.VendorUi

private val ScreenBg = Color(0xFFF8FAFC)

@Composable
fun UserKartScreen(
    modifier: Modifier = Modifier,
    marketplace: MarketplaceData,
) {
    var showOrderDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val cart = marketplace.cartList
    val productLines = cart.filterIsInstance<KartEntry.ProductInCart>()
    val bookingLines = cart.filterIsInstance<KartEntry.BookingPending>()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = "My Kart",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = VendorUi.TextDark,
        )
        Spacer(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Shopping cart",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = VendorUi.TextDark,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        if (productLines.isEmpty()) {
            Text(
                text = "No products yet — add items from the Home tab.",
                style = MaterialTheme.typography.bodyMedium,
                color = VendorUi.TextMuted,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        } else {
            productLines.forEach { line ->
                KartCard(
                    title = line.product.name,
                    subtitle = "${line.product.brand} · ${line.product.price} / ${line.product.unit}",
                    extra = line.product.description.takeIf { it.isNotBlank() },
                ) {
                    Button(
                        onClick = {
                            showOrderDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VendorUi.BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Order it")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.padding(vertical = 12.dp))

        Text(
            text = "Pending bookings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = VendorUi.TextDark,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        if (bookingLines.isEmpty()) {
            Text(
                text = "No pending bookings — book from the Reservations tab on Home.",
                style = MaterialTheme.typography.bodyMedium,
                color = VendorUi.TextMuted,
            )
        } else {
            bookingLines.forEach { line ->
                val r = line.reservation
                KartCard(
                    title = r.venueName,
                    subtitle = "${r.date} · ${r.timeSlot}",
                    extra = r.instructions.takeIf { it.isNotBlank() },
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Order Confirmed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VendorUi.BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
        if (showOrderDialog) {
            OrderConfirmDialog(
                onDismiss = { showOrderDialog = false },
                onOrderClick = { paymentType, pickupTime ->

                    if (paymentType == "Prepaid") {
                        Toast.makeText(context, "Payment done !", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Order confirmed", Toast.LENGTH_SHORT).show()
                    }

                    showOrderDialog = false
                }
            )
        }
    }
}
@Composable
fun OrderConfirmDialog(
    onDismiss: () -> Unit,
    onOrderClick: (String, String) -> Unit
) {
    var selectedPayment by remember { mutableStateOf("Prepaid") }
    var pickupTime by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Text(
                    text = "Confirm Order",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Choose Payment Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PaymentOptionChip(
                        text = "Prepaid",
                        selected = selectedPayment == "Prepaid",
                        onClick = { selectedPayment = "Prepaid" },
                        modifier = Modifier.weight(1f)
                    )

                    PaymentOptionChip(
                        text = "Postpaid",
                        selected = selectedPayment == "Postpaid",
                        onClick = { selectedPayment = "Postpaid" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Pickup Time",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pickupTime,
                    onValueChange = { pickupTime = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter pickup time (e.g. 5:30 PM)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onOrderClick(selectedPayment, pickupTime)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Order")
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color(0xFFE8F5E9) else Color(0xFFF7F7F7),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Color(0xFF2E7D32) else Color.LightGray
        )
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color(0xFF2E7D32) else Color.DarkGray
            )
        }
    }
}

@Composable
private fun KartCard(
    title: String,
    subtitle: String,
    extra: String?,
    footer: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = VendorUi.TextDark,
            )
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = VendorUi.TextMuted,
            )
            if (!extra.isNullOrBlank()) {
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = extra,
                    style = MaterialTheme.typography.bodySmall,
                    color = VendorUi.TextMuted,
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            footer()
        }
    }
}
