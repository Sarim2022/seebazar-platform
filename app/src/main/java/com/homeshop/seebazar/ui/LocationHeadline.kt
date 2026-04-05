package com.homeshop.seebazar.ui

/** Single-line headline: at most [maxChars] graphemes, then ellipsis (…). */
fun ellipsizeLocationHeadline(raw: String, maxChars: Int = 13, blankFallback: String = "Location"): String {
    val base = raw.ifBlank { blankFallback }.trim()
    if (base.length <= maxChars) return base
    return base.take(maxChars) + "…"
}
