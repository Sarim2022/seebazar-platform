package com.homeshop.seebazar.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.servicehome.VendorUi

private val VendorTopBarSideSlotMin = 48.dp

@Composable
fun VendorCommonTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    containerColor: Color = VendorUi.TopBarBg,
    dividerColor: Color = VendorUi.CardStroke,
    actions: @Composable RowScope.() -> Unit = {}
) {
    // Window insets: when this bar is used inside [VendorHome], the parent Scaffold already applies
    // safe-area padding via [contentModifier] — do not add status-bar padding here or it doubles on
    // Chats / Orders. Standalone screens must use the same parent padding (see MyWallet in VendorHome).
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = VendorUi.TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.widthIn(min = VendorTopBarSideSlotMin),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = dividerColor,
        )
    }
}