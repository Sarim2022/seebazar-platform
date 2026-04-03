package com.homeshop.seebazar.servicehome.smallcompose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeshop.seebazar.R

private val DummyLocationLine = "12 Market Street, Downtown"
private val DummyShopName = "FreshMart Provisions"
private val DummyShopCode = "VND-88421"
private val DummyOwnerName = "Ahmed Khan"
private val DummyFullAddress =
    "Shop 4, Ground Floor, Riverside Plaza, 12 Market Street, Downtown, Karachi 75500"

@Composable
fun HomeTopBar(
    shopIsOpen: Boolean = false,
    onMoonClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val brandBlue = Color(0xFF155AC1)
    val textMuted = Color(0xFF64748B)
    val textDark = Color(0xFF0F172A)

    val gradientColors = listOf(
        Color(0xFFDBEAFE),
        Color(0xFFE0F2FE),
        Color(0xFFF0F9FF),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White)
            .padding(start = 16.dp, end =16.dp , top = 8.dp, bottom = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = brandBlue,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Home",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textDark,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DummyLocationLine,
                    fontSize = 13.sp,
                    color = textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onProfileClick() },
                shape = CircleShape,
                color = brandBlue,
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                ) {
                    Image(
                        painter = painterResource(R.drawable.store),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .height(36.dp),
                        contentScale = ContentScale.Fit,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = DummyShopName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDark,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF),
                        ) {
                            Text(
                                text = DummyShopCode,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = brandBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = DummyOwnerName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textMuted,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = DummyFullAddress,
                        fontSize = 13.sp,
                        color = textMuted,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    StatusChip(shopIsOpen = shopIsOpen)
                }
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
    }
}

@Composable
private fun StatusChip(shopIsOpen: Boolean) {
    val bg = if (shopIsOpen) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
    val fg = if (shopIsOpen) Color(0xFF166534) else Color(0xFF991B1B)
    val label = if (shopIsOpen) "Open Now" else "Closed"

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeTopBarPreview() {
    HomeTopBar(shopIsOpen = true)
}
