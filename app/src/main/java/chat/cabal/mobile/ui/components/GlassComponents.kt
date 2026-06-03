package chat.cabal.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CabalGlassBubble(
    isMine: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (isMine) 20.dp else 4.dp,
        bottomEnd = if (isMine) 4.dp else 20.dp
    )
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (isMine) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6EA8FF),
                            Color(0xFF00D1B2)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E222C),
                            Color(0xFF161A22)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isMine) 0.3f else 0.1f),
                        Color.Transparent
                    )
                ),
                shape = shape
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        content()
    }
}
