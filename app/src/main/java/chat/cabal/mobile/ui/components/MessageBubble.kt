package chat.cabal.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import chat.cabal.mobile.ui.theme.CabalPeerTeal

@Composable
fun MessageBubble(
    text: String,
    authorName: String,
    authorHex: String,
    isMine: Boolean,
    status: Long = 1L,
    replyToText: String? = null,
    replyToAuthor: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMine) {
            PeerAvatar(authorHex, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
        }
        
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (!isMine) {
                Text(
                    text = authorName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CabalPeerTeal.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                )
            }
            CabalGlassBubble(
                isMine = isMine
            ) {
                Column(modifier = Modifier.padding(2.dp)) {
                    if (replyToText != null) {
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Row(modifier = Modifier.padding(8.dp).height(IntrinsicSize.Min)) {
                                Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(CabalPeerTeal))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = replyToAuthor?.take(12)?.uppercase() ?: "UNKNOWN",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CabalPeerTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = replyToText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.6f),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = text,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 22.sp
                            ),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        if (isMine) {
                            Icon(
                                imageVector = when (status) {
                                    0L -> Icons.Default.Schedule
                                    1L -> Icons.Default.Done
                                    else -> Icons.Default.DoneAll
                                },
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp).padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
