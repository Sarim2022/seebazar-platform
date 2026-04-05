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
import com.google.firebase.auth.FirebaseAuth
import com.homeshop.seebazar.data.KartEntry
import com.homeshop.seebazar.data.MarketplaceData
import com.homeshop.seebazar.data.UserCommerceFirestore
import com.homeshop.seebazar.data.UserProfilePrefs
import com.homeshop.seebazar.servicehome.VendorUi
import com.homeshop.seebazar.ui.FormBottomSheetScaffold
import com.homeshop.seebazar.ui.FormSheetPrimaryButton
import com.homeshop.seebazar.ui.FormSheetTextField

private val ScreenBg = Color(0xFFF8FAFC)
private val ChipSelectedBg = Color(0xFF1F2937)
private val ChipUnselectedBg = Color(0xFFF3F4F6)

private data class PrepaidSheetArgs(
    val line: KartEntry,
    val pickupTime: String,
)

@Composable
fun UserKartScreen(
    modifier: Modifier = Modifier,
    marketplace: MarketplaceData,
) {
    var pickupSheetLine by remember { mutableStateOf<KartEntry?>(null) }
    var prepaidArgs by remember { mutableStateOf<PrepaidSheetArgs?>(null) }

    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val buyerName = UserProfilePrefs.cachedDisplayName(context).ifBlank {
        FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()
    }
    val buyerEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
    val cart = marketplace.cartList
    val productLines = cart.filterIsInstance<KartEntry.ProductInCart>()
    val bookingLines = cart.filterIsInstance<KartEntry.BookingPending>()

    fun finalizePostpaid(line: KartEntry, pickupTime: String) {
        if (uid == null) {
            Toast.makeText(context, "Please sign in again.", Toast.LENGTH_SHORT).show()
            return
        }
        UserCommerceFirestore.finalizeCheckout(
            uid = uid,
            marketplace = marketplace,
            line = line,
            pickupTime = pickupTime,
            paymentType = "Postpaid",
            paymentStatus = "Pending",
            buyerName = buyerName,
            buyerEmail = buyerEmail,
        ) { err ->
            if (err != null) {
                Toast.makeText(context, "Could not save order.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Order confirmed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun finalizePrepaidPaid(line: KartEntry, pickupTime: String) {
        if (uid == null) {
            Toast.makeText(context, "Please sign in again.", Toast.LENGTH_SHORT).show()
            return
        }
        UserCommerceFirestore.finalizeCheckout(
            uid = uid,
            marketplace = marketplace,
            line = line,
            pickupTime = pickupTime,
            paymentType = "Prepaid",
            paymentStatus = "Paid",
            buyerName = buyerName,
            buyerEmail = buyerEmail,
        ) { err ->
            if (err != null) {
                Toast.makeText(context, "Could not save order.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Order confirmed", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                    subtitle = "${line.product.brand} · ${line.product.mrpPrice} / ${line.product.unit}",
                    extra = line.product.description.takeIf { it.isNotBlank() },
                    statusLine = "Status: ${line.orderStatus}",
                ) {
                    Button(
                        onClick = { pickupSheetLine = line },
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
                    statusLine = "Status: ${line.orderStatus}",
                ) {
                    Button(
                        onClick = { pickupSheetLine = line },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VendorUi.BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Order it")
                    }
                }
            }
        }
    }

    pickupSheetLine?.let { line ->
        PickupPaymentBottomSheet(
            onDismiss = { pickupSheetLine = null },
            onPlacePostpaid = { pickup ->
                pickupSheetLine = null
                finalizePostpaid(line, pickup)
            },
            onContinuePrepaid = { pickup ->
                pickupSheetLine = null
                prepaidArgs = PrepaidSheetArgs(line, pickup)
            },
        )
    }

    prepaidArgs?.let { args ->
        PrepaidUpiBottomSheet(
            shopName = args.line.vendorShopForPay(),
            upiLine = formatVendorUpiLine(args.line.vendorShopForPay(), args.line.vendorUpiForPay()),
            onDismiss = { prepaidArgs = null },
            onPayIt = {
                prepaidArgs = null
                finalizePrepaidPaid(args.line, args.pickupTime)
            },
        )
    }
}

private fun KartEntry.vendorShopForPay(): String = when (this) {
    is KartEntry.ProductInCart -> product.vendorShopName.ifBlank { product.brand }
    is KartEntry.BookingPending -> reservation.vendorShopName
}

private fun KartEntry.vendorUpiForPay(): String = when (this) {
    is KartEntry.ProductInCart -> product.vendorUpiId
    is KartEntry.BookingPending -> reservation.vendorUpiId
}

private fun formatVendorUpiLine(shop: String, upi: String): String {
    val u = upi.trim()
    if (u.isEmpty()) return "${shop.ifBlank { "Vendor" }} — UPI not shared by vendor yet"
    return if (u.contains('@')) "${shop.ifBlank { "Vendor" }} — $u" else "${shop.ifBlank { "vendor" }}@$u"
}

@Composable
private fun PickupPaymentBottomSheet(
    onDismiss: () -> Unit,
    onPlacePostpaid: (String) -> Unit,
    onContinuePrepaid: (String) -> Unit,
) {
    var selectedPayment by remember { mutableStateOf("Prepaid") }
    var pickupTime by remember { mutableStateOf("") }

    FormBottomSheetScaffold(
        onDismiss = onDismiss,
        title = "Place order",
        subtitle = "Choose payment and pickup time",
    ) {
        Text(
            text = "Payment type",
            style = MaterialTheme.typography.labelMedium,
            color = VendorUi.TextMuted,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PaymentOptionChip(
                text = "Prepaid",
                selected = selectedPayment == "Prepaid",
                onClick = { selectedPayment = "Prepaid" },
                modifier = Modifier.weight(1f),
            )
            PaymentOptionChip(
                text = "Postpaid",
                selected = selectedPayment == "Postpaid",
                onClick = { selectedPayment = "Postpaid" },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FormSheetTextField(
            label = "Pickup time",
            value = pickupTime,
            onValueChange = { pickupTime = it },
            placeholder = "e.g. Today 5:30 PM",
        )
        FormSheetPrimaryButton(
            text = "Place order",
            enabled = pickupTime.isNotBlank(),
            onClick = {
                if (selectedPayment == "Postpaid") {
                    onPlacePostpaid(pickupTime.trim())
                } else {
                    onContinuePrepaid(pickupTime.trim())
                }
            },
        )
    }
}

@Composable
private fun PrepaidUpiBottomSheet(
    shopName: String,
    upiLine: String,
    onDismiss: () -> Unit,
    onPayIt: () -> Unit,
) {
    FormBottomSheetScaffold(
        onDismiss = onDismiss,
        title = "Pay vendor",
        subtitle = "Send payment via UPI",
    ) {
        Text(
            text = "Pay to",
            style = MaterialTheme.typography.labelMedium,
            color = VendorUi.TextMuted,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = upiLine,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = VendorUi.TextDark,
        )
        if (shopName.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = shopName,
                style = MaterialTheme.typography.bodyMedium,
                color = VendorUi.TextMuted,
            )
        }
        FormSheetPrimaryButton(
            text = "Pay it",
            onClick = onPayIt,
        )
    }
}

@Composable
fun PaymentOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) ChipSelectedBg else ChipUnselectedBg,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) ChipSelectedBg else Color(0xFFE5E7EB),
        ),
    ) {
        Box(
            modifier = Modifier.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else VendorUi.TextDark,
            )
        }
    }
}

@Composable
private fun KartCard(
    title: String,
    subtitle: String,
    extra: String?,
    statusLine: String,
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
            Text(
                text = statusLine,
                style = MaterialTheme.typography.labelMedium,
                color = VendorUi.BrandBlue,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            footer()
        }
    }
}
