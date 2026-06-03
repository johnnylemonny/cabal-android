package chat.cabal.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import chat.cabal.mobile.ui.theme.CabalMuted

@Composable
fun MessageBubble(
    text: String,
    authorHex: String,
    isMine: Boolean,
    status: Long = 1L
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMine) {
            PeerAvatar(authorHex, modifier = Modifier.padding(bottom = 2.dp).size(32.dp))
            Spacer(Modifier.width(12.dp))
        }
        
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (!isMine) {
                Text(
                    text = authorHex.take(12).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }
            CabalGlassBubble(
                isMine = isMine
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onBackground,
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
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp).padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
