package com.homeshop.seebazar.data

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Reads last known location and writes location prefs. Runs callbacks on the main thread.
 */
object VendorLocationHelper {

    enum class PrefsTarget {
        Vendor,
        User,
    }

    fun fetchAndPersist(
        context: Context,
        permissionGranted: Boolean,
        onDone: () -> Unit,
        prefsTarget: PrefsTarget = PrefsTarget.Vendor,
    ) {
        val app = context.applicationContext
        if (!permissionGranted) {
            onDone()
            return
        }
        val fused = LocationServices.getFusedLocationProviderClient(app)
        fused.lastLocation.addOnCompleteListener { task ->
            val loc = task.result
            if (loc == null) {
                onDone()
                return@addOnCompleteListener
            }
            val lat = loc.latitude
            val lng = loc.longitude
            reverseGeocode(app, lat, lng) { line, city, postal ->
                when (prefsTarget) {
                    PrefsTarget.Vendor -> VendorLocationPrefs.save(app, lat, lng, line, city, postal)
                    PrefsTarget.User -> UserLocationPrefs.save(app, lat, lng, line, city, postal)
                }
                onDone()
            }
        }
    }

    private fun reverseGeocode(
        context: Context,
        lat: Double,
        lng: Double,
        onResult: (line: String, city: String, postal: String) -> Unit,
    ) {
        if (!Geocoder.isPresent()) {
            onResult("", "", "")
            return
        }
        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lng, 1) { addresses ->
                val a = addresses.firstOrNull()
                if (a == null) {
                    onResult("", "", "")
                } else {
                    onResult(
                        a.getAddressLine(0).orEmpty().trim(),
                        (a.locality ?: a.subAdminArea).orEmpty().trim(),
                        a.postalCode.orEmpty().trim(),
                    )
                }
            }
        } else {
            Executors.newSingleThreadExecutor().execute {
                @Suppress("DEPRECATION")
                val list = runCatching { geocoder.getFromLocation(lat, lng, 1) }.getOrNull()
                val a = list?.firstOrNull()
                val triple = if (a == null) {
                    Triple("", "", "")
                } else {
                    Triple(
                        a.getAddressLine(0).orEmpty().trim(),
                        (a.locality ?: a.subAdminArea).orEmpty().trim(),
                        a.postalCode.orEmpty().trim(),
                    )
                }
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onResult(triple.first, triple.second, triple.third)
                }
            }
        }
    }
}
