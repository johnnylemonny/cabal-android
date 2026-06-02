package chat.cabal.mobile.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import chat.cabal.database.CabalDatabase
import chat.cabal.mobile.core.SyncEngine
import chat.cabal.mobile.ui.screens.ChatScreen
import chat.cabal.mobile.ui.screens.WelcomeScreen
import chat.cabal.mobile.ui.viewmodel.ChatViewModelFactory
import chat.cabal.protocol.CableCore

@Composable
fun CabalNavGraph(
    navController: NavHostController,
    database: CabalDatabase,
    cableCore: CableCore,
    syncEngine: SyncEngine,
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
                viewModel = viewModel(
                    factory = ChatViewModelFactory(database, cableCore, syncEngine)
                ),
                myPublicKeyHex = myPublicKeyHex
            )
        }
        composable("about") {
            AboutScreen()
        }
    }
}

@Composable
fun AboutScreen() {
    androidx.compose.material3.Text(
        text = "Cabal Mobile v1.0\nA decentralized P2P chat client.\nFOSS - MIT License",
        modifier = Modifier.padding(16.dp)
    )
}
