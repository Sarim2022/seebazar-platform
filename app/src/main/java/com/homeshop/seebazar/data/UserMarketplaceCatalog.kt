package com.homeshop.seebazar.data

/**
 * Loads all vendor (`type == Service`) listings from Firestore into [MarketplaceData] for the buyer home.
 * Clears and repopulates product, service post, and reservation browse lists only (cart is left intact).
 */
object UserMarketplaceCatalog {

    private const val AGGREGATE_PRODUCT_ID_START = 10_000

    fun refreshFromFirestore(
        marketplace: MarketplaceData,
        onComplete: (Throwable?) -> Unit,
    ) {
        marketplace.productList.clear()
        marketplace.servicePostList.clear()
        marketplace.reservationSlotList.clear()
        marketplace.reservationPlaceList.clear()
        marketplace.reservationBrowseList.clear()
        marketplace.shopList.clear()

        UserFirestore.usersCollection()
            .whereEqualTo(UserFirestore.FIELD_TYPE, "Service")
            .get()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onComplete(task.exception)
                    return@addOnCompleteListener
                }
                val snapshot = task.result
                if (snapshot == null) {
                    onComplete(null)
                    return@addOnCompleteListener
                }
                var nextProductId = AGGREGATE_PRODUCT_ID_START
                for (doc in snapshot.documents) {
                    val data = doc.data ?: continue
                    val extract = VendorFirestoreSync.extractVendorListingsForCatalog(data)
                    val vendorKey = doc.id
                    val shopMap = data[UserFirestore.FIELD_SHOP] as? Map<*, *>
                    val shopUpi = shopMap?.get("upiId")?.toString()?.trim().orEmpty()
                    val shopNameHint = shopMap?.get("shopName")?.toString()?.trim().orEmpty()
                    for (p in extract.products) {
                        val labeled = p.copy(
                            id = nextProductId++,
                            vendorShopName = p.vendorShopName.ifBlank { shopNameHint },
                            sourceVendorId = vendorKey,
                            vendorUpiId = shopUpi,
                        )
                        marketplace.productList.add(labeled)
                    }
                    for (post in extract.servicePosts) {
                        marketplace.servicePostList.add(
                            post.copy(id = "${vendorKey}_${post.id}"),
                        )
                    }
                    for ((biz, slot) in extract.reservationPairs) {
                        val scopedBiz = biz.copy(id = "${vendorKey}_${biz.id}")
                        val scopedSlot = slot.copy(id = "${vendorKey}_${slot.id}")
                        marketplace.reservationBrowseList.add(
                            ReservationBrowseEntry(
                                business = scopedBiz,
                                slot = scopedSlot,
                                vendorUid = vendorKey,
                                vendorUpiId = shopUpi,
                            ),
                        )
                    }
                }
                marketplace.recomputeSequencesFromLoadedData()
                onComplete(null)
            }
    }
}
