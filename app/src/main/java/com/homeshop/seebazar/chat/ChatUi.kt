package com.homeshop.seebazar.chat

import androidx.compose.ui.graphics.Color
import com.homeshop.seebazar.servicehome.VendorUi

/**
 * Shared visual tokens for vendor + user chat surfaces (quick-commerce style: clean, airy, crisp).
 */
object ChatUi {
    val ScreenBg = Color(0xFFF3F4F6)
    val Surface = Color.White
    val Hairline = Color(0xFFE5E7EB)
    val BorderSubtle = Color(0xFFE8EAED)
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val TextTertiary = Color(0xFF9CA3AF)
    val Brand = VendorUi.BrandBlue
    val BrandSoft = Color(0xFFE8F0FE)
    val BubbleMine = Brand
    val BubbleMineText = Color.White
    val BubbleOther = Color.White
    val BubbleOtherStroke = Color(0xFFE5E7EB)
    val InputSurface = Color(0xFFF9FAFB)
    val SendFab = Brand
    val EmptyIconBg = Color(0xFFF3F4F6)
    val SearchPlaceholder = TextSecondary
}
