# Graph Report - .  (2026-05-24)

## Corpus Check
- Corpus is ~32,931 words - fits in a single context window. You may not need a graph.

## Summary
- 729 nodes · 1681 edges · 50 communities detected
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_JS Protocol Core|JS Protocol Core]]
- [[_COMMUNITY_Protocol Event Management|Protocol Event Management]]
- [[_COMMUNITY_State and Resource Tracking|State and Resource Tracking]]
- [[_COMMUNITY_Protocol Operations|Protocol Operations]]
- [[_COMMUNITY_Kotlin Protocol Models|Kotlin Protocol Models]]
- [[_COMMUNITY_Protocol Utils|Protocol Utils]]
- [[_COMMUNITY_JS Storage Bridge|JS Storage Bridge]]
- [[_COMMUNITY_Networking & Requests|Networking & Requests]]
- [[_COMMUNITY_Protocol Internal Functions|Protocol Internal Functions]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_QuickJS Engine Bridge|QuickJS Engine Bridge]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Sync Engine (Kotlin)|Sync Engine (Kotlin)]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_TCP Transport (Kotlin)|TCP Transport (Kotlin)]]
- [[_COMMUNITY_Crypto Utilities (Kotlin)|Crypto Utilities (Kotlin)]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Android Activity & Lifecycle|Android Activity & Lifecycle]]
- [[_COMMUNITY_Android Sync Service|Android Sync Service]]
- [[_COMMUNITY_Peer Discovery (Kotlin)|Peer Discovery (Kotlin)]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Key Store Management|Key Store Management]]
- [[_COMMUNITY_NSD Discovery Bridge|NSD Discovery Bridge]]
- [[_COMMUNITY_Cable Core Logic (Kotlin)|Cable Core Logic (Kotlin)]]
- [[_COMMUNITY_Varint Encoding|Varint Encoding]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Application & Initialization|Application & Initialization]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Chat ViewModel|Chat ViewModel]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 50|Community 50]]
- [[_COMMUNITY_Community 56|Community 56]]

## God Nodes (most connected - your core abstractions)
1. `call()` - 63 edges
2. `push()` - 46 edges
3. `create()` - 43 edges
4. `set()` - 42 edges
5. `get()` - 39 edges
6. `all()` - 38 edges
7. `map()` - 38 edges
8. `constructor()` - 30 edges
9. `#N()` - 24 edges
10. `toJSON()` - 24 edges

## Surprising Connections (you probably didn't know these)
- `constructor()` --calls--> `Ms()`  [EXTRACTED]
  app\src\main\assets\cable-protocol.bundle.js → app\src\main\assets\cable-protocol.bundle.js  _Bridges community 16 → community 2_
- `kf()` --calls--> `create()`  [EXTRACTED]
  app\src\main\assets\cable-protocol.bundle.js → app\src\main\assets\cable-protocol.bundle.js  _Bridges community 16 → community 3_
- `XE()` --calls--> `call()`  [EXTRACTED]
  app\src\main\assets\cable-protocol.bundle.js → app\src\main\assets\cable-protocol.bundle.js  _Bridges community 3 → community 2_
- `[ws]()` --calls--> `Pf()`  [EXTRACTED]
  app\src\main\assets\cable-protocol.bundle.js → app\src\main\assets\cable-protocol.bundle.js  _Bridges community 16 → community 19_
- `WE()` --calls--> `pop()`  [EXTRACTED]
  app\src\main\assets\cable-protocol.bundle.js → app\src\main\assets\cable-protocol.bundle.js  _Bridges community 16 → community 1_

## Communities

### Community 0 - "JS Protocol Core"
Cohesion: 0.02
Nodes (2): Bp(), op()

### Community 1 - "Protocol Event Management"
Cohesion: 0.05
Nodes (67): a_(), abort(), addEventListener(), addLiveStateRequest(), addRole(), analyze(), ao(), _attemptPrune() (+59 more)

### Community 2 - "State and Resource Tracking"
Cohesion: 0.05
Nodes (62): attachResource(), call(), checkIfHeads(), clearInfo(), clearMembership(), clearTopic(), constructor(), #D() (+54 more)

### Community 3 - "Protocol Operations"
Cohesion: 0.07
Nodes (62): $(), ah(), all(), an(), assignRole(), block(), blockUsers(), bw() (+54 more)

### Community 4 - "Kotlin Protocol Models"
Cohesion: 0.04
Nodes (13): CableMessage, CableParser, CablePost, ChannelListRequest, HashResponse, InfoPost, JoinPost, LeavePost (+5 more)

### Community 5 - "Protocol Utils"
Cohesion: 0.09
Nodes (41): BI(), bo(), f_(), Gu(), h_(), ht(), Hu(), _I() (+33 more)

### Community 6 - "JS Storage Bridge"
Cohesion: 0.08
Nodes (26): batch(), _chainedBatch(), _checkKey(), _checkValue(), createBufferTranscoder(), createUTF8Transcoder(), demoteAdmin(), e() (+18 more)

### Community 7 - "Networking & Requests"
Cohesion: 0.09
Nodes (30): broadcast(), cancelRequest(), decrementTTL(), dispatchRequest(), dispatchResponse(), Ec(), emitPeerNew(), forwardRequest() (+22 more)

### Community 8 - "Protocol Internal Functions"
Cohesion: 0.11
Nodes (28): ay(), da(), dx(), Ea(), ex(), Fg(), _g(), ga() (+20 more)

### Community 9 - "Community 9"
Cohesion: 0.13
Nodes (22): ad(), Al(), Bl(), bt(), cd(), cl(), fd(), fl() (+14 more)

### Community 10 - "QuickJS Engine Bridge"
Cohesion: 0.12
Nodes (4): NetworkBridge, QuickJsEngine, StorageBridge, UIBridge

### Community 11 - "Community 11"
Cohesion: 0.15
Nodes (13): ca(), close(), [cn](), cp(), end(), Gg(), hp(), next() (+5 more)

### Community 12 - "Sync Engine (Kotlin)"
Cohesion: 0.18
Nodes (3): JsChatMessage, JsPost, SyncEngine

### Community 13 - "Community 13"
Cohesion: 0.36
Nodes (10): as(), DC(), fh(), fs(), Lc(), mC(), NC(), rn() (+2 more)

### Community 14 - "TCP Transport (Kotlin)"
Cohesion: 0.2
Nodes (1): TcpTransport

### Community 15 - "Crypto Utilities (Kotlin)"
Cohesion: 0.2
Nodes (1): Crypto

### Community 16 - "Community 16"
Cohesion: 0.22
Nodes (9): Ef(), eI(), kf(), kI(), Ms(), Pf(), Rf(), WE() (+1 more)

### Community 17 - "Community 17"
Cohesion: 0.25
Nodes (8): Bn(), _emitStoredPost(), jI(), Qo(), _reindexHash(), Tn(), _y(), Ye()

### Community 18 - "Community 18"
Cohesion: 0.32
Nodes (8): ag(), Bc(), cg(), hg(), qA(), Ro(), TC(), Wp()

### Community 19 - "Community 19"
Cohesion: 0.29
Nodes (7): detachResource(), [ds](), iS(), nt(), [ws](), Ys(), zI()

### Community 20 - "Community 20"
Cohesion: 0.33
Nodes (6): Fn(), kw(), Mw(), Rw(), so(), tl()

### Community 21 - "Android Activity & Lifecycle"
Cohesion: 0.33
Nodes (1): MainActivity

### Community 22 - "Android Sync Service"
Cohesion: 0.33
Nodes (1): CabalSyncService

### Community 23 - "Peer Discovery (Kotlin)"
Cohesion: 0.33
Nodes (2): PeerDiscovery, PeerInfo

### Community 24 - "Community 24"
Cohesion: 0.4
Nodes (5): ax(), createViewTranscoder(), cx(), fx(), Lg()

### Community 25 - "Community 25"
Cohesion: 0.4
Nodes (5): dg(), El(), Q(), Qc(), YC()

### Community 26 - "Community 26"
Cohesion: 0.4
Nodes (5): mx(), Rx(), uh(), wx(), xx()

### Community 27 - "Key Store Management"
Cohesion: 0.4
Nodes (1): KeyStoreManager

### Community 28 - "NSD Discovery Bridge"
Cohesion: 0.4
Nodes (1): NsdDiscovery

### Community 29 - "Cable Core Logic (Kotlin)"
Cohesion: 0.4
Nodes (1): CableCore

### Community 30 - "Varint Encoding"
Cohesion: 0.4
Nodes (1): Varint

### Community 31 - "Community 31"
Cohesion: 0.5
Nodes (4): Gy(), max(), os(), vy()

### Community 32 - "Community 32"
Cohesion: 0.5
Nodes (4): fI(), hy(), Je(), Sy()

### Community 33 - "Community 33"
Cohesion: 0.67
Nodes (3): getLatestInfoHash(), getName(), _reindexInfoName()

### Community 34 - "Community 34"
Cohesion: 0.67
Nodes (3): ap(), fp(), Gs()

### Community 35 - "Community 35"
Cohesion: 0.67
Nodes (3): Hn(), Jl(), mo()

### Community 36 - "Community 36"
Cohesion: 0.67
Nodes (3): Pc(), Ul(), Zd()

### Community 37 - "Application & Initialization"
Cohesion: 0.67
Nodes (1): CabalApplication

### Community 38 - "Community 38"
Cohesion: 0.67
Nodes (1): CabalManager

### Community 41 - "Chat ViewModel"
Cohesion: 0.67
Nodes (1): ChatViewModel

### Community 42 - "Community 42"
Cohesion: 0.67
Nodes (1): ChatViewModelFactory

### Community 43 - "Community 43"
Cohesion: 0.67
Nodes (1): MainViewModel

### Community 44 - "Community 44"
Cohesion: 0.67
Nodes (1): MainViewModelFactory

### Community 45 - "Community 45"
Cohesion: 1.0
Nodes (2): Gc(), wI()

### Community 46 - "Community 46"
Cohesion: 1.0
Nodes (2): isDeleted(), _storeExternalBuf()

### Community 47 - "Community 47"
Cohesion: 1.0
Nodes (2): cI(), Jf()

### Community 48 - "Community 48"
Cohesion: 1.0
Nodes (2): log(), log_tee()

### Community 49 - "Community 49"
Cohesion: 1.0
Nodes (2): Kg(), Ug()

### Community 50 - "Community 50"
Cohesion: 1.0
Nodes (2): oA(), oI()

### Community 56 - "Community 56"
Cohesion: 1.0
Nodes (1): Constants

## Knowledge Gaps
- **4 isolated node(s):** `JsChatMessage`, `JsPost`, `PeerInfo`, `Constants`
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `JS Protocol Core`** (88 nodes): `cable-protocol.bundle.js`, `af()`, `aI()`, `Bp()`, `calculatedSize()`, `commonName()`, `count()`, `cr()`, `ct()`, `Cy()`, `D0()`, `dispose()`, `disposeAfter()`, `drop()`, `_emitChannels()`, `_emitChat()`, `_emitModeration()`, `_emitUsers()`, `ep()`, `FC()`, `fetchMethod()`, `Ff()`, `G0()`, `getDroppedChannels()`, `getDroppedPosts()`, `getDroppedUsers()`, `getHiddenPosts()`, `getHiddenUsers()`, `Gf()`, `gp()`, `ha()`, `hI()`, `hide()`, `Hw()`, `Ip()`, `isBlocked()`, `isDropped()`, `isHidden()`, `isUserBlocked()`, `isUserDropped()`, `isUserHidden()`, `Jn()`, `js()`, `jw()`, `length()`, `limit()`, `[Ln]()`, `maxSize()`, `Mf()`, `mp()`, `nm()`, `[Nr]()`, `ny()`, `op()`, `Ow()`, `pI()`, `pp()`, `qf()`, `qg()`, `Qr()`, `Qw()`, `resolveHashes()`, `rp()`, `[Rr]()`, `sI()`, `size()`, `Sp()`, `status()`, `tA()`, `tp()`, `ty()`, `undrop()`, `unhide()`, `unsafeExposeInternals()`, `V0()`, `"wasm-binary:./blake2b.wat"()`, `"wasm-binary:./sha256.wat"()`, `"wasm-binary:./sha512.wat"()`, `"wasm-binary:./siphash24.wat"()`, `"wasm-binary:./xsalsa20.wat"()`, `wg()`, `Wh()`, `Ww()`, `ya()`, `yI()`, `Yw()`, `yx()`, `zt()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `TCP Transport (Kotlin)`** (10 nodes): `TcpTransport.kt`, `TcpTransport`, `.broadcast()`, `.connectToPeer()`, `.handleConnection()`, `.readVarint()`, `.send()`, `.sendToPeer()`, `.start()`, `.stop()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Crypto Utilities (Kotlin)`** (10 nodes): `Crypto.kt`, `Crypto`, `.blake2b()`, `.decrypt()`, `.diffieHellman()`, `.encrypt()`, `.generateKeyPair()`, `.randomBytes()`, `.sign()`, `.verify()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Android Activity & Lifecycle`** (6 nodes): `MainActivity.kt`, `MainActivity`, `.checkAndRequestPermissions()`, `.onCreate()`, `.startDiscovery()`, `MainApp()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Android Sync Service`** (6 nodes): `CabalSyncService.kt`, `CabalSyncService`, `.createNotificationChannel()`, `.onBind()`, `.onCreate()`, `.onStartCommand()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Peer Discovery (Kotlin)`** (6 nodes): `Discovery.kt`, `PeerDiscovery`, `.announce()`, `.startDiscovery()`, `.stopDiscovery()`, `PeerInfo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Key Store Management`** (5 nodes): `KeyStoreManager.kt`, `KeyStoreManager`, `.generateKey()`, `.generateSoftwareKey()`, `.getOrCreateKeyPair()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `NSD Discovery Bridge`** (5 nodes): `NsdDiscovery.kt`, `NsdDiscovery`, `.announce()`, `.startDiscovery()`, `.stopDiscovery()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Cable Core Logic (Kotlin)`** (5 nodes): `CableCore.kt`, `CableCore`, `.createJoinPost()`, `.createTextPost()`, `.decryptText()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Varint Encoding`** (5 nodes): `Varint.kt`, `Varint`, `.decode()`, `.encode()`, `.size()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Application & Initialization`** (3 nodes): `CabalApplication.kt`, `CabalApplication`, `.onCreate()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 38`** (3 nodes): `CabalManager.kt`, `CabalManager`, `.getPublicKeyHex()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Chat ViewModel`** (3 nodes): `ChatViewModel.kt`, `ChatViewModel`, `.sendMessage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 42`** (3 nodes): `ChatViewModelFactory.kt`, `ChatViewModelFactory`, `.create()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 43`** (3 nodes): `MainViewModel.kt`, `MainViewModel`, `.addCabal()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 44`** (3 nodes): `MainViewModelFactory.kt`, `MainViewModelFactory`, `.create()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 45`** (2 nodes): `Gc()`, `wI()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 46`** (2 nodes): `isDeleted()`, `_storeExternalBuf()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 47`** (2 nodes): `cI()`, `Jf()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 48`** (2 nodes): `log()`, `log_tee()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 49`** (2 nodes): `Kg()`, `Ug()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 50`** (2 nodes): `oA()`, `oI()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 56`** (2 nodes): `Constants.kt`, `Constants`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `e()` connect `JS Storage Bridge` to `JS Protocol Core`, `Protocol Event Management`, `State and Resource Tracking`, `Community 11`?**
  _High betweenness centrality (0.017) - this node is a cross-community bridge._
- **Why does `call()` connect `State and Resource Tracking` to `JS Protocol Core`, `Protocol Event Management`, `Community 33`, `Protocol Operations`, `Protocol Utils`, `JS Storage Bridge`, `Networking & Requests`, `Community 46`, `Community 49`?**
  _High betweenness centrality (0.004) - this node is a cross-community bridge._
- **Why does `get()` connect `Protocol Event Management` to `JS Protocol Core`, `State and Resource Tracking`, `Protocol Operations`, `Protocol Utils`, `JS Storage Bridge`, `Networking & Requests`, `Protocol Internal Functions`, `Community 17`?**
  _High betweenness centrality (0.003) - this node is a cross-community bridge._
- **What connects `JsChatMessage`, `JsPost`, `PeerInfo` to the rest of the system?**
  _4 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `JS Protocol Core` be split into smaller, more focused modules?**
  _Cohesion score 0.02 - nodes in this community are weakly interconnected._
- **Should `Protocol Event Management` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._
- **Should `State and Resource Tracking` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._