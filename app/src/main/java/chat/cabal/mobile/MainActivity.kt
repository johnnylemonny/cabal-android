package chat.cabal.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import chat.cabal.database.CabalDatabase
import chat.cabal.mobile.core.*
import chat.cabal.mobile.network.CompositeDiscovery
import chat.cabal.mobile.network.NsdDiscovery
import chat.cabal.mobile.network.UdpDiscovery
import chat.cabal.mobile.ui.components.AddCabalDialog
import chat.cabal.mobile.ui.components.PeerAvatar
import chat.cabal.mobile.ui.navigation.CabalNavGraph
import chat.cabal.mobile.ui.theme.CabalTheme
import chat.cabal.mobile.ui.viewmodel.MainViewModel
import chat.cabal.network.PeerDiscovery
import chat.cabal.network.TcpTransport
import chat.cabal.protocol.CableCore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val database: CabalDatabase by inject()
    private val cableCore: CableCore by inject()
    private val transport: TcpTransport by inject()
    private val syncEngine: SyncEngine by inject()
    private val mainViewModel: MainViewModel by viewModel()
    private val scope: CoroutineScope by inject()
    private lateinit var discovery: PeerDiscovery

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        startDiscovery()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        try {
            val serviceIntent = Intent(this, CabalSyncService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start service: ${e.message}")
        }
        
        transport.start()
        
        // Composite discovery using both NSD (mDNS) and UDP Broadcast
        discovery = CompositeDiscovery(listOf(
            NsdDiscovery(this),
            UdpDiscovery(this, scope)
        ))

        checkAndRequestPermissions()

        val myPublicKeyHex = try { cableCore.publicKey.toHex() } catch (_: Exception) { "unknown" }
        
        setContent {
            CabalTheme {
                MainApp(database, cableCore, transport, syncEngine, myPublicKeyHex, mainViewModel)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        
        val allGranted = permissions.all { 
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED 
        }
        
        if (allGranted) {
            startDiscovery()
        } else {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun startDiscovery() {
        try {
            Log.i("MainActivity", "Starting Peer Discovery (NSD + UDP) for 'default'")
            discovery.startDiscovery("default") { peerInfo ->
                Log.i("MainActivity", "Peer Discovered: ${peerInfo.address}:${peerInfo.port}")
                // Ensure this runs on IO thread, not Main thread
                scope.launch(Dispatchers.IO) {
                    transport.connectToPeer(peerInfo.address, peerInfo.port)
                }
            }
            discovery.announce("default", 13330)
            Log.d("MainActivity", "NSD announcement sent for port 13330")
        } catch (e: Exception) {
            Log.e("MainActivity", "Discovery error: ${e.message}", e)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    database: CabalDatabase,
    cableCore: CableCore,
    transport: TcpTransport,
    syncEngine: SyncEngine,
    myPublicKeyHex: String,
    mainViewModel: MainViewModel
) {
    val localNavController = rememberNavController()
    val localDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val localComposableScope = rememberCoroutineScope()
    val localContext = androidx.compose.ui.platform.LocalContext.current
    
    val cabals by mainViewModel.cabals.collectAsState()
    val peerCount by transport.connectionCount.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var selectedCabalKey by remember { mutableStateOf<String?>(null) }
    var manualIp by remember { mutableStateOf("10.0.2.2") }
    var manualPort by remember { mutableStateOf("13330") }

    LaunchedEffect(cabals) {
        if (selectedCabalKey == null && cabals.isNotEmpty()) {
            selectedCabalKey = cabals.first().key
        }
    }
    
    if (showAddDialog) {
        AddCabalDialog(
            onDismiss = { 
                showAddDialog = false 
            },
            onConfirm = { key, name ->
                mainViewModel.addCabal(key, name)
                showAddDialog = false
            }
        )
    }

    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = { Text("Manual Peer Link") },
            text = {
                Column {
                    Text("Enter IP address of the peer. For emulators, use 10.0.2.2.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualIp,
                        onValueChange = { manualIp = it },
                        label = { Text("Peer IP") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualPort,
                        onValueChange = { manualPort = it },
                        label = { Text("Port") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    localComposableScope.launch {
                        val p = manualPort.toIntOrNull() ?: 13330
                        val success = transport.connectToPeer(manualIp, p)
                        if (success) {
                            Toast.makeText(localContext, "Link request sent to $manualIp:$p", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(localContext, "Could not reach $manualIp:$p", Toast.LENGTH_LONG).show()
                        }
                    }
                    showLinkDialog = false
                }) { Text("Connect") }
            },
            dismissButton = {
                TextButton(onClick = { showLinkDialog = false }) { Text("Cancel") }
            }
        )
    }
    
    val navBackStackEntry by localNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = localDrawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                ) {
                    PeerAvatar(myPublicKeyHex, modifier = Modifier.size(48.dp))
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
                        icon = { Icon(Icons.Default.Group, contentDescription = null) },
                        label = { Text(cabal.name) },
                        selected = (selectedCabalKey == cabal.key),
                        onClick = {
                            selectedCabalKey = cabal.key
                            localNavController.navigate("chat")
                            localComposableScope.launch { localDrawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
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
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("About") },
                    selected = (currentRoute == "about"),
                    onClick = {
                        localNavController.navigate("about")
                        localComposableScope.launch { localDrawerState.close() }
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
                        val titleText = if (currentRoute == "about") "About" else {
                            cabals.find { it.key == selectedCabalKey }?.name ?: "General"
                        }
                        Column {
                            Text(titleText)
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
                            localComposableScope.launch { localDrawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            showLinkDialog = true
                        }) {
                            Icon(Icons.Default.Link, contentDescription = "Manual Link")
                        }

                        if (peerCount > 0) {
                            Icon(
                                Icons.Default.CloudDone, 
                                contentDescription = "Connected",
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
                navController = localNavController,
                database = database,
                cableCore = cableCore,
                syncEngine = syncEngine,
                myPublicKeyHex = myPublicKeyHex,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
