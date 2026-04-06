package com.homeshop.seebazar.userhome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.homeshop.seebazar.chat.ChatConversationScreen
import com.homeshop.seebazar.chat.ChatEmptyState
import com.homeshop.seebazar.chat.ChatInboxSearchField
import com.homeshop.seebazar.chat.ChatInboxThreadRow
import com.homeshop.seebazar.chat.ChatUi
import com.homeshop.seebazar.data.ChatFirestore
import com.homeshop.seebazar.data.ChatRoomSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserChatsScreen(
    modifier: Modifier = Modifier,
    /** When set (e.g. after tapping Chat on a service), opens this room once. */
    pendingOpenRoomId: String? = null,
    pendingOpenTitle: String = "",
    onConsumedPendingOpen: () -> Unit = {},
) {
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

    LaunchedEffect(pendingOpenRoomId, pendingOpenTitle) {
        val id = pendingOpenRoomId
        if (!id.isNullOrBlank()) {
            selectedRoom = id to pendingOpenTitle.ifBlank { "Chat" }
            onConsumedPendingOpen()
        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatUi.ScreenBg),
        ) {
            Surface(color = ChatUi.Surface, shadowElevation = 0.dp) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = ChatUi.TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                    )
                    HorizontalDivider(thickness = 1.dp, color = ChatUi.Hairline)
                }
            }
            ChatInboxSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search shops and chats",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
            if (myUid.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ChatEmptyState(message = "Sign in to use messages.")
                }
            } else if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ChatEmptyState(
                        message = "No conversations yet.\nOpen Chat on a service to start.",
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(filtered, key = { it.roomId }) { room ->
                        val peerTitle =
                            if (myUid == room.vendorUid) room.buyerLabel else room.vendorLabel
                        val timeLabel = when {
                            room.lastAtMillis <= 0L -> ""
                            else -> {
                                val now = System.currentTimeMillis()
                                val day = 24L * 60 * 60 * 1000
                                if (now - room.lastAtMillis < day) timeFmt.format(Date(room.lastAtMillis))
                                else relFmt.format(Date(room.lastAtMillis))
                            }
                        }
                        ChatInboxThreadRow(
                            title = peerTitle.ifBlank { "Chat" },
                            subtitle = room.lastMessage.ifBlank { "No messages yet" },
                            timeLabel = timeLabel,
                            onClick = { selectedRoom = room.roomId to peerTitle.ifBlank { "Chat" } },
                        )
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
