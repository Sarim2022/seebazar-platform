package com.homeshop.seebazar.servicehome.smallcompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.homeshop.seebazar.data.ShopDetails
import com.homeshop.seebazar.servicehome.VendorUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailsScreen(
    shops: SnapshotStateList<ShopDetails>,
    shopIndex: Int,
    onBack: () -> Unit,
) {
    val shop = shops.getOrNull(shopIndex)
    if (shop == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var shopName by remember(shop) { mutableStateOf(shop.shopName) }
    var vendorId by remember(shop) { mutableStateOf(shop.vendorId) }
    var ownerName by remember(shop) { mutableStateOf(shop.ownerName) }
    var address by remember(shop) { mutableStateOf(shop.address) }
    var city by remember(shop) { mutableStateOf(shop.city) }
    var postalCode by remember(shop) { mutableStateOf(shop.postalCode) }
    var isOpen by remember(shop) { mutableStateOf(shop.isOpen) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Shop details",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VendorUi.TopBarBg,
                    titleContentColor = VendorUi.TextDark,
                    navigationIconContentColor = VendorUi.BrandBlue,
                ),
            )
        },
        containerColor = VendorUi.ScreenBg,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("Shop name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = vendorId,
                onValueChange = { vendorId = it },
                label = { Text("Vendor ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text("Owner name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = postalCode,
                onValueChange = { postalCode = it },
                label = { Text("Postal code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Shop status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = VendorUi.TextDark,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (isOpen) "Open" else "Closed",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VendorUi.TextMuted,
                    )
                    Switch(
                        checked = isOpen,
                        onCheckedChange = { isOpen = it },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    shops[shopIndex] = ShopDetails(
                        shopName = shopName.trim(),
                        vendorId = vendorId.trim(),
                        ownerName = ownerName.trim(),
                        address = address.trim(),
                        city = city.trim(),
                        postalCode = postalCode.trim(),
                        isOpen = isOpen,
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VendorUi.BrandBlue),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text("Save")
            }
        }
    }
}
