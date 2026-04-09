package com.homeshop.seebazar.chat

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val primaryName = remember(title) { title.substringBefore(" · ").substringBefore(" - ") }
    val secondaryName = remember(title) {
        when {
            title.contains(" · ") -> title.substringAfter(" · ")
            title.contains(" - ") -> title.substringAfter(" - ")
            else -> "Active"
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChatUi.ScreenBg,
        topBar = {
            Surface(
                color = ChatUi.Surface.copy(alpha = 0.85f),
                shadowElevation = 0.dp,
            ) {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = primaryName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ChatUi.TextPrimary,
                                    maxLines = 1,
                                )
                                Text(
                                    text = secondaryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Normal,
                                    color = ChatUi.TextSecondary,
                                    maxLines = 1,
                                )
                            }
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
                            containerColor = Color.Transparent,
                        ),
                    )
                    HorizontalDivider(thickness = 1.dp, color = ChatUi.Hairline)
                }
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
                    ) {
                        if (mine) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 300.dp)
                                    .clip(bubbleShapeMine)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                ChatUi.BubbleMineGradientStart,
                                                ChatUi.BubbleMineGradientEnd,
                                            )
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ChatUi.BubbleMineText,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 22.sp,
                                )
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
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                ) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ChatUi.BubbleOtherText,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 22.sp,
                                    )
                                }
                            }
                        }
                        if (msg.createdAtMillis > 0L) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = timeFmt.format(Date(msg.createdAtMillis)),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal,
                                color = ChatUi.TextTertiary,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                    }
                }
            }
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Start a conversation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ChatUi.TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        listOf(
                            "Is the service available?",
                            "What are your charges?",
                            "Can we schedule a time?"
                        ).forEach { quickMsg ->
                            Surface(
                                shape = RoundedCornerShape(percent = 50),
                                color = ChatUi.BrandSoft,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .clickable { draft = quickMsg }
                            ) {
                                Text(
                                    text = quickMsg,
                                    color = ChatUi.Brand,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(8.dp, RoundedCornerShape(26.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(26.dp),
                        color = ChatUi.InputSurface,
                    ) {
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            modifier = Modifier.fillMaxWidth(),
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
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = ChatUi.Brand,
                                focusedTextColor = ChatUi.TextPrimary,
                                unfocusedTextColor = ChatUi.TextPrimary,
                            ),
                        )
                    }
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
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(8.dp, CircleShape, spotColor = ChatUi.SendFab.copy(alpha = 0.3f)),
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
