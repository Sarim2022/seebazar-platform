package com.homeshop.seebazar.data

import org.json.JSONObject

private val UuidRegex = Regex("^[0-9a-fA-F-]{36}$")

private fun legacyOrderIdFromPlainText(t: String): String? {
    val prefix = "seebazar:order:"
    if (t.startsWith(prefix, ignoreCase = true)) {
        return t.substring(prefix.length).trim().takeIf { it.isNotEmpty() }
    }
    if (UuidRegex.matches(t)) return t
    return null
}

/**
 * Order pickup QRs must stay **short**: vendor scan uses [androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview],
 * which yields a small bitmap; dense JSON QRs often fail ML Kit decode → "Invalid order QR".
 * Full order data lives in Firestore; the scan only needs [UserPlacedOrder.orderId].
 */
object OrderQrPayload {

    /** Plain text encoded in the customer’s QR (easy to scan; parsed by [parse]). */
    fun scannablePayload(order: UserPlacedOrder): String =
        "seebazar:order:${order.orderId.trim()}"

    fun buildJson(order: UserPlacedOrder, @Suppress("UNUSED_PARAMETER") buyerUid: String): String =
        scannablePayload(order)

    /**
     * Returns [ParsedQr] when JSON is valid; [orderId] may still come from legacy formats.
     */
    fun parse(raw: String?): ParsedQr? {
        if (raw.isNullOrBlank()) return null
        val t = raw.trim()
        runCatching {
            if (t.startsWith("{")) {
                val o = JSONObject(t)
                val orderId = o.optString("orderId", "").trim()
                if (orderId.isNotEmpty()) {
                    return ParsedQr(
                        orderId = orderId,
                        vendorIdFromQr = o.optString("vendorId", "").trim().ifBlank { null },
                        buyerUidFromQr = o.optString("buyerUid", "").trim().ifBlank { null },
                    )
                }
            }
        }
        val legacy = legacyOrderIdFromPlainText(t)
        if (legacy != null) {
            return ParsedQr(orderId = legacy, vendorIdFromQr = null, buyerUidFromQr = null)
        }
        return null
    }
}

data class ParsedQr(
    val orderId: String,
    val vendorIdFromQr: String?,
    val buyerUidFromQr: String?,
)
