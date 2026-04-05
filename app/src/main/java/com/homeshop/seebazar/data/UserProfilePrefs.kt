package com.homeshop.seebazar.data

import android.content.Context

/**
 * Caches the signed-in user's Firestore profile fields for fast access after login.
 */
object UserProfilePrefs {
    private const val PREFS = "seebazar_user_profile"
    private const val KEY_UID = "uid"
    private const val KEY_NAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_TYPE = "type"

    private fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun clear(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }

    fun persist(ctx: Context, uid: String, name: String, email: String, accountType: String) {
        prefs(ctx).edit()
            .putString(KEY_UID, uid)
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_TYPE, accountType)
            .apply()
    }

    fun cachedDisplayName(ctx: Context): String = prefs(ctx).getString(KEY_NAME, "").orEmpty()

    fun cachedEmail(ctx: Context): String = prefs(ctx).getString(KEY_EMAIL, "").orEmpty()
}
