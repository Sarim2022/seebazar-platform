package com.homeshop.seebazar.common

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

private const val RECEIPT_CACHE_SUBDIR = "receipts"

fun saveBitmapToCachePng(context: Context, bitmap: Bitmap, fileName: String): Uri {
    val dir = File(context.cacheDir, RECEIPT_CACHE_SUBDIR).apply { mkdirs() }
    val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    val file = File(dir, safeName)
    FileOutputStream(file).use { out ->
        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
            throw IllegalStateException("PNG compression failed")
        }
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}

fun sharePngImage(context: Context, uri: Uri, chooserTitle: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        clipData = ClipData.newUri(context.contentResolver, "receipt", uri)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}
