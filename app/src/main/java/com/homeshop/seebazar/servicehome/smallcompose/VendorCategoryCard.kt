package com.homeshop.seebazar.servicehome.smallcompose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeshop.seebazar.R // Replace with your R package


@Composable
fun VendorCategoryCard(
    title: String,
    staticBgId: Int,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val titleSize = if (compact) 14.sp else 18.sp
    val cardHeight = if (compact) 100.dp else 130.dp
    val imageSize = if (compact) 42.dp else 60.dp
    val corner = if (compact) 16.dp else 20.dp
    val textEndPadding = if (compact) 42.dp else 56.dp

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            ),
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Title at top-left (clean, readable)
            Text(
                text = title,
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                overflow = TextOverflow.Ellipsis,
                lineHeight = if (compact) 16.sp else 20.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(end = textEndPadding)
            )

            // Image bottom-end, fully inside card
            Image(
                painter = painterResource(id = staticBgId),
                contentDescription = null,
                modifier = Modifier
                    .size(imageSize)
                    .align(Alignment.BottomEnd),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun VendorCategoryCardPreview() {
    VendorCategoryCard(
        title = "My Skjdfndfoifhop",
        staticBgId = R.drawable.store
    )
}


