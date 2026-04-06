package com.homeshop.seebazar.servicehome

import androidx.lifecycle.ViewModel
import com.homeshop.seebazar.data.UserPlacedOrder

/**
 * Holds the [UserPlacedOrder] while navigating from order list → [com.homeshop.seebazar.common.PaymentReceiptScreen].
 */
class VendorReceiptNavViewModel : ViewModel() {
    var pendingReceiptOrder: UserPlacedOrder? = null
        private set

    fun setPendingReceiptOrder(order: UserPlacedOrder) {
        pendingReceiptOrder = order
    }

    fun clearPendingReceiptOrder() {
        pendingReceiptOrder = null
    }
}

object VendorOrdersRoutes {
    const val List = "vendor_orders_list"
    const val Receipt = "vendor_orders_receipt"
}
