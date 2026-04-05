package com.homeshop.seebazar.data

import com.homeshop.seebazar.servicehome.ProductCategory
import com.homeshop.seebazar.servicehome.VendorProduct
import com.homeshop.seebazar.servicehome.VendorReservation
import java.util.UUID

/**
 * Serializes buyer [MarketplaceData.cartList] / [MarketplaceData.myOrderList] to Firestore
 * [UserFirestore.FIELD_MY_KART] and [UserFirestore.FIELD_MY_ORDER].
 */
object UserCommerceFirestore {

    fun persistCartAndOrders(
        uid: String,
        marketplace: MarketplaceData,
        onDone: (Throwable?) -> Unit = {},
    ) {
        val kart = marketplace.cartList.map { kartEntryToMap(it) }
        val orders = marketplace.myOrderList.map { placedOrderToMap(it) }
        UserFirestore.usersCollection().document(uid)
            .update(
                mapOf(
                    UserFirestore.FIELD_MY_KART to kart,
                    UserFirestore.FIELD_MY_ORDER to orders,
                ),
            )
            .addOnCompleteListener { onDone(it.exception) }
    }

    /**
     * Loads MyKart + MyOrder into [marketplace] (replaces existing cart and order lists).
     */
    fun loadCartAndOrders(
        uid: String,
        marketplace: MarketplaceData,
        onDone: (Throwable?) -> Unit,
    ) {
        UserFirestore.usersCollection().document(uid).get()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onDone(task.exception)
                    return@addOnCompleteListener
                }
                val snap = task.result
                marketplace.cartList.clear()
                if (snap == null || !snap.exists()) {
                    onDone(null)
                    return@addOnCompleteListener
                }
                (snap.get(UserFirestore.FIELD_MY_KART) as? List<*>)?.forEach { item ->
                    (item as? Map<*, *>)?.let { kartEntryFromMap(it) }?.let { marketplace.cartList.add(it) }
                }
                onDone(null)
            }
    }

    fun notifyCartChanged(uid: String?, marketplace: MarketplaceData) {
        val u = uid ?: return
        persistCartAndOrders(u, marketplace) { }
    }

    fun finalizeCheckout(
        uid: String,
        marketplace: MarketplaceData,
        line: KartEntry,
        pickupTime: String,
        paymentType: String,
        paymentStatus: String,
        buyerName: String,
        buyerEmail: String,
        onDone: (Throwable?) -> Unit,
    ) {
        val orderId = UUID.randomUUID().toString()
        val placedAt = System.currentTimeMillis()
        val vendorUid = when (line) {
            is KartEntry.ProductInCart -> line.product.sourceVendorId.trim()
            is KartEntry.BookingPending -> line.reservation.sourceVendorId.trim()
        }
        val orderCore = when (line) {
            is KartEntry.ProductInCart -> {
                val p = line.product
                UserPlacedOrder(
                    orderId = orderId,
                    lineId = line.lineId,
                    kind = UserOrderLineKind.Product,
                    title = p.name,
                    subtitle = "${p.brand} · ${p.mrpPrice} / ${p.unit}",
                    imageUri = p.imageUri,
                    vendorShopName = p.vendorShopName.ifBlank { p.brand },
                    pickupTime = pickupTime,
                    paymentType = paymentType,
                    paymentStatus = paymentStatus,
                    orderStatus = "Pending",
                    placedAtMillis = placedAt,
                    qrPayload = "",
                    buyerName = buyerName.trim(),
                    buyerEmail = buyerEmail.trim(),
                    vendorUid = vendorUid,
                )
            }
            is KartEntry.BookingPending -> {
                val r = line.reservation
                UserPlacedOrder(
                    orderId = orderId,
                    lineId = line.lineId,
                    kind = UserOrderLineKind.Booking,
                    title = r.venueName,
                    subtitle = "${r.date} · ${r.timeSlot}",
                    imageUri = null,
                    vendorShopName = r.vendorShopName,
                    pickupTime = pickupTime,
                    paymentType = paymentType,
                    paymentStatus = paymentStatus,
                    orderStatus = "Pending",
                    placedAtMillis = placedAt,
                    qrPayload = "",
                    buyerName = buyerName.trim(),
                    buyerEmail = buyerEmail.trim(),
                    vendorUid = vendorUid,
                )
            }
        }
        val qrPayload = OrderQrPayload.buildJson(orderCore, uid)
        val order = orderCore.copy(qrPayload = qrPayload)
        OrderFirestore.saveOrder(order, uid) { e1 ->
            if (e1 != null) {
                onDone(e1)
                return@saveOrder
            }
            OrderFirestore.appendVendorOrdersMirror(vendorUid, order, uid) { e2 ->
                if (e2 != null) {
                    onDone(e2)
                    return@appendVendorOrdersMirror
                }
                marketplace.cartList.remove(line)
                marketplace.myOrderList.add(0, order)
                persistCartAndOrders(uid, marketplace, onDone)
            }
        }
    }

    private fun placedOrderToMap(o: UserPlacedOrder): Map<String, Any?> = mapOf(
        "orderId" to o.orderId,
        "lineId" to o.lineId,
        "kind" to o.kind.name,
        "title" to o.title,
        "subtitle" to o.subtitle,
        "imageUri" to o.imageUri,
        "vendorShopName" to o.vendorShopName,
        "pickupTime" to o.pickupTime,
        "paymentType" to o.paymentType,
        "paymentStatus" to o.paymentStatus,
        "orderStatus" to o.orderStatus,
        "placedAtMillis" to o.placedAtMillis,
        "qrPayload" to o.qrPayload,
        "buyerName" to o.buyerName,
        "buyerEmail" to o.buyerEmail,
        "vendorUid" to o.vendorUid,
    )

    private fun placedOrderFromMap(m: Map<*, *>): UserPlacedOrder? {
        val orderId = m["orderId"]?.toString() ?: return null
        val kind = runCatching {
            UserOrderLineKind.valueOf(m["kind"]?.toString().orEmpty())
        }.getOrNull() ?: return null
        val placedAt = when (val v = m["placedAtMillis"]) {
            is Number -> v.toLong()
            is String -> v.toLongOrNull() ?: 0L
            else -> 0L
        }
        return UserPlacedOrder(
            orderId = orderId,
            lineId = m["lineId"]?.toString().orEmpty(),
            kind = kind,
            title = m["title"]?.toString().orEmpty(),
            subtitle = m["subtitle"]?.toString().orEmpty(),
            imageUri = m["imageUri"]?.toString(),
            vendorShopName = m["vendorShopName"]?.toString().orEmpty(),
            pickupTime = m["pickupTime"]?.toString().orEmpty(),
            paymentType = m["paymentType"]?.toString().orEmpty(),
            paymentStatus = m["paymentStatus"]?.toString().orEmpty(),
            orderStatus = m["orderStatus"]?.toString().orEmpty(),
            placedAtMillis = placedAt,
            qrPayload = m["qrPayload"]?.toString() ?: "seebazar:order:$orderId",
            buyerName = m["buyerName"]?.toString().orEmpty(),
            buyerEmail = m["buyerEmail"]?.toString().orEmpty(),
            vendorUid = m["vendorUid"]?.toString().orEmpty(),
        )
    }

    private fun kartEntryToMap(e: KartEntry): Map<String, Any?> = when (e) {
        is KartEntry.ProductInCart -> mapOf(
            "type" to "product",
            "lineId" to e.lineId,
            "orderStatus" to e.orderStatus,
            "product" to vendorProductToMap(e.product),
        )
        is KartEntry.BookingPending -> mapOf(
            "type" to "booking",
            "lineId" to e.lineId,
            "orderStatus" to e.orderStatus,
            "reservation" to vendorReservationToMap(e.reservation),
        )
    }

    private fun kartEntryFromMap(m: Map<*, *>): KartEntry? = when (m["type"]?.toString()) {
        "product" -> {
            val p = (m["product"] as? Map<*, *>)?.let { vendorProductFromMap(it) } ?: return null
            KartEntry.ProductInCart(
                product = p,
                lineId = m["lineId"]?.toString() ?: UUID.randomUUID().toString(),
                orderStatus = m["orderStatus"]?.toString() ?: "Pending",
            )
        }
        "booking" -> {
            val r = (m["reservation"] as? Map<*, *>)?.let { vendorReservationFromMap(it) } ?: return null
            KartEntry.BookingPending(
                reservation = r,
                lineId = m["lineId"]?.toString() ?: UUID.randomUUID().toString(),
                orderStatus = m["orderStatus"]?.toString() ?: "Pending",
            )
        }
        else -> null
    }

    private fun vendorProductToMap(p: VendorProduct): Map<String, Any?> = mapOf(
        "id" to p.id,
        "name" to p.name,
        "description" to p.description,
        "mrpPrice" to p.mrpPrice,
        "imageUri" to p.imageUri,
        "brand" to p.brand,
        "category" to p.category.name,
        "unit" to p.unit,
        "shelfLife" to p.shelfLife,
        "quantityLeft" to p.quantityLeft,
        "isActive" to p.isActive,
        "vendorShopName" to p.vendorShopName,
        "sourceVendorId" to p.sourceVendorId,
        "vendorUpiId" to p.vendorUpiId,
    )

    private fun vendorProductFromMap(m: Map<*, *>): VendorProduct? {
        val id = when (val v = m["id"]) {
            is Number -> v.toInt()
            is String -> v.toIntOrNull()
            else -> null
        } ?: return null
        val catName = m["category"]?.toString() ?: ProductCategory.Grocery.name
        val category = runCatching { ProductCategory.valueOf(catName) }.getOrDefault(ProductCategory.Grocery)
        return VendorProduct(
            id = id,
            name = m["name"]?.toString().orEmpty(),
            description = m["description"]?.toString().orEmpty(),
            mrpPrice = m["mrpPrice"]?.toString().orEmpty(),
            imageUri = m["imageUri"]?.toString(),
            brand = m["brand"]?.toString().orEmpty(),
            category = category,
            unit = m["unit"]?.toString().orEmpty(),
            shelfLife = m["shelfLife"]?.toString().orEmpty(),
            quantityLeft = m["quantityLeft"]?.toString().orEmpty(),
            isActive = m["isActive"] as? Boolean ?: true,
            imageDrawableRes = null,
            vendorShopName = m["vendorShopName"]?.toString().orEmpty(),
            sourceVendorId = m["sourceVendorId"]?.toString().orEmpty(),
            vendorUpiId = m["vendorUpiId"]?.toString().orEmpty(),
        )
    }

    private fun vendorReservationToMap(r: VendorReservation): Map<String, Any?> = mapOf(
        "venueName" to r.venueName,
        "date" to r.date,
        "timeSlot" to r.timeSlot,
        "numPeople" to r.numPeople,
        "instructions" to r.instructions,
        "price" to r.price,
        "vendorShopName" to r.vendorShopName,
        "sourceVendorId" to r.sourceVendorId,
        "vendorUpiId" to r.vendorUpiId,
    )

    private fun vendorReservationFromMap(m: Map<*, *>): VendorReservation = VendorReservation(
        venueName = m["venueName"]?.toString().orEmpty(),
        date = m["date"]?.toString().orEmpty(),
        timeSlot = m["timeSlot"]?.toString().orEmpty(),
        numPeople = m["numPeople"]?.toString().orEmpty(),
        instructions = m["instructions"]?.toString().orEmpty(),
        price = m["price"]?.toString().orEmpty(),
        vendorShopName = m["vendorShopName"]?.toString().orEmpty(),
        sourceVendorId = m["sourceVendorId"]?.toString().orEmpty(),
        vendorUpiId = m["vendorUpiId"]?.toString().orEmpty(),
    )
}
