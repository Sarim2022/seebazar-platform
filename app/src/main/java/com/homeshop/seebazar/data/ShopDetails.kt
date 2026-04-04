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
)

fun dummyShopDetails(): ShopDetails = ShopDetails(
    shopName = "FreshMart Provisions",
    vendorId = "VND-88421",
    ownerName = "Ahmed Khan",
    address = "Shop 4, Ground Floor, Riverside Plaza, 12 Market Street",
    city = "Karachi",
    postalCode = "75500",
    isOpen = false,
)
