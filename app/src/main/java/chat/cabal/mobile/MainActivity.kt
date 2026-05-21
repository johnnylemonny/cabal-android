package chat.cabal.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import chat.cabal.database.CabalDatabase
import chat.cabal.mobile.core.KeyStoreManager
import chat.cabal.mobile.core.SyncEngine
import chat.cabal.mobile.core.CabalSyncService
import android.content.Intent
import chat.cabal.mobile.ui.navigation.CabalNavGraph
import chat.cabal.mobile.ui.theme.CabalTheme
import chat.cabal.mobile.ui.viewmodel.MainViewModel
import chat.cabal.mobile.ui.viewmodel.MainViewModelFactory
import chat.cabal.mobile.network.NsdDiscovery
import chat.cabal.network.TcpTransport
import chat.cabal.protocol.CableCore
import chat.cabal.protocol.Crypto
import chat.cabal.mobile.core.toHex
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val database: CabalDatabase by inject()
    private val cableCore: CableCore by inject()
    private val transport: TcpTransport by inject()
    private val syncEngine: SyncEngine by inject()
    private val mainViewModel: MainViewModel by viewModel()
    private lateinit var discovery: NsdDiscovery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start Foreground Sync Service
        val serviceIntent = Intent(this, CabalSyncService::class.java)
        startForegroundService(serviceIntent)
        
        transport.start()
        
        discovery = NsdDiscovery(this)
        // Start discovery for the "default" cabal
        discovery.startDiscovery("default") { peerInfo ->
            lifecycleScope.launch {
                transport.connectToPeer(peerInfo.address, peerInfo.port)
                syncEngine.onPeerConnected("${peerInfo.address}:${peerInfo.port}")
            }
        }
        // Announce our presence
        discovery.announce("default", 13333)
        
        enableEdgeToEdge()
        val myPublicKeyHex = cableCore.publicKey.toHex()
        setContent {
            CabalTheme {
                MainApp(database, cableCore, transport, myPublicKeyHex, mainViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    database: CabalDatabase,
    cableCore: CableCore,
    transport: TcpTransport,
    myPublicKeyHex: String,
    mainViewModel: MainViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val cabals by mainViewModel.cabals.collectAsState()
    val peerCount by transport.connectionCount.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCabalKey by remember { mutableStateOf<String?>(null) }

    // Set first cabal as default
    LaunchedEffect(cabals) {
        if (selectedCabalKey == null && cabals.isNotEmpty()) {
            selectedCabalKey = cabals.first().key
        }
    }
    
    if (showAddDialog) {
        chat.cabal.mobile.ui.components.AddCabalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { key, name ->
                mainViewModel.addCabal(key, name)
                showAddDialog = false
            }
        )
    }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                ) {
                    chat.cabal.mobile.ui.components.PeerAvatar(myPublicKeyHex, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "My Identity",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            myPublicKeyHex.take(12) + "...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                HorizontalDivider()
                Text(
                    "My Cabals",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                )
                
                cabals.forEach { cabal ->
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Group, contentDesc = null) },
                        label = { Text(cabal.name) },
                        selected = selectedCabalKey == cabal.key,
                        onClick = {
                            selectedCabalKey = cabal.key
                            navController.navigate("chat")
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDesc = null) },
                    label = { Text("Add Cabal") },
                    selected = false,
                    onClick = {
                        showAddDialog = true
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(Modifier.weight(1f))
                HorizontalDivider()
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDesc = null) },
                    label = { Text("About") },
                    selected = currentRoute == "about",
                    onClick = {
                        navController.navigate("about")
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text(if (currentRoute == "about") "About" else "General")
                            if (currentRoute != "about") {
                                Text(
                                    text = if (peerCount > 0) "$peerCount peers connected" else "Searching for peers...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (peerCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDesc = "Menu")
                        }
                    },
                    actions = {
                        if (peerCount > 0) {
                            Icon(
                                Icons.Default.CloudDone, 
                                contentDesc = "Connected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp).padding(end = 16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            CabalNavGraph(
                navController = navController,
                database = database,
                cableCore = cableCore,
                syncEngine = syncEngine,
                myPublicKeyHex = myPublicKeyHex,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
