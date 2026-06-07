package chat.cabal.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.cabal.mobile.ui.theme.CabalCipherBlue
import chat.cabal.mobile.ui.theme.CabalDeepBlack
import chat.cabal.mobile.ui.theme.CabalPeerTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackupIdentity: () -> Unit,
    onAddRelay: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var relayAddress by remember { mutableStateOf("") }

    Scaffold(
        containerColor = CabalDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SETTINGS", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    "SECURITY \u0026 BACKUP",
                    style = MaterialTheme.typography.labelMedium,
                    color = CabalCipherBlue,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(12.dp))
                Card(
                    onClick = onBackupIdentity,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = CabalPeerTeal)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Backup Identity", fontWeight = FontWeight.Bold)
                            Text("Generate your 12-word seed phrase", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
                    }
                }
            }

            item {
                Text(
                    "P2P RELAYS (5G/NAT)",
                    style = MaterialTheme.typography.labelMedium,
                    color = CabalCipherBlue,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(12.dp))
                TextField(
                    value = relayAddress,
                    onValueChange = { relayAddress = it },
                    placeholder = { Text("Relay IP or Hostname", color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                    ),
                    trailingIcon = {
                        IconButton(onClick = { 
                            if (relayAddress.isNotBlank()) {
                                onAddRelay(relayAddress)
                                relayAddress = ""
                            }
                        }) {
                            Icon(Icons.Default.AddLink, contentDescription = "Add", tint = CabalPeerTeal)
                        }
                    }
                )
                Text(
                    "Connect to public nodes to find peers across different networks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
