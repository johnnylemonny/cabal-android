package chat.cabal.mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CabalPrimary,
    secondary = CabalSecondary,
    tertiary = CabalTertiary,
    background = CabalDeepBlack,
    surface = CabalSurfaceDark,
    onPrimary = CabalDeepBlack,
    onSecondary = CabalDeepBlack,
    onTertiary = CabalDeepBlack,
    onBackground = CabalTextLight,
    onSurface = CabalTextLight,
    surfaceVariant = CabalPrivateBlack,
    onSurfaceVariant = CabalMuted
)

private val LightColorScheme = lightColorScheme(
    primary = CabalPrimary,
    secondary = CabalSecondary,
    tertiary = CabalTertiary,
    background = CabalWhite,
    surface = Color(0xFFF0F2F5),
    onPrimary = CabalWhite,
    onSecondary = CabalWhite,
    onTertiary = CabalWhite,
    onBackground = CabalPrivateBlack,
    onSurface = CabalPrivateBlack,
    surfaceVariant = Color(0xFFE4E6EB),
    onSurfaceVariant = Color(0xFF65676B)
)

@Composable
fun CabalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Edge-to-edge is handled in MainActivity, no need to manually set status bar colors here.

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
