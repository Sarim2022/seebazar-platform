package com.homeshop.seebazar.servicehome

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(modifier: Modifier = Modifier) {
    val chats = remember {
        val base = listOf(
            ChatUser("Kriss Benwat", "Good to know", "Today", "https://example.com/1.jpg"),
            ChatUser("Rezi Makarov", "its been a while", "Today", "https://example.com/2.jpg"),
            ChatUser("Gustav Lemelo", "Hey, where are you?", "Yesterday", "https://example.com/3.jpg"),
            ChatUser("Bob Ryder", "Its over!", "28 Jan", "https://example.com/4.jpg"),
            ChatUser("Ken Simada", "Your Welcome!", "24 Jan", "https://example.com/5.jpg"),
            ChatUser("Emma Walkins", "I am going out!", "19 Jan", "https://example.com/6.jpg"),
            ChatUser("Gilbert Hamminway", "Just attach the image and send it.", "12 Jan", "https://example.com/7.jpg"),
            ChatUser("Bella Hammers", "The UI is sexy", "5 Jan", "https://example.com/8.jpg"),
        )
        val extra = listOf(
            ChatUser("Ava Chen", "Invoice sent for last week", "Today", ""),
            ChatUser("Marcus Lee", "Can we reschedule?", "Today", ""),
            ChatUser("Priya Sharma", "Thanks for the quick delivery!", "Yesterday", ""),
            ChatUser("Jonas K.", "Packaging looked great", "Yesterday", ""),
            ChatUser("Sofia Ruiz", "Need 2 more units", "26 Jan", ""),
            ChatUser("Tom H.", "Payment received", "25 Jan", ""),
            ChatUser("Mei Lin", "On my way to pickup", "24 Jan", ""),
            ChatUser("Oliver Grant", "Please confirm address", "23 Jan", ""),
            ChatUser("Nadia F.", "Refunded as discussed", "22 Jan", ""),
            ChatUser("Victor P.", "New order placed", "21 Jan", ""),
            ChatUser("Hannah W.", "Love the new menu items", "20 Jan", ""),
            ChatUser("Diego M.", "Running 10 min late", "19 Jan", ""),
            ChatUser("Yuki Tanaka", "Photos attached", "18 Jan", ""),
            ChatUser("Samir K.", "Call when ready", "17 Jan", ""),
        )
        base + extra
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VendorUi.ScreenBg,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            VendorStandardTopBar(
                title = "Chat",
                actions = {
                    IconButton(onClick = { /* add chat */ }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add",
                            tint = VendorUi.BrandBlue,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(VendorUi.ScreenBg)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 8.dp),
        ) {
            item {
                TextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search...", color = VendorUi.TextMuted) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = VendorUi.TextMuted,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(25.dp),
                )
            }
            itemsIndexed(chats, key = { index, _ -> index }) { _, chat ->
                ChatItemRow(chat)
            }
        }
    }
}

@Composable
fun ChatItemRow(chat: ChatUser) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(VendorUi.CardStroke),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = chat.name.take(1),
                fontWeight = FontWeight.Bold,
                color = VendorUi.BrandBlue,
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
        ) {
            Text(
                text = chat.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = VendorUi.TextDark,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = VendorUi.TextMuted,
                maxLines = 1,
            )
        }

        Text(
            text = chat.time,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = VendorUi.TextMuted,
        )
    }
}

data class ChatUser(
    val name: String,
    val lastMessage: String,
    val time: String,
    val imageUrl: String,
)

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MaterialTheme {
        ChatScreen()
    }
}
