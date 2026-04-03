package com.homeshop.seebazar.servicehome.smallcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.R

private val TextMuted = Color(0xFF6B7280)

private data class InsightTab(
    val title: String,
    val iconRes: Int,
    val placeholderMessage: String,
)

private val insightTabs = listOf(
    InsightTab(
        title = "Today Earnings",
        iconRes = R.drawable.income,
        placeholderMessage = "Today Earnings — content coming soon.",
    ),
    InsightTab(
        title = "Top Products",
        iconRes = R.drawable.toprated,
        placeholderMessage = "Top Products — content coming soon.",
    ),
    InsightTab(
        title = "Today Status",
        iconRes = R.drawable.statusorder,
        placeholderMessage = "Today Status — content coming soon.",
    ),
    InsightTab(
        title = "Shop",
        iconRes = R.drawable.shopcarks,
        placeholderMessage = "Shop — content coming soon.",
    ),
)

/**
 * Blinkit-style category row: icon above label, underline when selected; no horizontal scroll.
 * Bottom panel shows placeholder for the selected tab.
 */
@Composable
fun HomeItemListView(modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            insightTabs.forEachIndexed { index, tab ->
                InsightTabItem(
                    title = tab.title,
                    iconRes = tab.iconRes,
                    selected = index == selectedIndex,
                    onClick = { selectedIndex = index },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 2.dp, end = 2.dp, top = 0.dp, bottom = 0.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            val tab = insightTabs[selectedIndex]
            Text(
                text = tab.placeholderMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun HomeItemListViewPreview() {
    HomeItemListView(modifier = Modifier.height(280.dp))
}
