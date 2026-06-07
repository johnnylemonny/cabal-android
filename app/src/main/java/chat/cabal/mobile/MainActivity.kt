package chat.cabal.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import chat.cabal.database.CabalDatabase
import chat.cabal.mobile.core.toHex
import chat.cabal.mobile.core.*
import chat.cabal.mobile.network.CompositeDiscovery
import chat.cabal.mobile.network.NsdDiscovery
import chat.cabal.mobile.network.UdpDiscovery
import chat.cabal.mobile.ui.components.PeerAvatar
import chat.cabal.mobile.ui.navigation.CabalNavGraph
import chat.cabal.mobile.ui.theme.CabalTheme
import chat.cabal.mobile.ui.theme.*
import chat.cabal.mobile.ui.viewmodel.MainViewModel
import chat.cabal.network.PeerDiscovery
import chat.cabal.network.TcpTransport
import chat.cabal.protocol.CableCore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : FragmentActivity() {
    private val database: CabalDatabase by inject()
    private val cableCore: CableCore by inject()
    private val transport: TcpTransport by inject()
    private val syncEngine: SyncEngine by inject()
    private val mainViewModel: MainViewModel by viewModel()
    private val scope: CoroutineScope by inject()
    private var discovery: PeerDiscovery? = null
    private val biometricAuthManager: BiometricAuthManager by lazy { BiometricAuthManager(this) }
    private var isUnlocked by mutableStateOf(value = false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        startDiscovery()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        // Start Biometric Auth if supported
        if (biometricAuthManager.canAuthenticate()) {
            biometricAuthManager.authenticate(
                activity = this,
                onSuccess = { 
                    isUnlocked = true
                    initializeSystem()
                },
                onError = { error ->
                    Log.e("MainActivity", "Auth failed: $error")
                    // If user cancels, we stay on locked screen
                }
            )
        } else {
            isUnlocked = true
            initializeSystem()
        }

        val myPublicKeyHex: String = try { cableCore.publicKey.toHex() } catch (_: Exception) { "unknown" }
        
        setContent {
            CabalTheme {
                if (isUnlocked) {
                    MainApp(database, cableCore, transport, syncEngine, myPublicKeyHex, mainViewModel)
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(CabalDeepBlack), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cabal_splash_icon),
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = CabalCipherBlue
                            )
                            Spacer(Modifier.height(32.dp))
                            CircularProgressIndicator(color = CabalPeerTeal)
                            Text(
                                "CABAL LOCKED", 
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            Button(
                                onClick = { retryAuth() },
                                modifier = Modifier.padding(top = 32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Text("UNLOCK")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun retryAuth() {
        biometricAuthManager.authenticate(
            activity = this,
            onSuccess = { 
                isUnlocked = true
                initializeSystem()
            },
            onError = { Log.e("MainActivity", "Auth failed: $it") }
        )
    }

    private fun initializeSystem() {
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
        discovery = CompositeDiscovery(
            listOf(
                NsdDiscovery(this),
                UdpDiscovery(this, scope)
            )
        )

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        
        if (Build.VERSION.SDK_INT >= 35) { // Android 15+
            permissions.add("android.permission.ACCESS_LOCAL_NETWORK")
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
            discovery?.startDiscovery("default") { peerInfo ->
                Log.i("MainActivity", "Peer Discovered: ${peerInfo.address}:${peerInfo.port}")
                scope.launch(Dispatchers.IO) {
                    transport.connectToPeer(peerInfo.address, peerInfo.port)
                }
            }
            discovery?.announce("default", 13330)
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
    mainViewModel: MainViewModel,
) {
    val chatViewModel: chat.cabal.mobile.ui.viewmodel.ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = chat.cabal.mobile.ui.viewmodel.ChatViewModelFactory(database, cableCore, syncEngine)
    )
    val localNavController = rememberNavController()
    val localDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val localComposableScope = rememberCoroutineScope()
    
    val cabals by mainViewModel.cabals.collectAsState()
    val peerCount by transport.connectionCount.collectAsState()
    val showAddDialog = remember { mutableStateOf(false) }
    val showLinkDialog = remember { mutableStateOf(false) }
    val selectedCabalKey = remember { mutableStateOf<String?>(null) }
    
    val navBackStackEntry by localNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (showAddDialog.value) {
        var newCabalKey by remember { mutableStateOf("") }
        var newCabalName by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddDialog.value = false },
            title = { Text("CREATE OR JOIN CABAL", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextField(
                        value = newCabalName,
                        onValueChange = { newCabalName = it },
                        label = { Text("Cabal Name (e.g. Friends)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                        )
                    )
                    TextField(
                        value = newCabalKey,
                        onValueChange = { newCabalKey = it },
                        label = { Text("Cabal Key (hex)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Leave empty to generate new") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val key = if (newCabalKey.isBlank()) {
                            // Generate a random hex key
                            (1..32).joinToString("") { "%02x".format((0..255).random()) }
                        } else newCabalKey
                        mainViewModel.addCabal(key, newCabalName.ifBlank { "Unnamed Cabal" })
                        showAddDialog.value = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CabalPeerTeal)
                ) {
                    Text("CONFIRM", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog.value = false }) {
                    Text("CANCEL", color = CabalMuted)
                }
            },
            containerColor = CabalSurfaceDark,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    if (showLinkDialog.value) {
        var relayAddress by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showLinkDialog.value = false },
            title = { Text("MANUAL PEER LINK", fontWeight = FontWeight.Black) },
            text = {
                TextField(
                    value = relayAddress,
                    onValueChange = { relayAddress = it },
                    placeholder = { Text("10.0.2.2:13330", color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                    )
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (relayAddress.isNotBlank()) {
                        val parts = relayAddress.split(":")
                        val host = parts[0]
                        val port = parts.getOrNull(1)?.toIntOrNull() ?: 13330
                        localComposableScope.launch(Dispatchers.IO) {
                            transport.connectToPeer(host, port)
                        }
                        showLinkDialog.value = false
                    }
                }) {
                    Text("CONNECT")
                }
            },
            containerColor = CabalSurfaceDark,
            titleContentColor = Color.White
        )
    }

    ModalNavigationDrawer(
        drawerState = localDrawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CabalSurfaceDark,
                drawerContentColor = Color.White,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
                    ),
                drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
            ) {
                Spacer(Modifier.height(48.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { 
                            localNavController.navigate("profile")
                            localComposableScope.launch { localDrawerState.close() }
                        }
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    PeerAvatar(myPublicKeyHex, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text(
                            "My Identity",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            myPublicKeyHex.take(16).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = CabalPeerTeal,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.05f)
                )
                
                Text(
                    "PEER NETWORKS",
                    style = MaterialTheme.typography.labelMedium,
                    color = CabalCipherBlue.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.5.sp,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                )
                
                cabals.forEach { cabal ->
                    NavigationDrawerItem(
                        icon = { 
                            Icon(
                                if (selectedCabalKey.value == cabal.key) Icons.Default.CloudDone else Icons.Default.Group, 
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            ) 
                        },
                        label = { 
                            Text(
                                cabal.name.uppercase(), 
                                fontWeight = if (selectedCabalKey.value == cabal.key) FontWeight.ExtraBold else FontWeight.Medium,
                                letterSpacing = 1.sp
                            ) 
                        },
                        selected = (selectedCabalKey.value == cabal.key),
                        onClick = {
                            selectedCabalKey.value = cabal.key
                            chatViewModel.setChannel(cabal.name)
                            localNavController.navigate("chat")
                            localComposableScope.launch { localDrawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = CabalCipherBlue.copy(alpha = 0.15f),
                            selectedIconColor = CabalCipherBlue,
                            selectedTextColor = CabalCipherBlue,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = CabalMuted,
                            unselectedTextColor = CabalMuted
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("NEW CABAL", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
                    selected = false,
                    onClick = { 
                        showAddDialog.value = true 
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = CabalPeerTeal.copy(alpha = 0.8f),
                        unselectedTextColor = CabalPeerTeal.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(Modifier.weight(1f))
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("SETTINGS", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
                    selected = (currentRoute == "settings"),
                    onClick = {
                        localNavController.navigate("settings")
                        localComposableScope.launch { localDrawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = CabalMuted,
                        unselectedTextColor = CabalMuted,
                        selectedIconColor = CabalCipherBlue,
                        selectedTextColor = CabalCipherBlue
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("ABOUT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
                    selected = (currentRoute == "about"),
                    onClick = {
                        localNavController.navigate("about")
                        localComposableScope.launch { localDrawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = CabalMuted,
                        unselectedTextColor = CabalMuted,
                        selectedIconColor = CabalCipherBlue,
                        selectedTextColor = CabalCipherBlue
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute != "welcome") {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        title = { 
                            val titleText = if (currentRoute == "settings") "SETTINGS" else {
                                cabals.find { it.key == selectedCabalKey.value }?.name?.uppercase() ?: "GENERAL"
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (currentRoute != "settings") {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_cabal_mark_v2a_foreground),
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(10.dp))
                                    }
                                    Text(
                                        text = titleText,
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleLarge,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                                if (currentRoute != "settings") {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(if (peerCount > 0) MaterialTheme.colorScheme.secondary else CabalMuted)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (peerCount > 0) "$peerCount PEERS SYNCING" else "NETWORK SCANNING...",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp,
                                            color = if (peerCount > 0) MaterialTheme.colorScheme.secondary else CabalMuted
                                        )
                                    }
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
                                showLinkDialog.value = true
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
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            CabalNavGraph(
                navController = localNavController,
                chatViewModel = chatViewModel,
                transport = transport,
                myPublicKeyHex = myPublicKeyHex,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
