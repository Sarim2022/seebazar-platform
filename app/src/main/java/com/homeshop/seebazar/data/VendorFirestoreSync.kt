package com.homeshop.seebazar.data

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.homeshop.seebazar.servicehome.ProductCategory
import com.homeshop.seebazar.servicehome.ReservationBookingType
import com.homeshop.seebazar.servicehome.ReservationBusiness
import com.homeshop.seebazar.servicehome.ReservationBusinessType
import com.homeshop.seebazar.servicehome.ReservationSlot
import com.homeshop.seebazar.servicehome.ReservationSlotCategory
import com.homeshop.seebazar.servicehome.ServiceChargesType
import com.homeshop.seebazar.servicehome.ServiceProfession
import com.homeshop.seebazar.servicehome.VendorProduct
import com.homeshop.seebazar.servicehome.VendorServicePost
import com.homeshop.seebazar.servicehome.VendorServicePostCategory
import com.homeshop.seebazar.servicehome.VendorServiceProfile
import org.json.JSONArray
import org.json.JSONObject

/** Parsed listing rows from one vendor document (for user dashboard aggregation). */
data class VendorListingExtract(
    val products: List<VendorProduct>,
    val servicePosts: List<VendorServicePost>,
    val reservationPairs: List<Pair<ReservationBusiness, ReservationSlot>>,
)

/**
 * Maps vendor marketplace models to/from Firestore and JSON (SharedPreferences).
 */
object VendorFirestoreSync {

    /** Resolves UPI from a vendor [users] document map (root field, then shop / service / reservation). */
    fun resolveVendorUpiFromUserDocMap(data: Map<String, Any?>): String {
        val root = data[UserFirestore.FIELD_VENDOR_UPI]?.toString()?.trim().orEmpty()
        if (root.isNotBlank()) return root
        val shop = data[UserFirestore.FIELD_SHOP] as? Map<*, *>
        val shopUpi = shop?.get("upiId")?.toString()?.trim().orEmpty()
        if (shopUpi.isNotBlank()) return shopUpi
        val sp = data[UserFirestore.FIELD_SERVICE_PROFILE] as? Map<*, *>
        val spUpi = sp?.get("upiId")?.toString()?.trim().orEmpty()
        if (spUpi.isNotBlank()) return spUpi
        val rb = data[UserFirestore.FIELD_RESERVATION_BUSINESS] as? Map<*, *>
        return rb?.get("upiId")?.toString()?.trim().orEmpty()
    }

    fun fetchVendorUpiId(vendorUid: String, onResult: (String) -> Unit) {
        val uid = vendorUid.trim()
        if (uid.isEmpty()) {
            onResult("")
            return
        }
        UserFirestore.usersCollection().document(uid).get()
            .addOnSuccessListener { snap ->
                val data = snap.data
                if (data == null) {
                    onResult("")
                    return@addOnSuccessListener
                }
                onResult(resolveVendorUpiFromUserDocMap(data))
            }
            .addOnFailureListener { onResult("") }
    }

    private fun canonicalVendorUpi(marketplace: MarketplaceData): String {
        val shop = marketplace.shopList.firstOrNull()?.upiId?.trim().orEmpty()
        if (shop.isNotBlank()) return shop
        val svc = marketplace.serviceProfile?.upiId?.trim().orEmpty()
        if (svc.isNotBlank()) return svc
        return marketplace.reservationPlaceList.firstOrNull()?.upiId?.trim().orEmpty()
    }

    private fun boolFromFirestore(value: Any?): Boolean? = when (value) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        else -> null
    }

    fun applySnapshot(doc: DocumentSnapshot, marketplace: MarketplaceData): Boolean {
        val data = doc.data ?: return false
        return applyDataMap(data, marketplace)
    }

    /**
     * Reads shop products, service posts, and reservation slots from a vendor user document
     * without mutating [MarketplaceData]. Only active products/posts and active slots are included.
     */
    fun extractVendorListingsForCatalog(data: Map<String, Any?>): VendorListingExtract {
        val shopMap = data[UserFirestore.FIELD_SHOP] as? Map<*, *>
        val shopDetails = shopMap?.let { shopFromMap(it) }
        val shopName = shopDetails?.shopName.orEmpty()

        val products = mutableListOf<VendorProduct>()
        val shopEmbeddedProducts = shopMap?.get("products") as? List<*>
        fun addProduct(p: VendorProduct) {
            if (!p.isActive) return
            val labeled = if (p.vendorShopName.isBlank() && shopName.isNotBlank()) {
                p.copy(vendorShopName = shopName)
            } else {
                p
            }
            products.add(labeled)
        }
        if (shopMap != null && shopMap.containsKey("products")) {
            shopEmbeddedProducts?.forEach { item ->
                (item as? Map<*, *>)?.let { productFromMap(it) }?.let(::addProduct)
            }
        } else {
            (data[UserFirestore.FIELD_PRODUCTS] as? List<*>)?.forEach { item ->
                (item as? Map<*, *>)?.let { productFromMap(it) }?.let(::addProduct)
            }
        }

        val servicePosts = mutableListOf<VendorServicePost>()
        (data[UserFirestore.FIELD_SERVICE_POSTS] as? List<*>)?.forEach { item ->
            (item as? Map<*, *>)?.let { servicePostFromMap(it) }?.let { post ->
                if (post.isActive) servicePosts.add(post)
            }
        }
        if (servicePosts.isEmpty()) {
            val profile = (data[UserFirestore.FIELD_SERVICE_PROFILE] as? Map<*, *>)?.let(::serviceProfileFromMap)
            if (profile != null && profile.isAvailable) {
                val descExtras = buildString {
                    if (profile.serviceArea.isNotBlank()) {
                        append("Area: ${profile.serviceArea}")
                    }
                    if (profile.contactNumber.isNotBlank()) {
                        if (isNotEmpty()) append("\n")
                        append("Contact: ${profile.contactNumber}")
                    }
                }
                val description = listOfNotNull(
                    profile.shortDescription.takeIf { it.isNotBlank() },
                    descExtras.takeIf { it.isNotBlank() },
                ).joinToString("\n")
                val title = listOf(profile.profession.displayLabel, profile.providerName)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                    .ifBlank { profile.profession.displayLabel }
                servicePosts.add(
                    VendorServicePost(
                        id = profile.id,
                        title = title,
                        category = VendorServicePostCategory.Inspection,
                        description = description,
                        price = profile.baseCharge,
                        estimatedTime = "${profile.experienceYears} yrs · ${profile.chargesType.displayLabel}",
                        emergencyAvailable = false,
                        imageUri = profile.imageUri,
                        isActive = true,
                    ),
                )
            }
        }

        val reservationPairs = mutableListOf<Pair<ReservationBusiness, ReservationSlot>>()
        val resMap = data[UserFirestore.FIELD_RESERVATION_BUSINESS] as? Map<*, *>
        val business = resMap?.let { reservationBusinessFromMap(it) }
        val slotsList = mutableListOf<ReservationSlot>()
        val embeddedSlots = resMap?.get("reservationSlots") as? List<*>
        if (resMap != null && resMap.containsKey("reservationSlots")) {
            embeddedSlots?.forEach { item ->
                (item as? Map<*, *>)?.let { reservationSlotFromMap(it) }?.let { slotsList.add(it) }
            }
        } else {
            (data[UserFirestore.FIELD_RESERVATION_SLOTS] as? List<*>)?.forEach { item ->
                (item as? Map<*, *>)?.let { reservationSlotFromMap(it) }?.let { slotsList.add(it) }
            }
        }
        if (business != null) {
            slotsList.filter { it.isActive }.forEach { slot ->
                reservationPairs.add(business to slot)
            }
        }

        return VendorListingExtract(products, servicePosts, reservationPairs)
    }

    fun applyVendorJson(root: JSONObject, marketplace: MarketplaceData): Boolean {
        return runCatching {
            val data = jsonObjectToMap(root)
            applyDataMap(data, marketplace)
        }.getOrDefault(false)
    }

    fun pushVendorMarketplace(uid: String, marketplace: MarketplaceData, context: Context) {
        val updates = HashMap<String, Any>()
        val rootUpi = canonicalVendorUpi(marketplace)
        if (rootUpi.isNotBlank()) {
            updates[UserFirestore.FIELD_VENDOR_UPI] = rootUpi
        } else {
            updates[UserFirestore.FIELD_VENDOR_UPI] = FieldValue.delete()
        }
        updates[UserFirestore.FIELD_IS_SHOP_PROFILE] = marketplace.shopList.isNotEmpty()
        updates[UserFirestore.FIELD_IS_SERVICE_PROFILE] = marketplace.serviceProfile != null
        updates[UserFirestore.FIELD_IS_RESERVATION] = marketplace.reservationPlaceList.isNotEmpty()
        updates[UserFirestore.FIELD_SHOP] =
            if (marketplace.shopList.isNotEmpty()) {
                shopToMap(marketplace.shopList.first(), marketplace.productList)
            } else {
                FieldValue.delete()
            }
        updates[UserFirestore.FIELD_SERVICE_PROFILE] =
            marketplace.serviceProfile?.let(::serviceProfileToMap) ?: FieldValue.delete()
        updates[UserFirestore.FIELD_RESERVATION_BUSINESS] =
            marketplace.reservationPlaceList.firstOrNull()?.let { b ->
                reservationBusinessToMap(b, marketplace.reservationSlotList)
            } ?: FieldValue.delete()
        // Products live only under shop.products; strip legacy duplicate root field.
        updates[UserFirestore.FIELD_PRODUCTS] = FieldValue.delete()
        updates[UserFirestore.FIELD_SERVICE_POSTS] = marketplace.servicePostList.map(::servicePostToMap)
        updates[UserFirestore.FIELD_RESERVATION_SLOTS] = marketplace.reservationSlotList.map(::reservationSlotToMap)
        updates[UserFirestore.FIELD_WALLET_VENDOR] = marketplace.walletVendorList.toList()
        UserFirestore.usersCollection().document(uid).update(updates)
            .addOnSuccessListener {
                VendorPrefs.persistMarketplaceOnly(context, uid, marketplace)
            }
    }

    fun marketplaceToJson(marketplace: MarketplaceData): JSONObject {
        val o = JSONObject()
        val rootUpi = canonicalVendorUpi(marketplace)
        if (rootUpi.isNotBlank()) {
            o.put(UserFirestore.FIELD_VENDOR_UPI, rootUpi)
        }
        o.put(UserFirestore.FIELD_IS_SHOP_PROFILE, marketplace.shopList.isNotEmpty())
        o.put(UserFirestore.FIELD_IS_SERVICE_PROFILE, marketplace.serviceProfile != null)
        o.put(UserFirestore.FIELD_IS_RESERVATION, marketplace.reservationPlaceList.isNotEmpty())
        o.put(
            UserFirestore.FIELD_SHOP,
            marketplace.shopList.firstOrNull()?.let { shopToJson(it, marketplace.productList) },
        )
        o.put(UserFirestore.FIELD_SERVICE_PROFILE, marketplace.serviceProfile?.let { serviceProfileToJson(it) })
        o.put(
            UserFirestore.FIELD_RESERVATION_BUSINESS,
            marketplace.reservationPlaceList.firstOrNull()?.let {
                reservationBusinessToJson(it, marketplace.reservationSlotList)
            },
        )
        o.put(UserFirestore.FIELD_SERVICE_POSTS, JSONArray().apply {
            for (p in marketplace.servicePostList) put(servicePostToJson(p))
        })
        o.put(UserFirestore.FIELD_RESERVATION_SLOTS, JSONArray().apply {
            for (s in marketplace.reservationSlotList) put(reservationSlotToJson(s))
        })
        o.put(UserFirestore.FIELD_WALLET_VENDOR, JSONArray().apply {
            for (w in marketplace.walletVendorList) put(w)
        })
        return o
    }

    private fun applyDataMap(data: Map<String, Any?>, marketplace: MarketplaceData): Boolean {
        val rootUpiFallback = data[UserFirestore.FIELD_VENDOR_UPI]?.toString()?.trim().orEmpty()
        val shopMap = data[UserFirestore.FIELD_SHOP] as? Map<*, *>
        marketplace.shopList.clear()
        shopMap?.let { m ->
            shopFromMap(m)?.let { s ->
                val withUpi = if (s.upiId.isBlank() && rootUpiFallback.isNotBlank()) {
                    s.copy(upiId = rootUpiFallback)
                } else {
                    s
                }
                marketplace.shopList.add(withUpi)
            }
        }
        marketplace.productList.clear()
        val shopEmbeddedProducts = shopMap?.get("products") as? List<*>
        if (shopMap != null && shopMap.containsKey("products")) {
            shopEmbeddedProducts?.forEach { item ->
                (item as? Map<*, *>)?.let { productFromMap(it) }?.let { marketplace.productList.add(it) }
            }
        } else {
            (data[UserFirestore.FIELD_PRODUCTS] as? List<*>)?.forEach { item ->
                (item as? Map<*, *>)?.let { productFromMap(it) }?.let { marketplace.productList.add(it) }
            }
        }
        marketplace.serviceProfile = (data[UserFirestore.FIELD_SERVICE_PROFILE] as? Map<*, *>)?.let { m ->
            serviceProfileFromMap(m)?.let { p ->
                if (p.upiId.isBlank() && rootUpiFallback.isNotBlank()) p.copy(upiId = rootUpiFallback) else p
            }
        }
        marketplace.servicePostList.clear()
        (data[UserFirestore.FIELD_SERVICE_POSTS] as? List<*>)?.forEach { item ->
            (item as? Map<*, *>)?.let { servicePostFromMap(it) }?.let { marketplace.servicePostList.add(it) }
        }
        val resMap = data[UserFirestore.FIELD_RESERVATION_BUSINESS] as? Map<*, *>
        marketplace.reservationPlaceList.clear()
        resMap?.let { m ->
            reservationBusinessFromMap(m)?.let { b ->
                val withUpi = if (b.upiId.isBlank() && rootUpiFallback.isNotBlank()) {
                    b.copy(upiId = rootUpiFallback)
                } else {
                    b
                }
                marketplace.reservationPlaceList.add(withUpi)
            }
        }
        marketplace.reservationSlotList.clear()
        marketplace.reservationBrowseList.clear()
        val embeddedSlots = resMap?.get("reservationSlots") as? List<*>
        if (resMap != null && resMap.containsKey("reservationSlots")) {
            embeddedSlots?.forEach { item ->
                (item as? Map<*, *>)?.let { reservationSlotFromMap(it) }
                    ?.let { marketplace.reservationSlotList.add(it) }
            }
        } else {
            (data[UserFirestore.FIELD_RESERVATION_SLOTS] as? List<*>)?.forEach { item ->
                (item as? Map<*, *>)?.let { reservationSlotFromMap(it) }
                    ?.let { marketplace.reservationSlotList.add(it) }
            }
        }
        marketplace.walletVendorList.clear()
        (data[UserFirestore.FIELD_WALLET_VENDOR] as? List<*>)?.forEach { item ->
            item?.toString()?.takeIf { it.isNotBlank() }?.let { marketplace.walletVendorList.add(it) }
        }
        marketplace.recomputeSequencesFromLoadedData()
        return true
    }

    private fun jsonObjectToMap(o: JSONObject): Map<String, Any?> {
        val out = HashMap<String, Any?>()
        val keys = o.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            when (val v = o.get(k)) {
                is JSONObject -> out[k] = jsonObjectToMap(v)
                is JSONArray -> out[k] = jsonArrayToList(v)
                JSONObject.NULL -> out[k] = null
                else -> out[k] = v
            }
        }
        return out
    }

    private fun jsonArrayToList(a: JSONArray): List<Any?> {
        val list = ArrayList<Any?>()
        for (i in 0 until a.length()) {
            when (val v = a.get(i)) {
                is JSONObject -> list.add(jsonObjectToMap(v))
                is JSONArray -> list.add(jsonArrayToList(v))
                JSONObject.NULL -> list.add(null)
                else -> list.add(v)
            }
        }
        return list
    }

    private fun shopToJson(s: ShopDetails, products: List<VendorProduct> = emptyList()) = JSONObject().apply {
        put("shopName", s.shopName)
        put("vendorId", s.vendorId)
        put("ownerName", s.ownerName)
        put("address", s.address)
        put("city", s.city)
        put("postalCode", s.postalCode)
        put("isOpen", s.isOpen)
        put("upiId", s.upiId)
        put("products", JSONArray().apply { for (p in products) put(productToJson(p)) })
    }

    private fun shopToMap(s: ShopDetails, products: List<VendorProduct>) =
        shopToJson(s, products).let { jsonObjectToMap(it) }

    private fun shopFromMap(m: Map<*, *>): ShopDetails? {
        val name = m["shopName"]?.toString()?.trim().orEmpty()
        if (name.isBlank()) return null
        return ShopDetails(
            shopName = name,
            vendorId = m["vendorId"]?.toString().orEmpty(),
            ownerName = m["ownerName"]?.toString().orEmpty(),
            address = m["address"]?.toString().orEmpty(),
            city = m["city"]?.toString().orEmpty(),
            postalCode = m["postalCode"]?.toString().orEmpty(),
            isOpen = boolFromFirestore(m["isOpen"]) ?: false,
            upiId = m["upiId"]?.toString().orEmpty(),
        )
    }

    private fun productToJson(p: VendorProduct) = JSONObject().apply {
        put("id", p.id)
        put("name", p.name)
        put("description", p.description)
        put("mrpPrice", p.mrpPrice)
        put("imageUri", p.imageUri)
        put("brand", p.brand)
        put("category", p.category.name)
        put("unit", p.unit)
        put("shelfLife", p.shelfLife)
        put("quantityLeft", p.quantityLeft)
        put("isActive", p.isActive)
        put("vendorShopName", p.vendorShopName)
        put("sourceVendorId", p.sourceVendorId)
        put("vendorUpiId", p.vendorUpiId)
    }

    private fun productToMap(p: VendorProduct) = productToJson(p).let { jsonObjectToMap(it) }

    private fun productFromMap(m: Map<*, *>): VendorProduct? {
        val id = when (val v = m["id"]) {
            is Number -> v.toInt()
            is String -> v.toIntOrNull()
            else -> null
        } ?: return null
        val catName = m["category"]?.toString() ?: ProductCategory.Grocery.name
        val category = runCatching { ProductCategory.valueOf(catName) }.getOrDefault(ProductCategory.Grocery)
        return VendorProduct(
            id = id,
            name = m["name"]?.toString().orEmpty(),
            description = m["description"]?.toString().orEmpty(),
            mrpPrice = m["mrpPrice"]?.toString().orEmpty(),
            imageUri = m["imageUri"]?.toString(),
            brand = m["brand"]?.toString().orEmpty(),
            category = category,
            unit = m["unit"]?.toString().orEmpty(),
            shelfLife = m["shelfLife"]?.toString().orEmpty(),
            quantityLeft = m["quantityLeft"]?.toString().orEmpty(),
            isActive = boolFromFirestore(m["isActive"]) ?: true,
            imageDrawableRes = null,
            vendorShopName = m["vendorShopName"]?.toString().orEmpty(),
            sourceVendorId = m["sourceVendorId"]?.toString().orEmpty(),
            vendorUpiId = m["vendorUpiId"]?.toString().orEmpty(),
        )
    }

    private fun serviceProfileToJson(p: VendorServiceProfile) = JSONObject().apply {
        put("id", p.id)
        put("providerName", p.providerName)
        put("profession", p.profession.name)
        put("experienceYears", p.experienceYears)
        put("serviceArea", p.serviceArea)
        put("contactNumber", p.contactNumber)
        put("shortDescription", p.shortDescription)
        put("chargesType", p.chargesType.name)
        put("baseCharge", p.baseCharge)
        put("imageUri", p.imageUri)
        put("isAvailable", p.isAvailable)
        put("upiId", p.upiId)
    }

    private fun serviceProfileToMap(p: VendorServiceProfile) = serviceProfileToJson(p).let { jsonObjectToMap(it) }

    private fun serviceProfileFromMap(m: Map<*, *>): VendorServiceProfile? {
        val id = m["id"]?.toString() ?: return null
        val profName = m["profession"]?.toString() ?: ServiceProfession.Electrician.name
        val profession = runCatching { ServiceProfession.valueOf(profName) }.getOrDefault(ServiceProfession.Electrician)
        val chargesName = m["chargesType"]?.toString() ?: ServiceChargesType.PerVisit.name
        val charges = runCatching { ServiceChargesType.valueOf(chargesName) }.getOrDefault(ServiceChargesType.PerVisit)
        return VendorServiceProfile(
            id = id,
            providerName = m["providerName"]?.toString().orEmpty(),
            profession = profession,
            experienceYears = m["experienceYears"]?.toString().orEmpty(),
            serviceArea = m["serviceArea"]?.toString().orEmpty(),
            contactNumber = m["contactNumber"]?.toString().orEmpty(),
            shortDescription = m["shortDescription"]?.toString().orEmpty(),
            chargesType = charges,
            baseCharge = m["baseCharge"]?.toString().orEmpty(),
            imageUri = m["imageUri"]?.toString(),
            isAvailable = boolFromFirestore(m["isAvailable"]) ?: true,
            upiId = m["upiId"]?.toString().orEmpty(),
        )
    }

    private fun servicePostToJson(p: VendorServicePost) = JSONObject().apply {
        put("id", p.id)
        put("title", p.title)
        put("category", p.category.name)
        put("description", p.description)
        put("price", p.price)
        put("estimatedTime", p.estimatedTime)
        put("emergencyAvailable", p.emergencyAvailable)
        put("imageUri", p.imageUri)
        put("isActive", p.isActive)
    }

    private fun servicePostToMap(p: VendorServicePost) = servicePostToJson(p).let { jsonObjectToMap(it) }

    private fun servicePostFromMap(m: Map<*, *>): VendorServicePost? {
        val id = m["id"]?.toString() ?: return null
        val catName = m["category"]?.toString() ?: VendorServicePostCategory.Installation.name
        val cat = runCatching { VendorServicePostCategory.valueOf(catName) }
            .getOrDefault(VendorServicePostCategory.Installation)
        return VendorServicePost(
            id = id,
            title = m["title"]?.toString().orEmpty(),
            category = cat,
            description = m["description"]?.toString().orEmpty(),
            price = m["price"]?.toString().orEmpty(),
            estimatedTime = m["estimatedTime"]?.toString().orEmpty(),
            emergencyAvailable = m["emergencyAvailable"] as? Boolean ?: false,
            imageUri = m["imageUri"]?.toString(),
            isActive = boolFromFirestore(m["isActive"]) ?: true,
        )
    }

    private fun reservationBusinessToJson(
        b: ReservationBusiness,
        reservationSlots: List<ReservationSlot> = emptyList(),
    ) = JSONObject().apply {
        put("id", b.id)
        put("businessName", b.businessName)
        put("businessType", b.businessType.name)
        put("ownerName", b.ownerName)
        put("contactNumber", b.contactNumber)
        put("address", b.address)
        put("city", b.city)
        put("postalCode", b.postalCode)
        put("openTime", b.openTime)
        put("closeTime", b.closeTime)
        put("totalCapacity", b.totalCapacity)
        put("imageUri", b.imageUri)
        put("isOpen", b.isOpen)
        put("upiId", b.upiId)
        put(
            "reservationSlots",
            JSONArray().apply { for (s in reservationSlots) put(reservationSlotToJson(s)) },
        )
    }

    private fun reservationBusinessToMap(b: ReservationBusiness, slots: List<ReservationSlot>) =
        reservationBusinessToJson(b, slots).let { jsonObjectToMap(it) }

    private fun reservationBusinessFromMap(m: Map<*, *>): ReservationBusiness? {
        val id = m["id"]?.toString() ?: return null
        val typeName = m["businessType"]?.toString() ?: ReservationBusinessType.Restaurant.name
        val type = runCatching { ReservationBusinessType.valueOf(typeName) }
            .getOrDefault(ReservationBusinessType.Restaurant)
        return ReservationBusiness(
            id = id,
            businessName = m["businessName"]?.toString().orEmpty(),
            businessType = type,
            ownerName = m["ownerName"]?.toString().orEmpty(),
            contactNumber = m["contactNumber"]?.toString().orEmpty(),
            address = m["address"]?.toString().orEmpty(),
            city = m["city"]?.toString().orEmpty(),
            postalCode = m["postalCode"]?.toString().orEmpty(),
            openTime = m["openTime"]?.toString().orEmpty(),
            closeTime = m["closeTime"]?.toString().orEmpty(),
            totalCapacity = m["totalCapacity"]?.toString().orEmpty(),
            imageUri = m["imageUri"]?.toString(),
            isOpen = boolFromFirestore(m["isOpen"]) ?: true,
            upiId = m["upiId"]?.toString().orEmpty(),
        )
    }

    private fun reservationSlotToJson(s: ReservationSlot) = JSONObject().apply {
        put("id", s.id)
        put("title", s.title)
        put("category", s.category.name)
        put("description", s.description)
        put("capacity", s.capacity)
        put("price", s.price)
        put("availableDaily", s.availableDaily)
        put("specificDate", s.specificDate)
        put("startTime", s.startTime)
        put("endTime", s.endTime)
        put("totalAvailable", s.totalAvailable)
        put("bookingType", s.bookingType.name)
        put("imageUri", s.imageUri)
        put("isActive", s.isActive)
    }

    private fun reservationSlotToMap(s: ReservationSlot) = reservationSlotToJson(s).let { jsonObjectToMap(it) }

    private fun reservationSlotFromMap(m: Map<*, *>): ReservationSlot? {
        val id = m["id"]?.toString() ?: return null
        val catName = m["category"]?.toString() ?: ReservationSlotCategory.Table.name
        val cat = runCatching { ReservationSlotCategory.valueOf(catName) }
            .getOrDefault(ReservationSlotCategory.Table)
        val btName = m["bookingType"]?.toString() ?: ReservationBookingType.InstantBooking.name
        val bt = runCatching { ReservationBookingType.valueOf(btName) }
            .getOrDefault(ReservationBookingType.InstantBooking)
        return ReservationSlot(
            id = id,
            title = m["title"]?.toString().orEmpty(),
            category = cat,
            description = m["description"]?.toString().orEmpty(),
            capacity = m["capacity"]?.toString().orEmpty(),
            price = m["price"]?.toString().orEmpty(),
            availableDaily = m["availableDaily"] as? Boolean ?: false,
            specificDate = m["specificDate"]?.toString().orEmpty(),
            startTime = m["startTime"]?.toString().orEmpty(),
            endTime = m["endTime"]?.toString().orEmpty(),
            totalAvailable = m["totalAvailable"]?.toString().orEmpty(),
            bookingType = bt,
            imageUri = m["imageUri"]?.toString(),
            isActive = boolFromFirestore(m["isActive"]) ?: true,
        )
    }
}
