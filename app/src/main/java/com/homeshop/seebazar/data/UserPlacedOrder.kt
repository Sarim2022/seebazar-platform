package com.homeshop.seebazar.data

enum class UserOrderLineKind {
    Product,
    Booking,
}

/**
 * Buyer order stored in Firestore `orders/{orderId}` and mirrored in [MarketplaceData.myOrderList].
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
    /** `Pending` until vendor completes pickup via QR scan; then `Done`. */
    val orderStatus: String,
    val placedAtMillis: Long,
    /** Unique payload encoded in the order QR (same as [orderId] with app prefix). */
    val qrPayload: String,
    val buyerName: String = "",
    val buyerEmail: String = "",
    val vendorUid: String = "",
)
