package com.homeshop.seebazar.data

/** Vendor shop profile for the home top card and the shop-details editor screen. */
data class ShopDetails(
    val shopName: String,
    val vendorId: String,
    val ownerName: String,
    val address: String,
    val city: String,
    val postalCode: String,
    val isOpen: Boolean,
    /** UPI ID for prepaid checkout (e.g. merchant@paytm). */
    val upiId: String = "",
)
