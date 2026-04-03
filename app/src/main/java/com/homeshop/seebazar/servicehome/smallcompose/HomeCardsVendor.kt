package com.homeshop.seebazar.servicehome.smallcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.R // Replace with your R package
import com.homeshop.seebazar.servicehome.VendorCardRoute

@Composable
fun HomeCardsVendor(
    modifier: Modifier = Modifier,
    shopIsOpen: Boolean = false,
    onCardClick: (VendorCardRoute) -> Unit = {},
) {
    Column(
        modifier = modifier
            .background(Color.White) // Neutral off-white background
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VendorCategoryCard(
                title = "Products",
                staticBgId = R.drawable.product,
                onClick = { onCardClick(VendorCardRoute.Products) },
                modifier = Modifier.weight(1f),
                compact = true,
            )
            VendorCategoryCard(
                title = "My Services",
                staticBgId = R.drawable.services,
                onClick = { onCardClick(VendorCardRoute.Services) },
                modifier = Modifier.weight(1f),
                compact = true,
            )
            VendorCategoryCard(
                title = "My Reservations",
                staticBgId = R.drawable.reservations,
                onClick = { onCardClick(VendorCardRoute.Reservations) },
                modifier = Modifier.weight(1f),
                compact = true,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeCardsVendorPreview() {
    HomeCardsVendor()
}