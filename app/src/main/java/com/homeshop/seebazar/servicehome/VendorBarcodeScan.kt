package com.homeshop.seebazar.servicehome

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

fun scanQrFromCameraBitmap(bitmap: Bitmap, onRawText: (String?) -> Unit) {
    val image = InputImage.fromBitmap(bitmap, 0)
    BarcodeScanning.getClient().process(image)
        .addOnSuccessListener { codes: List<Barcode> ->
            val text = codes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
            onRawText(text)
        }
        .addOnFailureListener { onRawText(null) }
}
