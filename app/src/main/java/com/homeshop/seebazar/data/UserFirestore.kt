package com.homeshop.seebazar.data

import com.google.firebase.firestore.FirebaseFirestore

object UserFirestore {
    const val COLLECTION_USERS = "users"
    const val FIELD_TYPE = "type"
    const val FIELD_IS_LOGIN = "islogin"
    const val FIELD_NAME = "name"
    const val FIELD_EMAIL = "email"

    const val FIELD_IS_SHOP_PROFILE = "isShopprofile"
    const val FIELD_IS_SERVICE_PROFILE = "isServiceprofile"
    const val FIELD_IS_RESERVATION = "isReservation"

    const val FIELD_SHOP = "shop"
    const val FIELD_SERVICE_PROFILE = "serviceProfile"
    const val FIELD_RESERVATION_BUSINESS = "reservationBusiness"
    const val FIELD_PRODUCTS = "products"
    const val FIELD_SERVICE_POSTS = "servicePosts"
    const val FIELD_RESERVATION_SLOTS = "reservationSlots"
    /** Vendor wallet ledger entries; stored as an array (initially empty). */
    const val FIELD_WALLET_VENDOR = "WalletVendor"

    /** Buyer cart lines (serialized [KartEntry]). */
    const val FIELD_MY_KART = "MyKart"
    /** Buyer completed orders. */
    const val FIELD_MY_ORDER = "MyOrder"

    fun usersCollection() = FirebaseFirestore.getInstance().collection(COLLECTION_USERS)
}
