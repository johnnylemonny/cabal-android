package chat.cabal.mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import chat.cabal.mobile.ui.theme.*

@Composable
fun PeerAvatar(
    publicKeyHex: String,
    modifier: Modifier = Modifier
) {
    // Deterministic selection from brand palette + variety
    val hash = publicKeyHex.hashCode()
    val brandColors = listOf(
        CabalCipherBlue,
        CabalPeerTeal,
        CabalTertiary,
        Color(0xFF4FC3F7),
        Color(0xFF4DB6AC),
        Color(0xFF9575CD)
    )
    val baseColor = brandColors[(hash.coerceAtLeast(0) % brandColors.size)]

    Canvas(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
    ) {
        // Draw background
        drawRect(baseColor)
        
        // Draw modern geometric pattern
        val steps = 5
        val cellSize = size.width / steps
        
        for (i in 0 until steps) {
            for (j in 0 until steps) {
                // Symmetric pattern for a more professional "logo-like" feel
                val symI = if (i >= steps / 2) steps - 1 - i else i
                val bitIndex = (symI * steps + j) % 32
                
                if ((hash shr bitIndex) and 1 == 1) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.25f),
                        topLeft = Offset(i * cellSize, j * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
        
        // Subtle inner shadow/border
        drawCircle(
            color = Color.Black.copy(alpha = 0.1f),
            radius = size.width / 2,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
    }
}
