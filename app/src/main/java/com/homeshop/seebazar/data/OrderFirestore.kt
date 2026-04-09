package com.homeshop.seebazar.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Transaction

/**
 * Top-level `orders` collection: canonical order row for buyer, vendor UIs, and QR fulfillment.
 */
object OrderFirestore {
    const val COLLECTION_ORDERS = "orders"

    private const val FIELD_ORDER_ID = "orderId"
    const val FIELD_BUYER_UID = "buyerUid"
    private const val FIELD_BUYER_NAME = "buyerName"
    private const val FIELD_BUYER_EMAIL = "buyerEmail"
    const val FIELD_VENDOR_UID = "vendorUid"
    private const val FIELD_LINE_ID = "lineId"
    private const val FIELD_KIND = "kind"
    private const val FIELD_TITLE = "title"
    private const val FIELD_SUBTITLE = "subtitle"
    private const val FIELD_IMAGE_URI = "imageUri"
    private const val FIELD_VENDOR_SHOP = "vendorShopName"
    private const val FIELD_PICKUP = "pickupTime"
    private const val FIELD_PAYMENT_TYPE = "paymentType"
    private const val FIELD_PAYMENT_STATUS = "paymentStatus"
    private const val FIELD_ORDER_STATUS = "orderStatus"
    private const val FIELD_PLACED_AT = "placedAtMillis"
    private const val FIELD_QR = "qrPayload"
    private const val FIELD_PRICE = "price"

    fun ordersCollection() = FirebaseFirestore.getInstance().collection(COLLECTION_ORDERS)

    /** @deprecated Prefer [OrderQrPayload.parse]. */
    fun orderIdFromQrPayload(raw: String?): String? = OrderQrPayload.parse(raw)?.orderId

    private fun orderToNonNullMap(order: UserPlacedOrder, buyerUid: String): Map<String, Any> =
        orderToMap(order, buyerUid)
            .filterValues { it != null }
            .mapValues { @Suppress("UNCHECKED_CAST") it.value as Any }

    fun orderToMap(order: UserPlacedOrder, buyerUid: String): HashMap<String, Any?> = hashMapOf(
        FIELD_ORDER_ID to order.orderId,
        FIELD_BUYER_UID to buyerUid,
        FIELD_BUYER_NAME to order.buyerName,
        FIELD_BUYER_EMAIL to order.buyerEmail,
        FIELD_VENDOR_UID to order.vendorUid,
        FIELD_LINE_ID to order.lineId,
        FIELD_KIND to order.kind.name,
        FIELD_TITLE to order.title,
        FIELD_SUBTITLE to order.subtitle,
        FIELD_IMAGE_URI to order.imageUri,
        FIELD_VENDOR_SHOP to order.vendorShopName,
        FIELD_PICKUP to order.pickupTime,
        FIELD_PAYMENT_TYPE to order.paymentType,
        FIELD_PAYMENT_STATUS to order.paymentStatus,
        FIELD_ORDER_STATUS to order.orderStatus,
        FIELD_PLACED_AT to order.placedAtMillis,
        FIELD_QR to order.qrPayload,
        FIELD_PRICE to order.price,
    )

    fun orderFromSnapshot(snap: DocumentSnapshot): UserPlacedOrder? {
        val d = snap.data ?: return null
        val flat = HashMap<String, Any>(d.size)
        d.forEach { (k, v) ->
            if (k != null && v != null) flat[k.toString()] = v as Any
        }
        return orderFromMap(flat)
    }

    fun orderFromMap(m: Map<String, Any>): UserPlacedOrder? {
        val orderId = m[FIELD_ORDER_ID]?.toString() ?: return null
        val kind = runCatching {
            UserOrderLineKind.valueOf(m[FIELD_KIND]?.toString().orEmpty())
        }.getOrNull() ?: return null
        val placedAt = when (val v = m[FIELD_PLACED_AT]) {
            is Number -> v.toLong()
            is String -> v.toLongOrNull() ?: 0L
            else -> 0L
        }
        return UserPlacedOrder(
            orderId = orderId,
            lineId = m[FIELD_LINE_ID]?.toString().orEmpty(),
            kind = kind,
            title = m[FIELD_TITLE]?.toString().orEmpty(),
            subtitle = m[FIELD_SUBTITLE]?.toString().orEmpty(),
            imageUri = m[FIELD_IMAGE_URI]?.toString(),
            vendorShopName = m[FIELD_VENDOR_SHOP]?.toString().orEmpty(),
            pickupTime = m[FIELD_PICKUP]?.toString().orEmpty(),
            paymentType = m[FIELD_PAYMENT_TYPE]?.toString().orEmpty(),
            paymentStatus = m[FIELD_PAYMENT_STATUS]?.toString().orEmpty(),
            orderStatus = m[FIELD_ORDER_STATUS]?.toString().orEmpty(),
            placedAtMillis = placedAt,
            qrPayload = m[FIELD_QR]?.toString() ?: "seebazar:order:$orderId",
            buyerName = m[FIELD_BUYER_NAME]?.toString().orEmpty(),
            buyerEmail = m[FIELD_BUYER_EMAIL]?.toString().orEmpty(),
            vendorUid = m[FIELD_VENDOR_UID]?.toString().orEmpty(),
            price = (m[FIELD_PRICE] as? Number)?.toDouble() ?: m[FIELD_PRICE]?.toString()?.toDoubleOrNull() ?: 0.0,
        )
    }

    fun saveOrder(
        order: UserPlacedOrder,
        buyerUid: String,
        onDone: (Throwable?) -> Unit,
    ) {
        ordersCollection().document(order.orderId)
            .set(orderToMap(order, buyerUid))
            .addOnCompleteListener { onDone(it.exception) }
    }

    fun fetchOrder(
        orderId: String,
        onResult: (UserPlacedOrder?) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        ordersCollection().document(orderId).get()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onFailure(task.exception as? Exception ?: Exception("Load failed"))
                    return@addOnCompleteListener
                }
                val snap = task.result
                if (snap == null || !snap.exists()) {
                    onResult(null)
                    return@addOnCompleteListener
                }
                onResult(orderFromSnapshot(snap))
            }
    }

    /**
     * Appends a denormalized copy of the order to the vendor's [UserFirestore.FIELD_VENDOR_ORDERS] array.
     */
    fun appendVendorOrdersMirror(
        vendorUid: String,
        order: UserPlacedOrder,
        buyerUid: String,
        onDone: (Throwable?) -> Unit,
    ) {
        if (vendorUid.isBlank()) {
            onDone(null)
            return
        }
        val vRef = UserFirestore.usersCollection().document(vendorUid)
        vRef.get().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                onDone(task.exception)
                return@addOnCompleteListener
            }
            val snap = task.result
            if (snap == null || !snap.exists()) {
                onDone(Exception("Vendor profile not found"))
                return@addOnCompleteListener
            }
            val existing = mapsListFromFirestore(snap.get(UserFirestore.FIELD_VENDOR_ORDERS))
            val entry = LinkedHashMap(orderToNonNullMap(order, buyerUid))
            val withoutDup = existing.filterNot { it[FIELD_ORDER_ID]?.toString() == order.orderId }.toMutableList()
            withoutDup.add(0, entry)
            vRef.update(UserFirestore.FIELD_VENDOR_ORDERS, withoutDup)
                .addOnCompleteListener { onDone(it.exception) }
        }
    }

    fun markOrderDone(
        orderId: String,
        vendorUid: String,
        onDone: (Throwable?) -> Unit,
    ) {
        val db = FirebaseFirestore.getInstance()
        val orderRef = ordersCollection().document(orderId)
        db.runTransaction { tx ->
            markDoneTransaction(tx, orderRef, orderId, vendorUid)
            null
        }.addOnCompleteListener { task ->
            onDone(task.exception)
        }
    }

    private fun markDoneTransaction(
        tx: Transaction,
        orderRef: com.google.firebase.firestore.DocumentReference,
        orderId: String,
        vendorUid: String,
    ) {
        val orderSnap = tx.get(orderRef)
        if (!orderSnap.exists()) {
            throw FirebaseFirestoreException("Order not found", FirebaseFirestoreException.Code.NOT_FOUND)
        }
        val owner = orderSnap.getString(FIELD_VENDOR_UID).orEmpty()
        if (owner.isBlank() || owner != vendorUid) {
            throw FirebaseFirestoreException(
                "This order is not for your shop",
                FirebaseFirestoreException.Code.PERMISSION_DENIED,
            )
        }
        val status = orderSnap.getString(FIELD_ORDER_STATUS).orEmpty()
        if (status.equals("Done", ignoreCase = true)) {
            throw FirebaseFirestoreException("Order already completed", FirebaseFirestoreException.Code.FAILED_PRECONDITION)
        }
        val buyerUid = orderSnap.getString(FIELD_BUYER_UID).orEmpty()
        if (buyerUid.isBlank()) {
            throw FirebaseFirestoreException("Order has no buyer", FirebaseFirestoreException.Code.ABORTED)
        }
        val buyerRef = UserFirestore.usersCollection().document(buyerUid)
        val vendorRef = UserFirestore.usersCollection().document(vendorUid)
        val buyerSnap = tx.get(buyerRef)
        val vendorSnap = tx.get(vendorRef)

        val doneOrder = orderFromSnapshot(orderSnap)?.copy(orderStatus = "Done")
            ?: throw FirebaseFirestoreException("Invalid order data", FirebaseFirestoreException.Code.ABORTED)
        val historyEntry = LinkedHashMap(orderToNonNullMap(doneOrder, buyerUid))

        tx.update(orderRef, FIELD_ORDER_STATUS, "Done")

        if (buyerSnap.exists() && buyerSnap.get(UserFirestore.FIELD_MY_ORDER) != null) {
            val updatedBuyerOrders = replaceOrderStatusInMapsList(
                buyerSnap.get(UserFirestore.FIELD_MY_ORDER),
                orderId,
                "Done",
            )
            tx.update(buyerRef, UserFirestore.FIELD_MY_ORDER, updatedBuyerOrders)
        }

        if (vendorSnap.exists() && vendorSnap.get(UserFirestore.FIELD_VENDOR_ORDERS) != null) {
            val updatedVo = replaceOrderStatusInMapsList(
                vendorSnap.get(UserFirestore.FIELD_VENDOR_ORDERS),
                orderId,
                "Done",
            )
            tx.update(vendorRef, UserFirestore.FIELD_VENDOR_ORDERS, updatedVo)
        }

        if (buyerSnap.exists()) {
            val mergedBuyerHist = prependOrderMapToHistory(
                buyerSnap.get(UserFirestore.FIELD_USER_ORDER_HISTORY),
                historyEntry,
                orderId,
            )
            tx.update(buyerRef, UserFirestore.FIELD_USER_ORDER_HISTORY, mergedBuyerHist)
        }

        if (vendorSnap.exists()) {
            val mergedVendorHist = prependOrderMapToHistory(
                vendorSnap.get(UserFirestore.FIELD_VENDOR_ORDER_HISTORY),
                historyEntry,
                orderId,
            )
            tx.update(vendorRef, UserFirestore.FIELD_VENDOR_ORDER_HISTORY, mergedVendorHist)

            // Wallet logic: Atomically increment by product price.
            val productPrice = doneOrder.price.toLong() // User specifically asked for INTEGER
            if (productPrice > 0) {
                tx.update(vendorRef, UserFirestore.FIELD_WALLET_VENDOR, FieldValue.increment(productPrice))
            }
        }
    }

    private fun prependOrderMapToHistory(
        raw: Any?,
        entry: LinkedHashMap<String, Any>,
        orderId: String,
    ): List<Map<String, Any>> {
        val existing = mapsListFromFirestore(raw)
        val withoutDup = existing.filterNot { it[FIELD_ORDER_ID]?.toString() == orderId }
        return listOf(entry) + withoutDup
    }

    private fun mapsListFromFirestore(raw: Any?): MutableList<LinkedHashMap<String, Any>> {
        val list = (raw as? List<*>) ?: return mutableListOf()
        return list.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            LinkedHashMap<String, Any>().apply {
                m.forEach { (k, v) ->
                    if (k != null && v != null) put(k.toString(), v as Any)
                }
            }
        }.toMutableList()
    }

    private fun replaceOrderStatusInMapsList(raw: Any?, orderId: String, newStatus: String): List<Map<String, Any>> {
        val maps = mapsListFromFirestore(raw)
        return maps.map { m ->
            val copy = LinkedHashMap(m)
            if (copy["orderId"]?.toString() == orderId) {
                copy["orderStatus"] = newStatus
            }
            copy
        }
    }

    /**
     * One row per [UserPlacedOrder.orderId] (newest [placedAtMillis] wins). Avoids duplicate LazyColumn keys
     * if multiple docs ever share the same logical order id.
     */
    fun dedupeOrdersNewestFirst(orders: List<UserPlacedOrder>): List<UserPlacedOrder> =
        orders.groupBy { it.orderId }
            .values
            .mapNotNull { rows -> rows.maxByOrNull { it.placedAtMillis } }
            .sortedByDescending { it.placedAtMillis }

    fun listenBuyerOrders(
        buyerUid: String,
        onUpdate: (List<UserPlacedOrder>) -> Unit,
    ): ListenerRegistration =
        ordersCollection()
            .whereEqualTo(FIELD_BUYER_UID, buyerUid)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val list = snap.documents.mapNotNull { orderFromSnapshot(it) }
                onUpdate(dedupeOrdersNewestFirst(list))
            }

    fun listenVendorOrders(
        vendorUid: String,
        onUpdate: (List<UserPlacedOrder>) -> Unit,
    ): ListenerRegistration =
        ordersCollection()
            .whereEqualTo(FIELD_VENDOR_UID, vendorUid)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val list = snap.documents.mapNotNull { orderFromSnapshot(it) }
                onUpdate(dedupeOrdersNewestFirst(list))
            }

    fun ordersFromHistoryField(raw: Any?): List<UserPlacedOrder> {
        val list = (raw as? List<*>) ?: return emptyList()
        return list.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            val flat = HashMap<String, Any>(m.size)
            m.forEach { (k, v) ->
                if (k != null && v != null) flat[k.toString()] = v as Any
            }
            orderFromMap(flat)
        }.let { dedupeOrdersNewestFirst(it) }
    }

    fun listenBuyerOrderHistory(
        buyerUid: String,
        onUpdate: (List<UserPlacedOrder>) -> Unit,
    ): ListenerRegistration =
        UserFirestore.usersCollection().document(buyerUid)
            .addSnapshotListener { snap, _ ->
                if (snap == null || !snap.exists()) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                onUpdate(ordersFromHistoryField(snap.get(UserFirestore.FIELD_USER_ORDER_HISTORY)))
            }

    fun listenVendorOrderHistory(
        vendorUid: String,
        onUpdate: (List<UserPlacedOrder>) -> Unit,
    ): ListenerRegistration =
        UserFirestore.usersCollection().document(vendorUid)
            .addSnapshotListener { snap, _ ->
                if (snap == null || !snap.exists()) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                onUpdate(ordersFromHistoryField(snap.get(UserFirestore.FIELD_VENDOR_ORDER_HISTORY)))
            }
}
