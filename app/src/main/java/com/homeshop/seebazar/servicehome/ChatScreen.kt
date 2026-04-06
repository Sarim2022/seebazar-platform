package com.homeshop.seebazar.servicehome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.homeshop.seebazar.chat.ChatConversationScreen
import com.homeshop.seebazar.chat.ChatEmptyState
import com.homeshop.seebazar.chat.ChatInboxSearchField
import com.homeshop.seebazar.chat.ChatInboxThreadRow
import com.homeshop.seebazar.chat.ChatUi
import com.homeshop.seebazar.common.VendorCommonTopBar
import com.homeshop.seebazar.data.ChatFirestore
import com.homeshop.seebazar.data.ChatRoomSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(modifier: Modifier = Modifier) {
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    var rooms by remember { mutableStateOf<List<ChatRoomSummary>>(emptyList()) }
    var selectedRoom by remember { mutableStateOf<Pair<String, String>?>(null) }
    var query by remember { mutableStateOf("") }

    DisposableEffect(myUid) {
        if (myUid.isBlank()) {
            rooms = emptyList()
            return@DisposableEffect onDispose { }
        }
        val reg = ChatFirestore.listenRoomsForUser(myUid) { rooms = it }
        onDispose { reg.remove() }
    }

    val filtered = remember(query, rooms) {
        if (query.isBlank()) rooms
        else rooms.filter {
            it.vendorLabel.contains(query, ignoreCase = true) ||
                it.buyerLabel.contains(query, ignoreCase = true) ||
                it.lastMessage.contains(query, ignoreCase = true)
        }
    }

    val relFmt = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val timeFmt = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = ChatUi.ScreenBg,
            topBar = {
                VendorCommonTopBar(
                    title = "Chats",
                    onBackClick = { /* navController.popBackStack() */ },
                    containerColor = ChatUi.Surface,
                    dividerColor = ChatUi.Hairline,
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatUi.ScreenBg)
                    .padding(paddingValues),
            ) {
                ChatInboxSearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search conversations",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
                if (myUid.isBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        ChatEmptyState(message = "Sign in to see messages.")
                    }
                } else if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        ChatEmptyState(message = "No conversations yet.\nWhen customers message you, they will show up here.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 20.dp, top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(filtered, key = { it.roomId }) { room ->
                            val peerTitle =
                                if (myUid == room.vendorUid) room.buyerLabel else room.vendorLabel
                            val timeLabel = when {
                                room.lastAtMillis <= 0L -> ""
                                else -> {
                                    val now = System.currentTimeMillis()
                                    val day = 24L * 60 * 60 * 1000
                                    if (now - room.lastAtMillis < day) {
                                        timeFmt.format(Date(room.lastAtMillis))
                                    } else {
                                        relFmt.format(Date(room.lastAtMillis))
                                    }
                                }
                            }
                            ChatInboxThreadRow(
                                title = peerTitle.ifBlank { "Customer" },
                                subtitle = room.lastMessage.ifBlank { "No messages yet" },
                                timeLabel = timeLabel,
                                onClick = {
                                    selectedRoom = room.roomId to peerTitle.ifBlank { "Customer" }
                                },
                            )
                        }
                    }
                }
            }
        }

        selectedRoom?.let { (roomId, title) ->
            ChatConversationScreen(
                roomId = roomId,
                myUid = myUid,
                title = title,
                onBack = { selectedRoom = null },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MaterialTheme {
        ChatScreen()
    }
}
