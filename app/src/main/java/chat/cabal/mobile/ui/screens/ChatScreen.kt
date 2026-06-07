package chat.cabal.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.cabal.mobile.R
import chat.cabal.mobile.core.toHex
import chat.cabal.mobile.ui.components.MessageBubble
import chat.cabal.mobile.ui.theme.*
import chat.cabal.mobile.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    myPublicKeyHex: String,
    modifier: Modifier = Modifier
) {
    val messages: List<chat.cabal.database.Message> by viewModel.messages.collectAsState()
    val peers: List<chat.cabal.database.Peer> by viewModel.peers.collectAsState()
    val peerMap: Map<String, chat.cabal.database.Peer> = remember(peers) { 
        peers.associateBy { it.publicKey.toHex() } 
    }
    val replyTo by viewModel.replyTo
    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    val onSend = {
        if (textState.isNotBlank()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.sendMessage(textState)
            textState = ""
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CabalDeepBlack)
            .imePadding()
    ) {
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cabal_splash_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .alpha(0.05f),
                            tint = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).alpha(0.2f),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "SECURE CHANNEL",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "No messages in this cabal yet.\nBroadcast a message to your peers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages, key = { it.hash.toHex() }) { message ->
                    val authorHex: String = message.publicKey.toHex()
                    val authorName: String = peerMap[authorHex]?.name ?: authorHex.take(12).uppercase()
                    val parentMsg: chat.cabal.database.Message? = messages.find { it.hash.contentEquals(message.parentHash) }
                    val parentAuthorHex: String? = parentMsg?.publicKey?.toHex()
                    val parentAuthorName: String? = parentAuthorHex?.let { hex ->
                        peerMap[hex]?.name ?: hex.take(12).uppercase()
                    }
                    
                    Box(modifier = Modifier
                        .animateItem()
                        .clickable { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.setReplyTo(message) 
                        }
                    ) {
                        MessageBubble(
                            text = message.text,
                            authorName = authorName,
                            authorHex = authorHex,
                            isMine = authorHex == myPublicKeyHex,
                            status = message.status,
                            replyToText = parentMsg?.text,
                            replyToAuthor = parentAuthorName
                        )
                    }
                }
            }
        }
        
        Surface(
            tonalElevation = 12.dp,
            shadowElevation = 12.dp,
            color = Color(0xFF161A22),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            Column {
                if (replyTo != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(3.dp).height(32.dp).background(CabalPeerTeal))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            val replyAuthorHex: String? = replyTo?.publicKey?.toHex()
                            val replyAuthorName: String? = replyAuthorHex?.let { hex ->
                                peerMap[hex]?.name ?: hex.take(12).uppercase()
                            }
                            Text(
                                "Replying to $replyAuthorName",
                                style = MaterialTheme.typography.labelSmall,
                                color = CabalPeerTeal,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                replyTo?.text ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f),
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = { viewModel.setReplyTo(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White.copy(alpha = 0.4f))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { 
                            Text(
                                "Message encrypted...", 
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.3f)
                            ) 
                        },
                        shape = RoundedCornerShape(28.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSend() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.06f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White
                        )
                    )
                    FloatingActionButton(
                        onClick = { onSend() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = CabalDeepBlack,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 0.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send, 
                            contentDescription = "Send",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
