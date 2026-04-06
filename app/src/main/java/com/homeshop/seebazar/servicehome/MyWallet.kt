import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.common.VendorCommonTopBar
import com.homeshop.seebazar.servicehome.VendorUi
import com.homeshop.seebazar.ui.theme.SeebazarTheme

private val WalletScreenBg = Color(0xFFF7F8FA)
private val ActionChipBg = Color(0xFFF1F4F9)
private val ViewAllBlue = Color(0xFF2F80ED)
private val AmountGreen = Color(0xFF009667)
private val HeroGradientTop = Color(0xFF2F80ED)
private val HeroGradientBottom = Color(0xFF56CCF2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWalletScreen(modifier: Modifier = Modifier) {
    val recentTransactions = remember { emptyList<TransactionData>() }

    Scaffold(
        modifier = modifier,
        containerColor = WalletScreenBg,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            VendorCommonTopBar(
                title = "My Wallet",
                onBackClick = { /* navController.popBackStack() */ },
                containerColor = Color.White,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                WalletHeroBalanceCard(
                    balanceText = "₹ 0.0",
                    onWithdrawClick = { },
                    onViewBreakdownClick = { },
                )
            }
            item {
                WalletQuickActionRow(
                    onSettingsClick = { },
                    onServicesClick = { },
                    onBookingsClick = { },
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VendorUi.TextDark,
                    )
                    TextButton(onClick = { }) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = ViewAllBlue,
                        )
                    }
                }
            }
            if (recentTransactions.isEmpty()) {
                item {
                    Text(
                        text = "No transactions yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VendorUi.TextMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                items(
                    items = recentTransactions,
                    key = { "${it.title}-${it.time}-${it.amount}" },
                ) { tx ->
                    TransactionItem(transaction = tx)
                }
            }
        }
    }
}

@Composable
private fun WalletHeroBalanceCard(
    balanceText: String,
    onWithdrawClick: () -> Unit,
    onViewBreakdownClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(HeroGradientTop, HeroGradientBottom),
                    ),
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(20.dp),
        ) {
            Column {
                Text(
                    text = "Total Earnings",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.88f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = balanceText,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onWithdrawClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = HeroGradientTop,
                        ),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text(
                            text = "Download",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    TextButton(onClick = onViewBreakdownClick) {
                        Text(
                            text = "View Breakdown",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletQuickActionRow(
    onSettingsClick: () -> Unit,
    onServicesClick: () -> Unit,
    onBookingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        WalletQuickActionItem(
            label = "Settings",
            icon = Icons.Outlined.Settings,
            onClick = onSettingsClick,
        )
        WalletQuickActionItem(
            label = "Services",
            icon = Icons.Outlined.HomeRepairService,
            onClick = onServicesClick,
        )
        WalletQuickActionItem(
            label = "Bookings",
            icon = Icons.Outlined.CalendarMonth,
            onClick = onBookingsClick,
        )
    }
}

@Composable
private fun WalletQuickActionItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = ActionChipBg,
            modifier = Modifier.size(52.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = VendorUi.TextDark,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = VendorUi.TextDark,
        )
    }
}

@Composable
fun TransactionItem(transaction: TransactionData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = ActionChipBg,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (transaction.isCredit) {
                            Icons.Filled.ArrowUpward
                        } else {
                            Icons.Filled.ArrowDownward
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AmountGreen,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = VendorUi.TextDark,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.time,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = VendorUi.TextMuted,
                )
            }
            val sign = if (transaction.isCredit) "+" else "−"
            Text(
                text = "$sign₹${transaction.amount}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AmountGreen,
            )
        }
    }
}

data class TransactionData(val title: String, val time: String, val amount: Int, val isCredit: Boolean)

@Preview(showBackground = true)
@Composable
fun Preview() {
    SeebazarTheme {
        MyWalletScreen()
    }
}
