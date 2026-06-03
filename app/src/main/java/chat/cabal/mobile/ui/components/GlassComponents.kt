package chat.cabal.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import chat.cabal.mobile.ui.theme.CabalGlassSurface

@Composable
fun CabalGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(CabalGlassSurface)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        content()
    }
}

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
                            Color(0xFF6EA8FF).copy(alpha = 0.9f),
                            Color(0xFF00D1B2).copy(alpha = 0.9f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            CabalGlassSurface,
                            CabalGlassSurface.copy(alpha = 0.7f)
                        )
                    )
                }
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = if (isMine) 0.2f else 0.1f),
                shape = shape
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        content()
    }
}
