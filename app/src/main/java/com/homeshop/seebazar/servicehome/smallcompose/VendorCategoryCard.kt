package com.homeshop.seebazar.servicehome.smallcompose

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeshop.seebazar.R

@Composable
fun VendorCategoryCard(
    title: String,
    staticBgId: Int,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val cardHeight = if (compact) 110.dp else 140.dp
    val imageSize = if (compact) 60.dp else 85.dp
    val corner = 20.dp

    ElevatedCard(
        modifier = modifier
            .width(if (compact) 105.dp else 125.dp)
            .height(cardHeight)
            .border(
                1.dp,
                Color(0xFFD9D9D9),
                RoundedCornerShape(corner)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            ),
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Image Section
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = staticBgId),
                    contentDescription = null,
                    modifier = Modifier.size(imageSize),
                    contentScale = ContentScale.Fit
                )
            }

            // Text Section
//            Text(
//                text = title,
//                fontSize = fontSize,
//                fontWeight = FontWeight.Black,
//                color = Color(0xFF5D5D5D),
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 12.dp, start = 8.dp, end = 8.dp),
//                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
//                lineHeight = 16.sp
//            )
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