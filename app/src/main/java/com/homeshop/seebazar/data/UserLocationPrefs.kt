package com.homeshop.seebazar.data

import android.content.Context

/**
 * Cached user (buyer) location for the home header; separate from vendor prefs.
 */
object UserLocationPrefs {
    private const val PREFS = "seebazar_user_location"
    private const val KEY_LAT = "lat"
    private const val KEY_LNG = "lng"
    private const val KEY_LINE = "address_line"
    private const val KEY_CITY = "city"
    private const val KEY_POSTAL = "postal"

    private fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun clear(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }

    fun save(
        ctx: Context,
        latitude: Double,
        longitude: Double,
        addressLine: String,
        city: String,
        postalCode: String,
    ) {
        prefs(ctx).edit()
            .putLong(KEY_LAT, java.lang.Double.doubleToRawLongBits(latitude))
            .putLong(KEY_LNG, java.lang.Double.doubleToRawLongBits(longitude))
            .putString(KEY_LINE, addressLine)
            .putString(KEY_CITY, city)
            .putString(KEY_POSTAL, postalCode)
            .apply()
    }

    fun city(ctx: Context): String = prefs(ctx).getString(KEY_CITY, "").orEmpty()

    fun displaySubtitle(ctx: Context): String {
        val line = prefs(ctx).getString(KEY_LINE, "").orEmpty().trim()
        if (line.isNotBlank()) return line
        val p = prefs(ctx)
        if (!p.contains(KEY_LAT) || !p.contains(KEY_LNG)) return ""
        val lat = java.lang.Double.longBitsToDouble(p.getLong(KEY_LAT, 0))
        val lng = java.lang.Double.longBitsToDouble(p.getLong(KEY_LNG, 0))
        return "%.5f, %.5f".format(lat, lng)
    }
}
