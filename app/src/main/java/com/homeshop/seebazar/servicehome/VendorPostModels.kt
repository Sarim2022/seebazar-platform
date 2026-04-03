package com.homeshop.seebazar.servicehome

enum class ProductCategory {
    Grocery,
    Drink,
    JunkFood,
}

data class VendorProduct(
    val id: Int,
    val name: String,
    val description: String,
    val brand: String,
    val category: ProductCategory,
    val price: String,
    val unit: String,
    val tags: String?,
)

data class VendorReservation(
    val venueName: String,
    val date: String,
    val timeSlot: String,
    val numPeople: String,
    val instructions: String,
    val price: String,
)

data class VendorServiceItem(
    val serviceName: String,
    val priceFixed: String,
    val availability: String,
    val instruction: String,
)
