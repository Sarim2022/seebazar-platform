package com.homeshop.seebazar.servicehome

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Matches vendor home: [HomeTopBar] brand blue and light screen tint. */
object VendorUi {
    val BrandBlue = Color(0xFF0EA5E9)
    /** Same as [com.homeshop.seebazar.servicehome.smallcompose.HomeItemListView] screen tint. */
    val ScreenBg = Color(0xFFF8FAFC)
    val TextDark = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val CardStroke = Color(0xFFF1F5F9)
    val TopBarBg = Color.White
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorStandardTopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val zero = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = VendorUi.TextDark,
            )
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = VendorUi.TopBarBg,
            titleContentColor = VendorUi.TextDark,
            actionIconContentColor = VendorUi.BrandBlue,
        ),
        windowInsets = zero,
    )
}
