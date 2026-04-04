package com.homeshop.seebazar.servicehome.smallcompose

import com.homeshop.seebazar.data.ShopDetails
import com.homeshop.seebazar.servicehome.ReservationBusiness
import com.homeshop.seebazar.servicehome.VendorServiceProfile

fun shopDetailsShareText(shop: ShopDetails): String = buildString {
    appendLine(shop.shopName)
    appendLine("Vendor ID: ${shop.vendorId}")
    appendLine("Owner: ${shop.ownerName}")
    val addressLine = listOf(shop.address, shop.city, shop.postalCode).filter { it.isNotBlank() }.joinToString(", ")
    if (addressLine.isNotBlank()) appendLine(addressLine)
    appendLine("Status: ${if (shop.isOpen) "Open now" else "Closed"}")
}.trimEnd()

fun serviceProfileShareText(profile: VendorServiceProfile): String = buildString {
    appendLine("Service profile — ${profile.providerName}")
    appendLine("Service ID: ${profile.id}")
    appendLine("Profession: ${profile.profession.displayLabel}")
    appendLine("Experience: ${profile.experienceYears}")
    appendLine("Service area: ${profile.serviceArea}")
    appendLine("Contact: ${profile.contactNumber}")
    appendLine("Charges: ${profile.chargesType.displayLabel} — ${profile.baseCharge}")
    if (profile.shortDescription.isNotBlank()) appendLine(profile.shortDescription)
    appendLine("Availability: ${if (profile.isAvailable) "Available" else "Busy"}")
}.trimEnd()

fun reservationBusinessShareText(business: ReservationBusiness): String = buildString {
    appendLine("Reservations — ${business.businessName}")
    appendLine("Business ID: ${business.id}")
    appendLine("Type: ${business.businessType.displayLabel}")
    appendLine("Owner: ${business.ownerName}")
    appendLine("Contact: ${business.contactNumber}")
    val addressLine =
        listOf(business.address, business.city, business.postalCode).filter { it.isNotBlank() }.joinToString(", ")
    if (addressLine.isNotBlank()) appendLine(addressLine)
    appendLine("Hours: ${business.openTime} – ${business.closeTime}")
    appendLine("Capacity: ${business.totalCapacity}")
    appendLine("Status: ${if (business.isOpen) "Open" else "Closed"}")
}.trimEnd()
