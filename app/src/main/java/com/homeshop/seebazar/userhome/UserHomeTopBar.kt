package com.homeshop.seebazar.userhome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DummyLocationLine = "12 Market Street, Downtown"

/** Top row: home + location (same pattern as vendor [HomeTopBar]) and profile. */
@Composable
fun UserHomeTopBar(
    onProfileClick: () -> Unit = {},
) {
    val brandBlue = Color(0xFF155AC1)
    val textMuted = Color(0xFF64748B)
    val textDark = Color(0xFF0F172A)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // LEFT SECTION: Address
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn, // Map pin icon matches image better
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Home",
                        fontSize = 20.sp, // Slightly larger for that "Bold" header look
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "A-114 Street 1/1 Bhagirathi Vihar...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f), // Slightly transparent white
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // RIGHT SECTION: Action Icons
            Row(verticalAlignment = Alignment.CenterVertically) {


                // 3. Profile Initial Circle — opens vendor settings
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onProfileClick() },
                    shape = CircleShape,
                    color = Color(0xFFFDEFD5), // Cream color from image
                    border = BorderStroke(1.dp, Color(0xFF0090FF)), // Gold border
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "S",
                            color = Color(0xFF2196F3), // Brownish text
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}
