package com.homeshop.seebazar.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.homeshop.seebazar.R
import com.homeshop.seebazar.servicehome.ProductCategory
import com.homeshop.seebazar.servicehome.ReservationBusiness
import com.homeshop.seebazar.servicehome.ReservationSlot
import com.homeshop.seebazar.servicehome.ServiceChargesType
import com.homeshop.seebazar.servicehome.ServiceProfession
import com.homeshop.seebazar.servicehome.VendorProduct
import com.homeshop.seebazar.servicehome.VendorReservation
import com.homeshop.seebazar.servicehome.VendorServicePost
import com.homeshop.seebazar.servicehome.VendorServicePostCategory
import com.homeshop.seebazar.servicehome.VendorServiceProfile
import java.util.UUID

/**
 * Dummy marketplace state shared between vendor and user flows (same activity / navigation graph).
 */
class MarketplaceData {
    /** Dummy vendor shop row(s); index 0 powers the home top card. */
    val shopList: SnapshotStateList<ShopDetails> = mutableStateListOf(dummyShopDetails())

    val productList: SnapshotStateList<VendorProduct> = mutableStateListOf<VendorProduct>().apply {
        val shopName = shopList.first().shopName
        addAll(sampleVendorProducts(shopName))
    }
    /** Legacy list; user browse reads [reservationSlotList] + [reservationPlaceList] instead. */
    val reservationList: SnapshotStateList<VendorReservation> = mutableStateListOf()
    /** At most one place in normal UI; stored as list for snapshot state. */
    val reservationPlaceList: SnapshotStateList<ReservationBusiness> = mutableStateListOf()
    val reservationSlotList: SnapshotStateList<ReservationSlot> = mutableStateListOf()

    /** At most one service-provider profile in the dummy flow; seeded like [productList] for test mode. */
    var serviceProfile: VendorServiceProfile? by mutableStateOf(dummyServiceProfile())

    val servicePostList: SnapshotStateList<VendorServicePost> = mutableStateListOf<VendorServicePost>().apply {
        addAll(sampleVendorServicePosts())
    }

    /** Products added from the user Product tab; reservations added via "Book it". */
    val cartList: SnapshotStateList<KartEntry> = mutableStateListOf()

    /** Next product id (101, 102, …) — shown in the add form before submit. */
    private var nextProductId: Int = SAMPLE_PRODUCT_IDS_END_EXCLUSIVE
    private var nextReservationBusinessSeq: Int = 101
    private var nextReservationSlotSeq: Int = 201
    /** After sample ids SRV-101 and POST-201…POST-203. */
    private var nextServiceProfileSeq: Int = SAMPLE_SERVICE_PROFILE_SEQ_NEXT
    private var nextServicePostSeq: Int = SAMPLE_SERVICE_POST_SEQ_NEXT

    fun peekNextServiceProfileId(): String = "SRV-$nextServiceProfileSeq"

    fun takeNextServiceProfileId(): String {
        val id = peekNextServiceProfileId()
        nextServiceProfileSeq++
        return id
    }

    fun peekNextServicePostId(): String = "POST-$nextServicePostSeq"

    fun takeNextServicePostId(): String {
        val id = peekNextServicePostId()
        nextServicePostSeq++
        return id
    }

    fun peekNextProductId(): Int = nextProductId

    fun takeNextProductId(): Int = nextProductId++

    fun peekNextReservationBusinessId(): String = "RES-$nextReservationBusinessSeq"

    fun takeNextReservationBusinessId(): String {
        val id = peekNextReservationBusinessId()
        nextReservationBusinessSeq++
        return id
    }

    fun peekNextReservationSlotId(): String = "SLOT-$nextReservationSlotSeq"

    fun takeNextReservationSlotId(): String {
        val id = peekNextReservationSlotId()
        nextReservationSlotSeq++
        return id
    }
}

sealed class KartEntry {
    abstract val lineId: String

    data class ProductInCart(
        val product: VendorProduct,
        override val lineId: String = UUID.randomUUID().toString(),
    ) : KartEntry()

    data class BookingPending(
        val reservation: VendorReservation,
        override val lineId: String = UUID.randomUUID().toString(),
    ) : KartEntry()
}

private const val SAMPLE_PRODUCT_IDS_END_EXCLUSIVE = 107

private const val SAMPLE_SERVICE_PROFILE_SEQ_NEXT = 102

private const val SAMPLE_SERVICE_POST_SEQ_NEXT = 204

private fun dummyServiceProfile(): VendorServiceProfile = VendorServiceProfile(
    id = "SRV-101",
    providerName = "Ahmad Electric",
    profession = ServiceProfession.Electrician,
    experienceYears = "8",
    serviceArea = "Karachi · DHA & Clifton",
    contactNumber = "+92 300 1234567",
    shortDescription = "Licensed electrician — wiring, fixtures, DB work, emergency callouts.",
    chargesType = ServiceChargesType.PerVisit,
    baseCharge = "Rs 1,500",
    imageUri = null,
    isAvailable = true,
)

private fun sampleVendorServicePosts(): List<VendorServicePost> = listOf(
    VendorServicePost(
        id = "POST-201",
        title = "Fan & light installation",
        category = VendorServicePostCategory.Installation,
        description = "Ceiling fans, LED panels, switches, and basic fittings.",
        price = "Rs 800 onwards",
        estimatedTime = "45 min – 2 hrs",
        emergencyAvailable = false,
        imageUri = null,
        isActive = true,
    ),
    VendorServicePost(
        id = "POST-202",
        title = "Short circuit / trip fix",
        category = VendorServicePostCategory.Repair,
        description = "DB check, faulty wiring trace, and safe repair.",
        price = "Rs 1,200 onwards",
        estimatedTime = "1–3 hrs",
        emergencyAvailable = true,
        imageUri = null,
        isActive = true,
    ),
    VendorServicePost(
        id = "POST-203",
        title = "Full home wiring inspection",
        category = VendorServicePostCategory.Inspection,
        description = "Safety inspection with written notes and quote for fixes.",
        price = "Rs 2,500",
        estimatedTime = "2–4 hrs",
        emergencyAvailable = false,
        imageUri = null,
        isActive = true,
    ),
)

private fun sampleVendorProducts(shopName: String): List<VendorProduct> = listOf(
    VendorProduct(
        id = 101,
        name = "Milk",
        description = "Fresh full-cream milk.",
        mrpPrice = "Rs 220",
        imageUri = null,
        brand = "Dairy Best",
        category = ProductCategory.Grocery,
        unit = "1 L",
        shelfLife = "3 days",
        quantityLeft = "40",
        imageDrawableRes = R.drawable.milk,
        vendorShopName = shopName,
    ),
    VendorProduct(
        id = 102,
        name = "Thums Up",
        description = "Chilled soft drink.",
        mrpPrice = "Rs 95",
        imageUri = null,
        brand = "Thums Up",
        category = ProductCategory.Drinks,
        unit = "500 ml",
        shelfLife = "9 months",
        quantityLeft = "120",
        imageDrawableRes = R.drawable.thumsup,
        vendorShopName = shopName,
    ),
    VendorProduct(
        id = 103,
        name = "Chipslays",
        description = "Crispy potato chips.",
        mrpPrice = "Rs 60",
        imageUri = null,
        brand = "Chipslays",
        category = ProductCategory.Snacks,
        unit = "52 g",
        shelfLife = "6 months",
        quantityLeft = "80",
        imageDrawableRes = R.drawable.chipslays,
        vendorShopName = shopName,
    ),
    VendorProduct(
        id = 104,
        name = "Curd",
        description = "Fresh set curd.",
        mrpPrice = "Rs 140",
        imageUri = null,
        brand = "Farm Fresh",
        category = ProductCategory.Grocery,
        unit = "500 g",
        shelfLife = "2 days",
        quantityLeft = "25",
        imageDrawableRes = R.drawable.curd,
        vendorShopName = shopName,
    ),
    VendorProduct(
        id = 105,
        name = "Coffee",
        description = "Instant coffee jar.",
        mrpPrice = "Rs 890",
        imageUri = null,
        brand = "Classic Roast",
        category = ProductCategory.Drinks,
        unit = "200 g",
        shelfLife = "18 months",
        quantityLeft = "15",
        imageDrawableRes = R.drawable.coffee,
        vendorShopName = shopName,
    ),
    VendorProduct(
        id = 106,
        name = "Maggi",
        description = "Instant noodles masala.",
        mrpPrice = "Rs 28",
        imageUri = null,
        brand = "Maggi",
        category = ProductCategory.DryItems,
        unit = "70 g",
        shelfLife = "8 months",
        quantityLeft = "200",
        imageDrawableRes = R.drawable.maggii,
        vendorShopName = shopName,
    ),
)
