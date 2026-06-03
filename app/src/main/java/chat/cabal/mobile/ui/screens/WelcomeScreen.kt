package chat.cabal.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.cabal.mobile.R
import chat.cabal.mobile.ui.theme.CabalDeepBlack

@Composable
fun WelcomeScreen(
    onEnter: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val backgroundOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0B0D12),
                        CabalDeepBlack
                    ),
                    center = Offset(backgroundOffset, backgroundOffset),
                    radius = 2000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle animated background "aurora"
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.15f)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            Color.Transparent
                        ),
                        start = Offset(backgroundOffset, 0f),
                        end = Offset(0f, backgroundOffset)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .scale(scale)
                .alpha(alpha)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Glow effect behind logo
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_cabal_splash_icon),
                    contentDescription = "Cabal Logo",
                    modifier = Modifier.size(140.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            }
            
            Spacer(Modifier.height(40.dp))
            
            Text(
                text = "CABAL",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = "DECENTRALIZED • PRIVATE • P2P",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp
            )
            
            Spacer(Modifier.height(56.dp))
            
            Text(
                text = "The future of secure communication.\nNo central servers. Pure peer-to-peer.",
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            
            Spacer(Modifier.height(80.dp))
            
            Button(
                onClick = onEnter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = CabalDeepBlack
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    "START CHATTING",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
