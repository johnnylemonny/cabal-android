package chat.cabal.mobile.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    myPublicKeyHex: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = modifier
    ) {
        composable("welcome") {
            WelcomeScreen(onEnter = {
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
            ProfileScreen(
                myPublicKeyHex = myPublicKeyHex,
                onSave = { name, status ->
                    chatViewModel.updateProfile(name, status)
                    navController.popBackStack()
                }
            )
        }
        composable("settings") {
            var showMnemonic by remember { mutableStateOf(false) }
            val mnemonic = remember { 
                listOf("abandon", "ability", "able", "about", "above", "absent", "absorb", "abstract", "absurd", "abuse", "access", "accident")
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
                            Text("Write down these 12 words and keep them safe. Anyone with this phrase can access your account.", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(16.dp))
                            Text(mnemonic.joinToString(" "), fontWeight = FontWeight.Bold, color = CabalPeerTeal)
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
