package com.homeshop.seebazar.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val colorStops = arrayOf(
        0.0f to Color(0xFFF1F5F9), // BorderLight / base
        0.5f to Color(0xFFE2E8F0), // darker shimmer highlight
        1.0f to Color(0xFFF1F5F9)
    )

    val brush = Brush.linearGradient(
        colorStops = colorStops,
        start = Offset(10f, 10f),
        end = Offset(translateAnim, translateAnim)
    )

    return this.then(Modifier.background(brush))
}
