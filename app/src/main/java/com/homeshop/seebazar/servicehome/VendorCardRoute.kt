package com.homeshop.seebazar.servicehome

/**
 * Home grid cards → [cardsfeature] screens.
 */
enum class VendorCardRoute {
    Shop,
    Services,
    Reservations,
    Products,
}

fun VendorCardRoute.title(): String = when (this) {
    VendorCardRoute.Shop -> "Shop Profile"
    VendorCardRoute.Services -> "My Services"
    VendorCardRoute.Reservations -> "My Reservations"
    VendorCardRoute.Products -> "My Products"
}
