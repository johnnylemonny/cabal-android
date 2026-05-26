# Graph Report - new-cabal-mobile  (2026-05-26)

## Corpus Check
- 39 files · ~26,061 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 801 nodes · 1764 edges · 81 communities (43 shown, 38 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS · INFERRED: 6 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `a9dee1e7`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 50|Community 50]]
- [[_COMMUNITY_Community 51|Community 51]]
- [[_COMMUNITY_Community 52|Community 52]]
- [[_COMMUNITY_Community 53|Community 53]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 61|Community 61]]
- [[_COMMUNITY_Community 62|Community 62]]
- [[_COMMUNITY_Community 63|Community 63]]
- [[_COMMUNITY_Community 64|Community 64]]
- [[_COMMUNITY_Community 65|Community 65]]
- [[_COMMUNITY_Community 66|Community 66]]
- [[_COMMUNITY_Community 67|Community 67]]
- [[_COMMUNITY_Community 68|Community 68]]
- [[_COMMUNITY_Community 69|Community 69]]
- [[_COMMUNITY_Community 70|Community 70]]
- [[_COMMUNITY_Community 71|Community 71]]
- [[_COMMUNITY_Community 72|Community 72]]

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
- `MainApp()` --calls--> `AddCabalDialog()`  [INFERRED]
  app/src/main/java/chat/cabal/mobile/MainActivity.kt → app/src/main/java/chat/cabal/mobile/ui/components/AddCabalDialog.kt
- `MainApp()` --calls--> `PeerAvatar()`  [INFERRED]
  app/src/main/java/chat/cabal/mobile/MainActivity.kt → app/src/main/java/chat/cabal/mobile/ui/components/PeerAvatar.kt
- `MainApp()` --calls--> `CabalNavGraph()`  [INFERRED]
  app/src/main/java/chat/cabal/mobile/MainActivity.kt → app/src/main/java/chat/cabal/mobile/ui/navigation/NavGraph.kt

## Communities (81 total, 38 thin omitted)

### Community 1 - "Community 1"
Cohesion: 0.05
Nodes (85): all(), attachResource(), call(), checkIfHeads(), clearInfo(), clearMembership(), clearTopic(), constructor() (+77 more)

### Community 2 - "Community 2"
Cohesion: 0.06
Nodes (63): a_(), abort(), addEventListener(), addLiveStateRequest(), addRole(), analyze(), ao(), #b() (+55 more)

### Community 3 - "Community 3"
Cohesion: 0.08
Nodes (51): $(), ah(), assignRole(), block(), blockUsers(), cancelRequest(), create(), defer() (+43 more)

### Community 4 - "Community 4"
Cohesion: 0.08
Nodes (46): BI(), bo(), $c(), decrement(), f_(), Gu(), h_(), ht() (+38 more)

### Community 5 - "Community 5"
Cohesion: 0.07
Nodes (23): batch(), _chainedBatch(), _checkKey(), _checkValue(), close(), [cn](), e(), end() (+15 more)

### Community 6 - "Community 6"
Cohesion: 0.09
Nodes (33): ay(), da(), dx(), Ea(), ex(), Fg(), _g(), ga() (+25 more)

### Community 7 - "Community 7"
Cohesion: 0.1
Nodes (24): _attemptPrune(), broadcast(), decrementTTL(), dispatchResponse(), Ec(), emitPeerLost(), emitPeerNew(), forwardRequest() (+16 more)

### Community 8 - "Community 8"
Cohesion: 0.13
Nodes (22): ad(), Al(), Bl(), bt(), cd(), cl(), fd(), fl() (+14 more)

### Community 9 - "Community 9"
Cohesion: 0.12
Nodes (7): AddCabalDialog(), PeerAvatar(), MainActivity, MainApp(), CabalNavGraph(), NsdDiscovery, CabalTheme()

### Community 10 - "Community 10"
Cohesion: 0.13
Nodes (18): Android, Android Implementation, Brand Identity Decision, Cabal Final Brand Package — v2A Optical Stronger, code:text (svg/cabal_lockup_horizontal_final_optical_stronger_white.svg), code:text (svg/      Scalable Vector Graphics (Source of Truth)), code:text (android/ic_cabal_mark_v2a_foreground.xml), code:text (android/ic_cabal_splash_icon.xml) (+10 more)

### Community 11 - "Community 11"
Cohesion: 0.11
Nodes (17): Architecture, Build Instructions, Cabal Android, code:bash (git clone https://github.com/johnnylemonny/cabal-android.git), code:bash (./gradlew assembleDebug), code:bash (# On the first emulator), Comparison with v2, Getting Started (+9 more)

### Community 12 - "Community 12"
Cohesion: 0.12
Nodes (4): NetworkBridge, QuickJsEngine, StorageBridge, UIBridge

### Community 13 - "Community 13"
Cohesion: 0.14
Nodes (3): JsChatMessage, JsPost, SyncEngine

### Community 14 - "Community 14"
Cohesion: 0.17
Nodes (11): Agent Instructions, Beads Issue Tracker, code:bash (bd ready              # Find available work), code:bash (# Force overwrite without prompting), code:bash (bd ready              # Find available work), code:bash (git pull --rebase), Non-Interactive Shell Commands, Quick Reference (+3 more)

### Community 17 - "Community 17"
Cohesion: 0.36
Nodes (10): as(), DC(), fh(), fs(), Lc(), mC(), NC(), rn() (+2 more)

### Community 19 - "Community 19"
Cohesion: 0.22
Nodes (8): 🏗️ Architecture, Cabal Android (Zipline Branch), 🔄 Key Differences (Zipline vs. Master), 📜 License, Secure Most (Bridges), 📖 Table of Contents, 🛠️ Technical Benefits, 🚀 Why Zipline?

### Community 20 - "Community 20"
Cohesion: 0.22
Nodes (9): Ef(), eI(), kf(), kI(), Ms(), Pf(), Rf(), WE() (+1 more)

### Community 21 - "Community 21"
Cohesion: 0.22
Nodes (9): Bn(), _emitStoredPost(), jI(), Qo(), _reindexChannelMembership(), _reindexHash(), Tn(), _y() (+1 more)

### Community 22 - "Community 22"
Cohesion: 0.32
Nodes (8): ag(), Bc(), cg(), hg(), qA(), Ro(), TC(), Wp()

### Community 26 - "Community 26"
Cohesion: 0.33
Nodes (6): Fn(), kw(), Mw(), Rw(), so(), tl()

### Community 30 - "Community 30"
Cohesion: 0.4
Nodes (5): ax(), createViewTranscoder(), cx(), fx(), Lg()

### Community 31 - "Community 31"
Cohesion: 0.4
Nodes (5): dg(), El(), Q(), Qc(), YC()

### Community 43 - "Community 43"
Cohesion: 0.5
Nodes (4): ca(), cp(), hp(), xA()

### Community 44 - "Community 44"
Cohesion: 0.5
Nodes (4): Gy(), max(), os(), vy()

### Community 45 - "Community 45"
Cohesion: 0.5
Nodes (4): fI(), hy(), Je(), Sy()

### Community 52 - "Community 52"
Cohesion: 0.67
Nodes (3): ap(), fp(), Gs()

### Community 53 - "Community 53"
Cohesion: 0.67
Nodes (3): an(), bw(), XE()

### Community 54 - "Community 54"
Cohesion: 0.67
Nodes (3): getLatestInfoHash(), getName(), _reindexInfoName()

### Community 55 - "Community 55"
Cohesion: 0.67
Nodes (3): Pc(), Ul(), Zd()

### Community 56 - "Community 56"
Cohesion: 0.67
Nodes (3): Hn(), Jl(), mo()

## Knowledge Gaps
- **34 isolated node(s):** `pb`, `sodium`, `PeerInfo`, `Constants`, `code:bash (bd ready              # Find available work)` (+29 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **38 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `e()` connect `Community 5` to `Community 0`, `Community 1`, `Community 2`?**
  _High betweenness centrality (0.014) - this node is a cross-community bridge._
- **Why does `call()` connect `Community 1` to `Community 0`, `Community 2`, `Community 3`, `Community 4`, `Community 5`, `Community 53`, `Community 54`, `Community 62`, `Community 63`?**
  _High betweenness centrality (0.004) - this node is a cross-community bridge._
- **Why does `get()` connect `Community 2` to `Community 0`, `Community 1`, `Community 65`, `Community 3`, `Community 4`, `Community 5`, `Community 6`, `Community 7`, `Community 21`?**
  _High betweenness centrality (0.003) - this node is a cross-community bridge._
- **What connects `pb`, `sodium`, `PeerInfo` to the rest of the system?**
  _34 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.02 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._