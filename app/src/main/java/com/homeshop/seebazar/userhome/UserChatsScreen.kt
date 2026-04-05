package com.homeshop.seebazar.userhome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.homeshop.seebazar.chat.ChatConversationScreen
import com.homeshop.seebazar.data.ChatFirestore
import com.homeshop.seebazar.data.ChatRoomSummary
import com.homeshop.seebazar.servicehome.VendorUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val UserChatScreenBg = Color(0xFFF1F5F9)
private val UserChatCardBg = Color.White
private val UserChatBorder = Color(0xFFE2E8F0)
private val UserChatMuted = Color(0xFF64748B)
private val UserChatTitle = Color(0xFF0F172A)
private val UserChatAccentStart = Color(0xFF155AC1)
private val UserChatAccentEnd = Color(0xFF3B82F6)

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
                .background(UserChatScreenBg),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Messages",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = UserChatTitle,
                )
            }
            UserChatsSearchField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (myUid.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Sign in to use messages.", color = UserChatMuted)
                }
            } else if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No conversations yet.\nOpen Chat on a service to start.",
                        color = UserChatMuted,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
                        UserChatThreadCard(
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

@Composable
private fun UserChatsSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
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
                modifier = Modifier.size(20.dp),
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
private fun UserChatThreadCard(
    title: String,
    subtitle: String,
    timeLabel: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    text = title.take(2).uppercase(Locale.getDefault()),
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
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = UserChatTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (timeLabel.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = UserChatMuted,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = UserChatMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
