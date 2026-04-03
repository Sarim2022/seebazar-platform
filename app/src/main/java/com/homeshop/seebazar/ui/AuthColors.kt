package com.homeshop.seebazar.ui

import androidx.compose.ui.graphics.Color

object AuthColors {
    val AccentBlue = Color(0xFF155AC1)
    /** Same hue, lighter for unselected tabs */
    val AccentBlueLight = Color(0xFF155AC1).copy(alpha = 0.45f)

    /** Muted blue label on solid #155AC1 when fields incomplete (readable on same bg) */
    val PrimaryButtonDisabledText = Color(0xFF9EC5EB)
}
