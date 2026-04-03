package com.homeshop.seebazar.servicehome.cardsfeature

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.servicehome.VendorReservation
import com.homeshop.seebazar.servicehome.VendorUi

@Composable
fun MyReservation(
    modifier: Modifier = Modifier,
    reservations: List<VendorReservation>,
) {
    if (reservations.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(VendorUi.ScreenBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No reservations yet",
                style = MaterialTheme.typography.bodyLarge,
                color = VendorUi.TextMuted,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VendorUi.ScreenBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(reservations, key = { index, _ -> index }) { _, reservation ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, VendorUi.CardStroke),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = reservation.venueName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VendorUi.TextDark,
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(VendorUi.ScreenBg, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                        ) {
                            Text(
                                text = "Date",
                                style = MaterialTheme.typography.labelMedium,
                                color = VendorUi.TextMuted,
                            )
                            Text(
                                text = reservation.date.ifBlank { "—" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VendorUi.BrandBlue,
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(VendorUi.ScreenBg, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                        ) {
                            Text(
                                text = "Time slot",
                                style = MaterialTheme.typography.labelMedium,
                                color = VendorUi.TextMuted,
                            )
                            Text(
                                text = reservation.timeSlot.ifBlank { "—" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VendorUi.BrandBlue,
                            )
                        }
                    }
                    if (reservation.numPeople.isNotBlank() || reservation.instructions.isNotBlank()) {
                        Spacer(modifier = Modifier.padding(vertical = 8.dp))
                        if (reservation.numPeople.isNotBlank()) {
                            Text(
                                text = "People: ${reservation.numPeople}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VendorUi.TextDark,
                            )
                        }
                        if (reservation.instructions.isNotBlank()) {
                            Text(
                                text = reservation.instructions,
                                style = MaterialTheme.typography.bodySmall,
                                color = VendorUi.TextMuted,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Price: ${reservation.price}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = VendorUi.TextDark,
                    )
                }
            }
        }
    }
}
