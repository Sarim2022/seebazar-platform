package com.homeshop.seebazar.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.data.ChatFirestore
import com.homeshop.seebazar.data.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    roomId: String,
    myUid: String,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }

    DisposableEffect(roomId) {
        val reg = ChatFirestore.listenMessages(roomId) { messages = it }
        onDispose { reg.remove() }
    }

    val timeFmt = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val bubbleShapeMine = RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)
    val bubbleShapeTheirs = RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChatUi.ScreenBg,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ChatUi.TextPrimary,
                            maxLines = 1,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ChatUi.Brand,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ChatUi.Surface,
                        titleContentColor = ChatUi.TextPrimary,
                        navigationIconContentColor = ChatUi.Brand,
                    ),
                )
                HorizontalDivider(thickness = 1.dp, color = ChatUi.Hairline)
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(messages, key = { it.id }) { msg ->
                    val mine = msg.fromUid == myUid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
                    ) {
                        if (mine) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 300.dp)
                                    .clip(bubbleShapeMine)
                                    .background(ChatUi.BubbleMine)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                            ) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ChatUi.BubbleMineText,
                                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                                )
                                if (msg.createdAtMillis > 0L) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = timeFmt.format(Date(msg.createdAtMillis)),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = ChatUi.BubbleMineText.copy(alpha = 0.85f),
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier.widthIn(max = 300.dp),
                                shape = bubbleShapeTheirs,
                                color = ChatUi.BubbleOther,
                                border = BorderStroke(1.dp, ChatUi.BubbleOtherStroke),
                                shadowElevation = 0.dp,
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                ) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ChatUi.TextPrimary,
                                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                                    )
                                    if (msg.createdAtMillis > 0L) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = timeFmt.format(Date(msg.createdAtMillis)),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = ChatUi.TextTertiary,
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Surface(
                color = ChatUi.Surface,
                tonalElevation = 0.dp,
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Type a message…",
                                color = ChatUi.TextTertiary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        maxLines = 4,
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ChatUi.InputSurface,
                            unfocusedContainerColor = ChatUi.InputSurface,
                            disabledContainerColor = ChatUi.InputSurface,
                            focusedBorderColor = ChatUi.BorderSubtle,
                            unfocusedBorderColor = ChatUi.BorderSubtle,
                            cursorColor = ChatUi.Brand,
                            focusedTextColor = ChatUi.TextPrimary,
                            unfocusedTextColor = ChatUi.TextPrimary,
                        ),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    val canSend = !sending && draft.isNotBlank()
                    FilledIconButton(
                        onClick = {
                            if (draft.isBlank() || sending) return@FilledIconButton
                            sending = true
                            val toSend = draft
                            draft = ""
                            ChatFirestore.sendMessage(roomId, myUid, toSend) { err ->
                                sending = false
                                if (err != null) draft = toSend
                            }
                        },
                        enabled = canSend,
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = ChatUi.SendFab,
                            contentColor = Color.White,
                            disabledContainerColor = ChatUi.Hairline,
                            disabledContentColor = ChatUi.TextTertiary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}
