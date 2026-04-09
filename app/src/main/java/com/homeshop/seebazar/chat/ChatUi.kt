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
    val BubbleMineGradientStart = Color(0xFF007BFF) // Electric blue
    val BubbleMineGradientEnd = Color(0xFF00D2D3) // Deep cyan
    val BubbleMineText = Color.White
    val BubbleOther = Color(0xFFFAFAFA) // Off-white/light-gray
    val BubbleOtherText = Color(0xFF333333) // Charcoal text
    val BubbleOtherStroke = Color(0xFFE5E7EB)
    val InputSurface = Color(0xFFFFFFFF)
    val SendFab = Color(0xFF007BFF)
    val EmptyIconBg = Color(0xFFF3F4F6)
    val SearchPlaceholder = TextSecondary
}
