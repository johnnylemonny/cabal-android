package chat.cabal.mobile.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import chat.cabal.mobile.core.IdentityBackupManager
import chat.cabal.mobile.core.KeyStoreManager
import chat.cabal.mobile.core.toHex
import chat.cabal.mobile.ui.screens.*
import chat.cabal.mobile.ui.theme.CabalPeerTeal
import chat.cabal.mobile.ui.theme.CabalSurfaceDark
import chat.cabal.mobile.ui.viewmodel.ChatViewModel
import chat.cabal.network.TcpTransport

@Composable
fun CabalNavGraph(
    navController: NavHostController,
    chatViewModel: ChatViewModel,
    transport: TcpTransport,
    keyStoreManager: KeyStoreManager,
    myPublicKeyHex: String,
    startDestination: String = "welcome",
    onWelcomeCompleted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("welcome") {
            WelcomeScreen(onEnter = {
                onWelcomeCompleted()
                navController.navigate("chat") {
                    popUpTo("welcome") { inclusive = true }
                }
            })
        }
        composable("chat") {
            ChatScreen(
                viewModel = chatViewModel,
                myPublicKeyHex = myPublicKeyHex,
                modifier = Modifier.fillMaxSize()
            )
        }
        composable("profile") {
            val peers by chatViewModel.peers.collectAsState()
            val currentPeer = remember(peers, myPublicKeyHex) {
                peers.find { it.publicKey.toHex() == myPublicKeyHex }
            }

            ProfileScreen(
                myPublicKeyHex = myPublicKeyHex,
                initialName = currentPeer?.name ?: "",
                initialStatus = currentPeer?.status ?: "",
                onSave = { name, status ->
                    chatViewModel.updateProfile(name, status)
                    navController.popBackStack()
                }
            )
        }
        composable("settings") {
            var showMnemonic by remember { mutableStateOf(false) }
            val mnemonic = remember {
                val privBytes = keyStoreManager.getPrivateKeyBytes()
                if (privBytes != null) {
                    IdentityBackupManager.getMnemonicForPrivateKey(privBytes)
                } else {
                    emptyList()
                }
            }

            SettingsScreen(
                onBackupIdentity = {
                    showMnemonic = true
                },
                onAddRelay = { address ->
                    transport.addRelay(address)
                }
            )

            if (showMnemonic) {
                AlertDialog(
                    onDismissRequest = { showMnemonic = false },
                    title = { Text("YOUR RECOVERY PHRASE") },
                    text = {
                        Column {
                            Text(
                                "Write down these 12 words and keep them safe. Anyone with this phrase can access your account.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(16.dp))
                            if (mnemonic.isNotEmpty()) {
                                Text(
                                    mnemonic.joinToString(" "),
                                    fontWeight = FontWeight.Bold,
                                    color = CabalPeerTeal,
                                    lineHeight = 22.sp
                                )
                            } else {
                                Text(
                                    "Unable to export seed phrase for hardware-backed key.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showMnemonic = false }) {
                            Text("DONE")
                        }
                    },
                    containerColor = CabalSurfaceDark,
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }
        }
        composable("about") {
            AboutScreen()
        }
    }
}

