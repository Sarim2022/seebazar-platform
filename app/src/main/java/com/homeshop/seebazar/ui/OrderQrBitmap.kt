package com.homeshop.seebazar.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

fun encodeOrderQrBitmap(content: String, sizePx: Int): Bitmap? {
    if (content.isBlank() || sizePx <= 0) return null
    return runCatching {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val w = bits.width
        val h = bits.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                pixels[y * w + x] = if (bits[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }
        Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, w, 0, 0, w, h)
        }
    }.getOrNull()
}

@Composable
fun rememberOrderQrBitmap(content: String, sizePx: Int): Bitmap? =
    remember(content, sizePx) { encodeOrderQrBitmap(content, sizePx) }
