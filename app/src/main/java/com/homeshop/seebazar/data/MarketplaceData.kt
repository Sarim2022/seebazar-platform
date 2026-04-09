package com.homeshop.seebazar.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.homeshop.seebazar.servicehome.ReservationBusiness
import com.homeshop.seebazar.servicehome.ReservationSlot
import com.homeshop.seebazar.servicehome.VendorProduct
import com.homeshop.seebazar.servicehome.VendorReservation
import com.homeshop.seebazar.servicehome.VendorServicePost
import com.homeshop.seebazar.servicehome.VendorServiceProfile
import java.util.UUID

/** One bookable slot with its owning venue (user dashboard lists one row per pair). */
data class ReservationBrowseEntry(
    val business: ReservationBusiness,
    val slot: ReservationSlot,
    val vendorUid: String = "",
    val vendorUpiId: String = "",
)

/**
 * In-memory marketplace state shared between vendor and user flows (backed by Firestore for vendors).
 */
class MarketplaceData {
    val shopList: SnapshotStateList<ShopDetails> = mutableStateListOf()

    val productList: SnapshotStateList<VendorProduct> = mutableStateListOf()

    /** Legacy list; vendor UI uses [reservationPlaceList] + [reservationSlotList]. */
    val reservationList: SnapshotStateList<VendorReservation> = mutableStateListOf()
    /** At most one place in normal UI; stored as list for snapshot state. */
    val reservationPlaceList: SnapshotStateList<ReservationBusiness> = mutableStateListOf()
    val reservationSlotList: SnapshotStateList<ReservationSlot> = mutableStateListOf()
    /** Aggregated rows for the buyer home / search (each slot tied to its vendor business). */
    val reservationBrowseList: SnapshotStateList<ReservationBrowseEntry> = mutableStateListOf()

    var serviceProfile: VendorServiceProfile? by mutableStateOf(null)

    val servicePostList: SnapshotStateList<VendorServicePost> = mutableStateListOf()

    /** Vendor wallet numeric balance synced with Firestore [UserFirestore.FIELD_WALLET_VENDOR]. */
    var walletVendor: Long by mutableStateOf(0L)

    /** Products added from the user Product tab; reservations added via "Book it". */
    val cartList: SnapshotStateList<KartEntry> = mutableStateListOf()

    /** Completed orders (Firestore [UserFirestore.FIELD_MY_ORDER]). */
    val myOrderList: SnapshotStateList<UserPlacedOrder> = mutableStateListOf()

    private var nextProductId: Int = DEFAULT_NEXT_PRODUCT_ID
    private var nextReservationBusinessSeq: Int = 101
    private var nextReservationSlotSeq: Int = 201
    private var nextServiceProfileSeq: Int = 101
    private var nextServicePostSeq: Int = 201

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

    fun recomputeSequencesFromLoadedData() {
        nextProductId = (productList.maxOfOrNull { it.id } ?: (DEFAULT_NEXT_PRODUCT_ID - 1)) + 1
        nextServiceProfileSeq = serviceProfile?.id?.removePrefix("SRV-")?.toIntOrNull()?.plus(1) ?: 101
        nextServicePostSeq = (servicePostList.mapNotNull { it.id.removePrefix("POST-").toIntOrNull() }.maxOrNull()
            ?: (DEFAULT_NEXT_SERVICE_POST_SEQ - 1)) + 1
        nextReservationBusinessSeq = reservationPlaceList.firstOrNull()?.id
            ?.removePrefix("RES-")?.toIntOrNull()?.plus(1) ?: 101
        nextReservationSlotSeq = (reservationSlotList.mapNotNull {
            it.id.removePrefix("SLOT-").toIntOrNull()
        }.maxOrNull() ?: 200) + 1
    }

    /** Clears vendor inventory and browse lists (e.g. user login or logout). */
    fun resetForUserSession() {
        shopList.clear()
        productList.clear()
        reservationList.clear()
        reservationPlaceList.clear()
        reservationSlotList.clear()
        reservationBrowseList.clear()
        serviceProfile = null
        servicePostList.clear()
        walletVendor = 0L
        cartList.clear()
        myOrderList.clear()
        nextProductId = DEFAULT_NEXT_PRODUCT_ID
        nextReservationBusinessSeq = 101
        nextReservationSlotSeq = 201
        nextServiceProfileSeq = 101
        nextServicePostSeq = DEFAULT_NEXT_SERVICE_POST_SEQ
    }
}

sealed class KartEntry {
    abstract val lineId: String
    abstract val orderStatus: String

    data class ProductInCart(
        val product: VendorProduct,
        override val lineId: String = UUID.randomUUID().toString(),
        override val orderStatus: String = "Pending",
    ) : KartEntry()

    data class BookingPending(
        val reservation: VendorReservation,
        override val lineId: String = UUID.randomUUID().toString(),
        override val orderStatus: String = "Pending",
    ) : KartEntry()
}

private const val DEFAULT_NEXT_PRODUCT_ID = 101

private const val DEFAULT_NEXT_SERVICE_POST_SEQ = 201
