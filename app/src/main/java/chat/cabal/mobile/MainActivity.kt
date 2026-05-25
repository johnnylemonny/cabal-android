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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import chat.cabal.database.CabalDatabase
import chat.cabal.mobile.core.*
import chat.cabal.mobile.network.NsdDiscovery
import chat.cabal.mobile.ui.components.AddCabalDialog
import chat.cabal.mobile.ui.components.PeerAvatar
import chat.cabal.mobile.ui.navigation.CabalNavGraph
import chat.cabal.mobile.ui.theme.CabalTheme
import chat.cabal.mobile.ui.viewmodel.MainViewModel
import chat.cabal.network.TcpTransport
import chat.cabal.protocol.CableCore
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            startDiscovery()
        } else {
            startDiscovery()
            Toast.makeText(this, "Permissions required for local sync", Toast.LENGTH_SHORT).show()
        }
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
        discovery = NsdDiscovery(this)

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
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (Build.VERSION.SDK_INT >= 37) {
            permissions.add("android.permission.ACCESS_LOCAL_NETWORK")
        }

        val allGranted = permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
        
        if (allGranted) {
            startDiscovery()
        } else {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun startDiscovery() {
        try {
            Log.d("MainActivity", "Starting NSD discovery for 'default'")
            discovery.startDiscovery("default") { peerInfo ->
                Log.d("MainActivity", "Found peer via NSD: ${peerInfo.address}:${peerInfo.port}")
                lifecycleScope.launch {
                    if (transport.connectToPeer(peerInfo.address, peerInfo.port)) {
                        Log.i("MainActivity", "Successfully connected to peer: ${peerInfo.address}")
                        syncEngine.onPeerConnected("${peerInfo.address}:${peerInfo.port}")
                    } else {
                        Log.w("MainActivity", "Failed to connect to discovered peer: ${peerInfo.address}")
                    }
                }
            }
            discovery.announce("default", 13333)
            Log.d("MainActivity", "NSD announcement sent for port 13333")
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
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val cabals by mainViewModel.cabals.collectAsState()
    val peerCount by transport.connectionCount.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCabalKey by remember { mutableStateOf<String?>(null) }

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
                            navController.navigate("chat")
                            scope.launch { drawerState.close() }
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
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
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
