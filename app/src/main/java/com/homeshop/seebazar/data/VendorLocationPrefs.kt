package com.homeshop.seebazar.data

import android.content.Context

/**
 * Cached vendor location (from GPS + optional geocode) for onboarding and the home header.
 */
object VendorLocationPrefs {
    private const val PREFS = "seebazar_vendor_location"
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

    fun hasCoordinates(ctx: Context): Boolean {
        val p = prefs(ctx)
        return p.contains(KEY_LAT) && p.contains(KEY_LNG)
    }

    fun coordinates(ctx: Context): Pair<Double, Double>? {
        val p = prefs(ctx)
        if (!p.contains(KEY_LAT) || !p.contains(KEY_LNG)) return null
        return Pair(
            java.lang.Double.longBitsToDouble(p.getLong(KEY_LAT, 0)),
            java.lang.Double.longBitsToDouble(p.getLong(KEY_LNG, 0)),
        )
    }

    fun addressLine(ctx: Context): String = prefs(ctx).getString(KEY_LINE, "").orEmpty()

    fun city(ctx: Context): String = prefs(ctx).getString(KEY_CITY, "").orEmpty()

    fun postalCode(ctx: Context): String = prefs(ctx).getString(KEY_POSTAL, "").orEmpty()

    /** Single line for the top bar; falls back to coordinates. */
    fun displaySubtitle(ctx: Context): String {
        val line = addressLine(ctx).trim()
        if (line.isNotBlank()) return line
        val coords = coordinates(ctx) ?: return ""
        return "%.5f, %.5f".format(coords.first, coords.second)
    }
}
