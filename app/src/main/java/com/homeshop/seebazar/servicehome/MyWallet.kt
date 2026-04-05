package com.homeshop.seebazar.servicehome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWallet(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VendorUi.ScreenBg,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            VendorStandardTopBar(title = "Wallet")
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VendorUi.ScreenBg)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WalletBalanceCard(
                balanceLabel = "Available Balance",
                balanceAmount = "₹0",
            )
            Text(
                text = "Transactions will appear here once you receive payments.",
                style = MaterialTheme.typography.bodyMedium,
                color = VendorUi.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            )
        }
    }
}

@Composable
private fun WalletBalanceCard(
    balanceLabel: String,
    balanceAmount: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, VendorUi.CardStroke),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFFDBEAFE),
                            Color(0xFFE0F2FE),
                            Color(0xFFF0F9FF),
                        ),
                    ),
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(20.dp),
        ) {
            Column {
                Text(
                    text = balanceLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VendorUi.TextMuted,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = balanceAmount,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = VendorUi.TextDark,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWallet() {
    MaterialTheme {
        MyWallet()
    }
}
