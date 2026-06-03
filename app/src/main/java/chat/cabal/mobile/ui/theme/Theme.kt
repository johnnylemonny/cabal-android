package chat.cabal.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CabalCipherBlue,
    secondary = CabalPeerTeal,
    tertiary = CabalTertiary,
    background = CabalDeepBlack,
    surface = CabalSurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E222C),
    onSurfaceVariant = CabalMuted,
    outline = Color.White.copy(alpha = 0.12f)
)

@Composable
fun CabalTheme(
    content: @Composable () -> Unit
) {
    // Force DarkColorScheme for the "High-End" look the user requested
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
