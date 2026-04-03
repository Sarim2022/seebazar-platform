package com.homeshop.seebazar.servicehome.smallcompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeshop.seebazar.ui.AuthColors

@Composable
fun TodayStatus(
    modifier: Modifier = Modifier,
    todayWorkSummary: String? = null,
) {
    val subtitleColor = Color(0xFF9CA3AF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = modifier.weight(1f) // take remaining space
        ) {
            Text(
                text = "Today",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AuthColors.AccentBlue,
            )

            Text(
                text = if (todayWorkSummary.isNullOrBlank()) {
                    "No active orders today"
                } else {
                    todayWorkSummary
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = subtitleColor,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Button(
            onClick = {
                // add action
            },
            modifier = Modifier
                .padding(start = 8.dp)
                .height(40.dp)
                .widthIn(min = 70.dp), // optional minimum width
            shape = RoundedCornerShape(12.dp),

                    colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF155AC1) // background color
                    )
        ) {
            Text(
                text = "POST",
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayStatusNoWorkPreview() {
    TodayStatus()
}

@Preview(showBackground = true)
@Composable
private fun TodayStatusWithWorkPreview() {
    TodayStatus(todayWorkSummary = "3 orders to pack · 1 pickup at 4:00 PM")
}