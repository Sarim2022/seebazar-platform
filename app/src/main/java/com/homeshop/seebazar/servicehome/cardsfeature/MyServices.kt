package com.homeshop.seebazar.servicehome.cardsfeature

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.homeshop.seebazar.servicehome.VendorServiceItem
import com.homeshop.seebazar.servicehome.VendorUi

@Composable
fun MyServices(
    modifier: Modifier = Modifier,
    services: List<VendorServiceItem>,
) {
    if (services.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(VendorUi.ScreenBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No services yet",
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
        itemsIndexed(services, key = { index, _ -> index }) { _, service ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, VendorUi.CardStroke),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = service.serviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VendorUi.TextDark,
                    )
                    Spacer(modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        text = "Fixed price",
                        style = MaterialTheme.typography.labelMedium,
                        color = VendorUi.TextMuted,
                    )
                    Text(
                        text = service.priceFixed,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = VendorUi.BrandBlue,
                    )
                    if (service.availability.isNotBlank()) {
                        Spacer(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "Availability: ${service.availability}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VendorUi.TextDark,
                        )
                    }
                    if (service.instruction.isNotBlank()) {
                        Spacer(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = service.instruction,
                            style = MaterialTheme.typography.bodySmall,
                            color = VendorUi.TextMuted,
                        )
                    }
                }
            }
        }
    }
}
