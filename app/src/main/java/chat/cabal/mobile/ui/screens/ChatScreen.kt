package chat.cabal.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.cabal.mobile.R
import chat.cabal.mobile.ui.components.MessageBubble
import chat.cabal.mobile.ui.theme.CabalDeepBlack
import chat.cabal.mobile.ui.theme.CabalGlassSurface
import chat.cabal.mobile.ui.viewmodel.ChatViewModel
import chat.cabal.mobile.core.toHex

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    myPublicKeyHex: String,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
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
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cabal_splash_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .alpha(0.1f),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Invite your peers or start typing to begin the conversation.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        textAlign = TextAlign.Center
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
                    val authorHex = message.publicKey.toHex()
                    Box(modifier = Modifier.animateItem()) {
                        MessageBubble(
                            text = message.text,
                            authorHex = authorHex,
                            isMine = authorHex == myPublicKeyHex,
                            status = message.status
                        )
                    }
                }
            }
        }
        
        Surface(
            tonalElevation = 12.dp,
            shadowElevation = 12.dp,
            color = CabalGlassSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                TextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(
                            "Message encrypted...", 
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        ) 
                    },
                    shape = RoundedCornerShape(28.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground
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
