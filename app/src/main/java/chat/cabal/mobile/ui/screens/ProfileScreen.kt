package chat.cabal.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import chat.cabal.mobile.R
import chat.cabal.mobile.ui.components.PeerAvatar
import chat.cabal.mobile.ui.theme.CabalCipherBlue
import chat.cabal.mobile.ui.theme.CabalDeepBlack
import chat.cabal.mobile.ui.theme.CabalPeerTeal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    myPublicKeyHex: String,
    onSave: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val clipboard = LocalClipboard.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CabalDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MY IDENTITY", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                PeerAvatar(myPublicKeyHex, modifier = Modifier.size(120.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_cabal_mark_v2a_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(CabalDeepBlack)
                        .padding(4.dp),
                    tint = CabalPeerTeal
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "PUBLIC KEY",
                style = MaterialTheme.typography.labelMedium,
                color = CabalCipherBlue
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = myPublicKeyHex.take(16) + "...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        val clipEntry = androidx.compose.ui.platform.ClipEntry(
                            android.content.ClipData.newPlainText("Public Key", myPublicKeyHex)
                        )
                        scope.launch {
                            clipboard.setClipEntry(clipEntry)
                            snackbarHostState.showSnackbar("Key copied to clipboard")
                        }
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CabalPeerTeal)
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                )
            )
            
            Spacer(Modifier.height(16.dp))
            
            TextField(
                value = status,
                onValueChange = { status = it },
                label = { Text("Status Message") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                )
            )
            
            Spacer(Modifier.weight(1f))
            
            Button(
                onClick = { onSave(name, status) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CabalPeerTeal)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("SAVE PROFILE", fontWeight = FontWeight.Bold)
            }
        }
    }
}
