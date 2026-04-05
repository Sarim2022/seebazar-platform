package com.homeshop.seebazar.servicehome

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.data.OrderFirestore
import com.homeshop.seebazar.data.UserPlacedOrder
import com.homeshop.seebazar.ui.FormBottomSheetScaffold
import com.homeshop.seebazar.ui.FormSheetPrimaryButton

@Composable
fun VendorOrderReceiptBottomSheet(
    order: UserPlacedOrder,
    vendorUid: String,
    onDismiss: () -> Unit,
    onMarkedDone: () -> Unit,
) {
    val context = LocalContext.current
    val isDone = order.orderStatus.equals("Done", ignoreCase = true)
    val canComplete =
        !isDone &&
            order.vendorUid.isNotBlank() &&
            vendorUid.isNotBlank() &&
            order.vendorUid == vendorUid

    FormBottomSheetScaffold(
        onDismiss = onDismiss,
        title = "Order receipt",
        subtitle = when {
            isDone -> "Already completed"
            !canComplete && order.vendorUid.isBlank() ->
                "This order has no shop link — cannot mark done from the app"
            !canComplete -> "This order is not for your shop"
            else -> "Verify customer and complete pickup"
        },
    ) {
        ReceiptLine("Product / booking", order.title)
        ReceiptLine("Details", order.subtitle.ifBlank { "—" })
        ReceiptLine("Customer", order.buyerName.ifBlank { "—" })
        ReceiptLine("Email", order.buyerEmail.ifBlank { "—" })
        ReceiptLine("Pickup time", order.pickupTime.ifBlank { "—" })
        ReceiptLine("Payment", "${order.paymentType} · ${order.paymentStatus}")
        ReceiptLine("Order status", order.orderStatus.ifBlank { "Pending" })
        Spacer(modifier = Modifier.height(8.dp))
        FormSheetPrimaryButton(
            text = when {
                isDone -> "Close"
                canComplete -> "Order done"
                else -> "Close"
            },
            onClick = {
                if (isDone || !canComplete) {
                    onDismiss()
                    return@FormSheetPrimaryButton
                }
                OrderFirestore.markOrderDone(order.orderId, vendorUid) { err ->
                    if (err != null) {
                        Toast.makeText(
                            context,
                            VendorScanViewModel.mapMarkDoneError(err),
                            Toast.LENGTH_LONG,
                        ).show()
                    } else {
                        Toast.makeText(context, "Order marked done", Toast.LENGTH_SHORT).show()
                        onMarkedDone()
                    }
                }
            },
        )
    }
}

@Composable
private fun ReceiptLine(label: String, value: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = VendorUi.TextMuted,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = VendorUi.TextDark,
    )
    Spacer(modifier = Modifier.height(12.dp))
}
