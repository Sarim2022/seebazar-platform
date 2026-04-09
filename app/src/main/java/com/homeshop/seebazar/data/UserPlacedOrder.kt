package com.homeshop.seebazar.data

enum class UserOrderLineKind {
    Product,
    Booking,
}

/**
 * A completed buyer order: mirrored in [UserFirestore.FIELD_MY_ORDER], [UserFirestore.FIELD_VENDOR_ORDERS],
 * and canonical doc [OrderFirestore.COLLECTION_ORDERS]/[orderId].
 */
data class UserPlacedOrder(
    val orderId: String,
    val lineId: String,
    val kind: UserOrderLineKind,
    val title: String,
    val subtitle: String,
    val imageUri: String?,
    val vendorShopName: String,
    val pickupTime: String,
    val paymentType: String,
    val paymentStatus: String,
    val orderStatus: String,
    val placedAtMillis: Long,
    /** JSON or legacy `seebazar:order:<uuid>` for ZXing. */
    val qrPayload: String,
    val buyerName: String = "",
    val buyerEmail: String = "",
    /** Vendor Firebase uid (shop owner) for routing and scan verification. */
    val vendorUid: String = "",
    /** Numeric product/booking price for wallet additions. */
    val price: Double = 0.0,
)
