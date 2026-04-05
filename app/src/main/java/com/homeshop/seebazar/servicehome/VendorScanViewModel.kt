package com.homeshop.seebazar.servicehome

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestoreException
import com.homeshop.seebazar.data.OrderQrPayload
import com.homeshop.seebazar.data.UserPlacedOrder
import com.homeshop.seebazar.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Handles vendor QR scan → Firestore verification → receipt UI.
 */
class VendorScanViewModel : ViewModel() {

    private val _receiptOrder = MutableStateFlow<UserPlacedOrder?>(null)
    val receiptOrder: StateFlow<UserPlacedOrder?> = _receiptOrder.asStateFlow()

    fun dismissReceipt() {
        _receiptOrder.value = null
    }

    /**
     * @param onUserMessage short message for Toast/Snackbar when scan cannot proceed
     */
    fun processCameraQrText(
        raw: String?,
        vendorUid: String,
        onUserMessage: (String) -> Unit,
    ) {
        val parsed = OrderQrPayload.parse(raw)
        if (parsed == null) {
            onUserMessage("Invalid order QR")
            return
        }
        if (vendorUid.isBlank()) {
            onUserMessage("Sign in to scan orders")
            return
        }
        parsed.vendorIdFromQr?.takeIf { it.isNotBlank() }?.let { vid ->
            if (vid != vendorUid) {
                onUserMessage("This QR is for a different vendor")
                return
            }
        }
        OrderRepository.fetchOrder(
            orderId = parsed.orderId,
            onResult = { order ->
                if (order == null) {
                    onUserMessage("Order not found")
                    return@fetchOrder
                }
                if (order.vendorUid.isNotBlank() && order.vendorUid != vendorUid) {
                    onUserMessage("This order is not for your shop")
                    return@fetchOrder
                }
                if (order.orderStatus.equals("Done", ignoreCase = true)) {
                    onUserMessage("This order is already completed")
                    return@fetchOrder
                }
                _receiptOrder.value = order
            },
            onFailure = { e -> onUserMessage(e.localizedMessage ?: "Could not load order") },
        )
    }

    companion object {
        fun mapMarkDoneError(e: Throwable?): String =
            when (e) {
                is FirebaseFirestoreException -> when (e.code) {
                    FirebaseFirestoreException.Code.NOT_FOUND -> "Order not found"
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> "This order is not for your shop"
                    FirebaseFirestoreException.Code.FAILED_PRECONDITION -> "Order already completed"
                    FirebaseFirestoreException.Code.ABORTED -> e.message ?: "Could not update order"
                    else -> e.message ?: "Could not update order"
                }
                else -> e?.localizedMessage ?: "Could not update order"
            }
    }
}
