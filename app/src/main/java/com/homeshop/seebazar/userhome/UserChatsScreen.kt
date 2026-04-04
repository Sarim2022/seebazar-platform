package com.homeshop.seebazar.userhome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val UserChatScreenBg = Color(0xFFF1F5F9)
private val UserChatCardBg = Color.White
private val UserChatBorder = Color(0xFFE2E8F0)
private val UserChatMuted = Color(0xFF64748B)
private val UserChatTitle = Color(0xFF0F172A)
private val UserChatAccentStart = Color(0xFF155AC1)
private val UserChatAccentEnd = Color(0xFF3B82F6)

private data class UserChatThread(
    val title: String,
    val subtitle: String,
    val timeLabel: String,
    val unread: Boolean,
)

@Composable
fun UserChatsScreen(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    val threads = remember {
        listOf(
            UserChatThread("Fresh Mart — Sector 4", "Your order is packed for pickup", "2m", true),
            UserChatThread("QuickFix Plumbing", "We can visit tomorrow 10–12", "1h", true),
            UserChatThread("Spice Route Kitchen", "Table for 4 confirmed Sat 7pm", "Yesterday", false),
            UserChatThread("GreenLeaf Grocery", "Thanks! Glad you liked the mangoes", "Yesterday", false),
            UserChatThread("Urban Cuts Salon", "Reminder: appointment Thu 4pm", "Mon", false),
            UserChatThread("BookNest Café", "Wi‑Fi password at the counter", "Sun", false),
            UserChatThread("Metro Pharmacy", "Prescription ready for collection", "28 Mar", false),
            UserChatThread("Laundry Express", "Whites will be ready by 6pm", "27 Mar", false),
            UserChatThread("TechCare Repairs", "Estimate attached — reply to confirm", "26 Mar", false),
            UserChatThread("Bloom Florist", "Delivery photo sent ✓", "25 Mar", false),
        )
    }

    val filtered = remember(query, threads) {
        if (query.isBlank()) threads
        else threads.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.subtitle.contains(query, ignoreCase = true)
        }
    }

    // Parent [UserHome] Scaffold already applies paddingValues (status bar + bottom nav).
    // Do not add statusBarsPadding here — it doubles the top inset and leaves a blank gap.
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UserChatScreenBg),
    ) {
        // --- 1. CUSTOM TOP BAR (Tight spacing) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Messages",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = UserChatTitle,
            )

            // Filter Icon Aligned to the Right
            IconButton(
                onClick = { /* filter */ },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Filter",
                    tint = UserChatAccentStart,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // --- 2. SEARCH BAR ---
        UserChatsSearchField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. CHAT LIST ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(filtered, key = { it.title + it.timeLabel }) { thread ->
                UserChatThreadCard(thread)
            }
        }
    }
}
@Composable
private fun UserChatsSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text("Search shops and chats", color = UserChatMuted, fontSize = 15.sp)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = UserChatMuted,
                modifier = Modifier.size(20.dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = UserChatCardBg,
            unfocusedContainerColor = UserChatCardBg,
            focusedBorderColor = UserChatBorder.copy(alpha = 0.5f),
            unfocusedBorderColor = UserChatBorder.copy(alpha = 0.5f),
        ),
    )
}

@Composable
private fun UserChatThreadCard(thread: UserChatThread) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = UserChatCardBg,
        shadowElevation = 1.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            listOf(UserChatAccentStart, UserChatAccentEnd),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = thread.title.take(2).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = thread.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = UserChatTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = thread.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (thread.unread) UserChatAccentStart else UserChatMuted,
                        fontWeight = if (thread.unread) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = thread.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = UserChatMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserChatsScreenPreview() {
    MaterialTheme {
        UserChatsScreen(Modifier.statusBarsPadding())
    }
}
