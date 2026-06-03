package chat.cabal.mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import chat.cabal.mobile.ui.theme.*

@Composable
fun PeerAvatar(
    publicKeyHex: String,
    modifier: Modifier = Modifier
) {
    val hash = publicKeyHex.hashCode()
    val brandGradients = listOf(
        listOf(CabalCipherBlue, Color(0xFF4FC3F7)),
        listOf(CabalPeerTeal, Color(0xFF4DB6AC)),
        listOf(CabalTertiary, Color(0xFF9575CD)),
        listOf(Color(0xFF6EA8FF), Color(0xFFB060FF)),
        listOf(Color(0xFF00D1B2), Color(0xFF6EA8FF))
    )
    val gradient = brandGradients[(hash.coerceAtLeast(0) % brandGradients.size)]

    Canvas(
        modifier = modifier
            .size(40.dp)
            .shadow(elevation = 4.dp, shape = CircleShape)
            .clip(CircleShape)
    ) {
        // Multi-layered gradient background
        drawRect(
            brush = Brush.linearGradient(
                colors = gradient,
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
        )
        
        // Geometric "shield" pattern
        val steps = 6
        val cellSize = size.width / steps
        for (i in 0 until steps) {
            for (j in 0 until steps) {
                // Symmetric design
                val symI = if (i >= steps / 2) steps - 1 - i else i
                val bitIndex = (symI * steps + j) % 32
                
                if ((hash shr bitIndex) and 1 == 1) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.2f),
                        topLeft = Offset(i * cellSize, j * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
        
        // Inner Glow / Highlight border
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)
            ),
            radius = size.width / 2,
            style = Stroke(width = 1.dp.toPx())
        )
        
        // Subtle outer vignette
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.1f)),
                center = center,
                radius = size.width / 2
            ),
            radius = size.width / 2
        )
    }
}
