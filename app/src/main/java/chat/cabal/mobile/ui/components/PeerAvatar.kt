package chat.cabal.mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

@Composable
fun PeerAvatar(
    publicKeyHex: String,
    modifier: Modifier = Modifier
) {
    // Generate a deterministic color based on the public key
    val hash = publicKeyHex.hashCode()
    val color = Color(
        red = (hash and 0xFF0000 shr 16) / 255f,
        green = (hash and 0x00FF00 shr 8) / 255f,
        blue = (hash and 0x0000FF) / 255f,
        alpha = 1.0f
    ).copy(alpha = 0.8f)

    Canvas(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
    ) {
        drawRect(color)
        // Simple geometric pattern based on hash
        val steps = 4
        val stepSize = size.width / steps
        for (i in 0 until steps) {
            for (j in 0 until steps) {
                if ((hash shr (i * steps + j)) and 1 == 1) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = androidx.compose.ui.geometry.Offset(i * stepSize, j * stepSize),
                        size = androidx.compose.ui.geometry.Size(stepSize, stepSize)
                    )
                }
            }
        }
    }
}
