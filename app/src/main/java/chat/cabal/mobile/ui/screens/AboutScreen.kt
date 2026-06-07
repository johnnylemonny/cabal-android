package chat.cabal.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.cabal.mobile.R
import chat.cabal.mobile.ui.theme.CabalCipherBlue
import chat.cabal.mobile.ui.theme.CabalPeerTeal

@Composable
fun AboutScreen() {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Section
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CabalCipherBlue.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
                .border(1.dp, CabalCipherBlue.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_cabal_mark_v2a_foreground),
                contentDescription = "Cabal Logo",
                modifier = Modifier.size(80.dp),
                tint = CabalCipherBlue
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            text = "CABAL MOBILE",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            color = Color.White
        )
        
        Text(
            text = "BETA v0.3.0",
            style = MaterialTheme.typography.labelLarge,
            color = CabalPeerTeal,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Text(
            text = "by johnnylemonny",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(Modifier.height(32.dp))
        
        // Mission Card
        InfoCard(
            title = "THE MISSION",
            content = "Cabal is a decentralized, private-first communication protocol. No servers, no central authority, no trackers. Just you and your peers.",
            icon = Icons.Default.Shield
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Protocol Specs
        InfoCard(
            title = "CORE PROTOCOL",
            content = "Built on the Cable protocol, utilizing Ed25519 signatures, BLAKE2b hashing, and a persistent append-only multi-log for eventual consistency in P2P networks.",
            icon = Icons.Default.Code
        )
        
        Spacer(Modifier.height(32.dp))
        
        // Features Grid Header
        Text(
            text = "KEY FEATURES",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 12.dp)
        )
        
        // Features Grid
        Row(Modifier.fillMaxWidth()) {
            FeatureItem(Modifier.weight(1f), "E2EE", Icons.Default.Lock, "End-to-End Encryption")
            Spacer(Modifier.width(12.dp))
            FeatureItem(Modifier.weight(1f), "P2P", Icons.Default.Share, "Serverless Discovery")
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            FeatureItem(Modifier.weight(1f), "AGPL", Icons.Default.Gavel, "GNU Affero GPL v3")
            Spacer(Modifier.width(12.dp))
            FeatureItem(Modifier.weight(1f), "LOCAL", Icons.Default.Storage, "No Cloud Storage")
        }
        
        Spacer(Modifier.height(48.dp))
        
        // Footer
        Text(
            text = "Created by johnnylemonny\nReleased under GNU AGPL v3 License",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.3f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun InfoCard(title: String, content: String, icon: ImageVector) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = CabalCipherBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = CabalCipherBlue
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun FeatureItem(modifier: Modifier, label: String, icon: ImageVector, description: String) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = CabalPeerTeal, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
