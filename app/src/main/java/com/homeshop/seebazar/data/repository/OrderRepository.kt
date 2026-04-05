package com.homeshop.seebazar.data.repository

import com.google.firebase.firestore.ListenerRegistration
import com.homeshop.seebazar.data.OrderFirestore
import com.homeshop.seebazar.data.UserPlacedOrder

/**
 * Facade over [OrderFirestore] for ViewModels and future caching/offline layers.
 */
object OrderRepository {

    fun saveCanonicalOrder(order: UserPlacedOrder, buyerUid: String, onDone: (Throwable?) -> Unit) {
        OrderFirestore.saveOrder(order, buyerUid, onDone)
    }

    fun appendVendorMirror(vendorUid: String, order: UserPlacedOrder, buyerUid: String, onDone: (Throwable?) -> Unit) {
        OrderFirestore.appendVendorOrdersMirror(vendorUid, order, buyerUid, onDone)
    }

    fun markOrderDone(orderId: String, vendorUid: String, onDone: (Throwable?) -> Unit) {
        OrderFirestore.markOrderDone(orderId, vendorUid, onDone)
    }

    fun fetchOrder(
        orderId: String,
        onResult: (UserPlacedOrder?) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        OrderFirestore.fetchOrder(orderId, onResult, onFailure)
    }

    fun listenBuyerOrders(buyerUid: String, onUpdate: (List<UserPlacedOrder>) -> Unit): ListenerRegistration =
        OrderFirestore.listenBuyerOrders(buyerUid, onUpdate)

    fun listenVendorOrders(vendorUid: String, onUpdate: (List<UserPlacedOrder>) -> Unit): ListenerRegistration =
        OrderFirestore.listenVendorOrders(vendorUid, onUpdate)
}
