package com.homeshop.seebazar.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun decodeBitmapFromUriString(context: Context, uriString: String): Bitmap? {
    val s = uriString.trim()
    if (s.isEmpty()) return null
    return runCatching {
        if (s.startsWith("http://", ignoreCase = true) || s.startsWith("https://", ignoreCase = true)) {
            URL(s).openStream().use { BitmapFactory.decodeStream(it) }
        } else {
            val uri = Uri.parse(s)
            when (uri.scheme?.lowercase()) {
                "http", "https" -> URL(s).openStream().use { BitmapFactory.decodeStream(it) }
                else -> context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            }
        }
    }.getOrNull()
}

@Composable
fun rememberDecodedBitmap(uriString: String?): Bitmap? {
    val context = LocalContext.current
    var bitmap by remember(uriString) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(uriString) {
        bitmap = null
        if (uriString.isNullOrBlank()) return@LaunchedEffect
        bitmap = withContext(Dispatchers.IO) {
            decodeBitmapFromUriString(context, uriString)
        }
    }
    return bitmap
}
