package com.homeshop.seebazar.servicehome

enum class ProductCategory(val displayLabel: String) {
    Grocery("Grocery"),
    Drinks("Drinks"),
    DryItems("Dry Items"),
    Snacks("Snacks"),
    Household("Household"),
}

/**
 * Vendor product row stored in [com.homeshop.seebazar.data.MarketplaceData.productList].
 * [id] is a 3-digit style numeric id (e.g. 101, 102) assigned by the marketplace.
 */
data class VendorProduct(
    val id: Int,
    val name: String,
    val description: String,
    val mrpPrice: String,
    val imageUri: String?,
    val brand: String,
    val category: ProductCategory,
    val unit: String,
    val shelfLife: String,
    val quantityLeft: String,
    val isActive: Boolean = true,
    /** When set, UI shows this drawable instead of decoding [imageUri]. */
    val imageDrawableRes: Int? = null,
    /** Shop name shown on user browse cards; falls back to marketplace shop if blank. */
    val vendorShopName: String = "",
    /** Vendor Firebase uid when aggregated from catalog (buyer flows). */
    val sourceVendorId: String = "",
    /** Payment UPI from vendor shop profile when available. */
    val vendorUpiId: String = "",
)

data class VendorReservation(
    val venueName: String,
    val date: String,
    val timeSlot: String,
    val numPeople: String,
    val instructions: String,
    val price: String,
    val vendorShopName: String = "",
    val sourceVendorId: String = "",
    val vendorUpiId: String = "",
)

enum class ReservationBusinessType(val displayLabel: String) {
    Restaurant("Restaurant"),
    Gym("Gym"),
    Library("Library"),
    Salon("Salon"),
    Cafe("Cafe"),
    Clinic("Clinic"),
    CoWorkingSpace("Co-working Space"),
}

/**
 * Vendor-owned reservation venue ([com.homeshop.seebazar.data.MarketplaceData.reservationPlaceList]).
 */
data class ReservationBusiness(
    val id: String,
    val businessName: String,
    val businessType: ReservationBusinessType,
    val ownerName: String,
    val contactNumber: String,
    val address: String,
    val city: String,
    val postalCode: String,
    val openTime: String,
    val closeTime: String,
    val totalCapacity: String,
    val imageUri: String?,
    val isOpen: Boolean = true,
)

enum class ReservationSlotCategory(val displayLabel: String) {
    Table("Table"),
    Seat("Seat"),
    Session("Session"),
    Room("Room"),
    ServiceSlot("Service Slot"),
}

enum class ReservationBookingType(val displayLabel: String) {
    InstantBooking("Instant Booking"),
    ApprovalRequired("Approval Required"),
}

/**
 * Bookable slot posted under a reservation business ([com.homeshop.seebazar.data.MarketplaceData.reservationSlotList]).
 */
data class ReservationSlot(
    val id: String,
    val title: String,
    val category: ReservationSlotCategory,
    val description: String,
    val capacity: String,
    val price: String,
    val availableDaily: Boolean,
    val specificDate: String,
    val startTime: String,
    val endTime: String,
    val totalAvailable: String,
    val bookingType: ReservationBookingType,
    val imageUri: String?,
    val isActive: Boolean = true,
)

enum class ServiceProfession(val displayLabel: String) {
    Electrician("Electrician"),
    Plumber("Plumber"),
    AcRepair("AC Repair"),
    Carpenter("Carpenter"),
    Painter("Painter"),
    Cleaner("Cleaner"),
    ApplianceRepair("Appliance Repair"),
}

enum class ServiceChargesType(val displayLabel: String) {
    PerVisit("Per Visit"),
    PerHour("Per Hour"),
    Custom("Custom"),
}

/** Category for an individual service post (installation, repair, …). */
enum class VendorServicePostCategory(val displayLabel: String) {
    Installation("Installation"),
    Repair("Repair"),
    Maintenance("Maintenance"),
    Emergency("Emergency"),
    Inspection("Inspection"),
}

/**
 * One service-provider profile per vendor ([com.homeshop.seebazar.data.MarketplaceData.serviceProfile]).
 */
data class VendorServiceProfile(
    val id: String,
    val providerName: String,
    val profession: ServiceProfession,
    val experienceYears: String,
    val serviceArea: String,
    val contactNumber: String,
    val shortDescription: String,
    val chargesType: ServiceChargesType,
    val baseCharge: String,
    val imageUri: String?,
    /** true = Available, false = Busy */
    val isAvailable: Boolean,
)

/**
 * A posted service offering under the vendor profile ([com.homeshop.seebazar.data.MarketplaceData.servicePostList]).
 */
data class VendorServicePost(
    val id: String,
    val title: String,
    val category: VendorServicePostCategory,
    val description: String,
    val price: String,
    val estimatedTime: String,
    val emergencyAvailable: Boolean,
    val imageUri: String?,
    val isActive: Boolean = true,
)
