package com.homeshop.seebazar.data

import android.content.Context
import org.json.JSONObject

/**
 * Caches the signed-in vendor's profile and marketplace snapshot for fast reload.
 */
object VendorPrefs {
    private const val PREFS = "seebazar_vendor"
    private const val KEY_UID = "uid"
    private const val KEY_NAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_PAYLOAD = "vendor_payload_json"

    private fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun clear(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }

    fun persist(ctx: Context, uid: String, name: String, email: String, marketplace: MarketplaceData) {
        val json = VendorFirestoreSync.marketplaceToJson(marketplace)
        json.put("uid", uid)
        json.put("name", name)
        json.put("email", email)
        prefs(ctx).edit()
            .putString(KEY_UID, uid)
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PAYLOAD, json.toString())
            .apply()
    }

    fun persistMarketplaceOnly(ctx: Context, uid: String, marketplace: MarketplaceData) {
        val p = prefs(ctx)
        val name = p.getString(KEY_NAME, "").orEmpty()
        val email = p.getString(KEY_EMAIL, "").orEmpty()
        val cachedUid = p.getString(KEY_UID, null)
        val useUid = if (cachedUid.isNullOrBlank()) uid else cachedUid
        persist(ctx, useUid, name, email, marketplace)
    }

    fun loadIntoMarketplace(ctx: Context, marketplace: MarketplaceData): Boolean {
        val raw = prefs(ctx).getString(KEY_PAYLOAD, null) ?: return false
        return runCatching {
            val o = JSONObject(raw)
            VendorFirestoreSync.applyVendorJson(o, marketplace)
        }.getOrDefault(false)
    }

    fun cachedUid(ctx: Context): String? = prefs(ctx).getString(KEY_UID, null)?.takeIf { it.isNotBlank() }

    fun cachedDisplayName(ctx: Context): String = prefs(ctx).getString(KEY_NAME, "").orEmpty()
}
