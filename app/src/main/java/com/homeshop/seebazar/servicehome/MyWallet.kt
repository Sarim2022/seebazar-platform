package com.homeshop.seebazar.servicehome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWallet(modifier: Modifier = Modifier) {
    val transactions = listOf(
        TransactionItem("Order #88421", "Today, 02:30 PM", "+ ₹1,200", true),
        TransactionItem("Withdrawal", "Yesterday, 06:10 PM", "- ₹500", false),
        TransactionItem("Service Fee #S202", "Mar 30, 11:45 AM", "+ ₹850", true),
        TransactionItem("Refund", "Mar 29, 09:20 AM", "- ₹300", false),
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VendorUi.ScreenBg,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            VendorStandardTopBar(title = "Wallet")
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(VendorUi.ScreenBg)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                WalletBalanceCard(
                    balanceLabel = "Available Balance",
                    balanceAmount = "₹12,450",
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BalanceDetailItem("Shop Sale", "₹8,200", Modifier.weight(1f))
                    BalanceDetailItem("Services", "₹2,250", Modifier.weight(1f))
                    BalanceDetailItem("Reserved", "₹2,000", Modifier.weight(1f))
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All Transactions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = VendorUi.TextDark,
                )
            }
            items(transactions) { transaction ->
                MinimalTransactionRow(transaction)
            }
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

@Composable
fun BalanceDetailItem(label: String, amount: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, VendorUi.CardStroke),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = VendorUi.TextMuted,
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = VendorUi.TextDark,
            )
        }
    }
}

data class TransactionItem(
    val title: String,
    val date: String,
    val amount: String,
    val isCredit: Boolean,
)

@Composable
fun MinimalTransactionRow(item: TransactionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (item.isCredit) Color(0xFFEAF8EE) else Color(0xFFFFF1F1),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (item.isCredit) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                contentDescription = null,
                tint = if (item.isCredit) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                modifier = Modifier.size(18.dp),
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = VendorUi.TextDark,
            )
            Text(
                text = item.date,
                style = MaterialTheme.typography.bodySmall,
                color = VendorUi.TextMuted,
            )
        }

        Text(
            text = item.amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (item.isCredit) Color(0xFF2E7D32) else Color(0xFFD32F2F),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWallet() {
    MaterialTheme {
        MyWallet()
    }
}
