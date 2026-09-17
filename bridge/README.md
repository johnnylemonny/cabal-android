# Cabal <-> Cable Protocol Bridge

A lightweight, zero-configuration local bridge connecting the **Cabal v1 / Hyperswarm P2P network** (used by [cabal.chat](https://cabal.chat/)) with the **Cable Protocol (draft-8)** native Android application.

---

## 🌟 How It Works

```
┌─────────────────────────────────────────────────────────────┐
│                    cabal.chat (Web / P2P)                   │
│             BitTorrent DHT / Hyperswarm + Hypercore         │
└──────────────────────────────┬──────────────────────────────┘
                               │
                      Hyperswarm / Multi-Writer
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    cabal-chat-bridge                        │
│                                                             │
│  - Joins cabal://324eee92611cd877841c4de9fd5253e9dba...     │
│  - Receives web chat messages -> encodes Cable TextPosts    │
│  - Receives Android Cable posts -> publishes to Cabal room  │
│  - Serves Cable TCP on :13330 with Varint framing           │
│  - Broadcasts UDP LAN announcements on :13334               │
└──────────────────────────────┬──────────────────────────────┘
                               │
                        TCP :13330 (Cable)
                     UDP :13334 (Discovery)
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 Cabal Android Native App                    │
│                (Cable Protocol draft-8)                     │
└─────────────────────────────────────────────────────────────┘
```

1. **Hyperswarm & Cabal Swarm**: Connects to the public room `cabal://324eee92611cd877841c4de9fd5253e9dba6033329a837ee5f01beb005dffb2f` (or any custom room configured via `CABAL_KEY`).
2. **Cable TCP Listener (`13330`)**: Listens for incoming TCP connections from Android clients on the local network or Android emulator (`10.0.2.2`).
3. **UDP Discovery (`13334`)**: Periodically broadcasts peer presence announcements (`CABAL|default|13330`) across the local subnet so the Android app automatically discovers the bridge without manual IP entry.
4. **Bi-directional Translation**:
   - Web messages sent from [cabal.chat](https://cabal.chat/) are converted to Cable `TextPost` records signed with the bridge's Ed25519 identity and streamed to Android.
   - Posts created in the Android app are published as `chat/text` entries onto the Cabal feed and immediately appear on the web client.

---

## 🚀 Quick Start (No Docker Required)

### 1. Requirements
- Node.js **v18+** (v24 recommended).

### 2. Installation
Navigate to the `bridge` folder and install dependencies:
```bash
cd bridge
npm install
```

### 3. Start the Bridge
```bash
npm start
```

You should see output similar to:
```
====================================================
        CABAL <-> CABLE PROTOCOL BRIDGE            
====================================================
[Bridge] Cabal Room Key: 324eee92611cd877841c4de9fd5253e9dba6033329a837ee5f01beb005dffb2f
[Bridge] Default Channel: #default
[Bridge] TCP Listen Port: 13330
[Bridge] UDP Discovery Port: 13334
[Bridge] Initializing Cabal Core...
[Bridge] Cable TCP Server listening on 0.0.0.0:13330
[Bridge] UDP Discovery broadcaster online on port 13334
[Bridge] Connected to Cabal P2P Hyperswarm network.
```

---

## 📱 Connecting with Android

### Automatic LAN Discovery
If your Android phone or emulator is connected to the same Wi-Fi network, the app's UDP/NSD discovery automatically detects the bridge and connects within seconds.

### Manual Peer Link
You can also connect manually inside the app:
1. Open the Android app.
2. Tap the **Link** (🔗) icon in the top app bar.
3. Enter your computer's IP and port:
   - For Android Emulator: `10.0.2.2:13330`
   - For physical device on Wi-Fi: `192.168.x.x:13330`
4. Tap **CONNECT**.

---

## 🧪 Running Unit Tests

Run the test suite verifying Cable Protocol encoding, decoding, Blake2b hashing, and Ed25519 signatures:
```bash
npm test
```

---

## ⚙️ Configuration (Environment Variables)

| Variable | Default Value | Description |
|---|---|---|
| `CABAL_KEY` | `324eee92611cd877841c4de9fd5253e9dba6033329a837ee5f01beb005dffb2f` | Target Cabal swarm public room key |
| `DEFAULT_CHANNEL` | `default` | Default channel to bind (`default` matches cabal.chat) |
| `CABLE_TCP_PORT` | `13330` | TCP port for Cable protocol connections |
| `CABLE_UDP_PORT` | `13334` | UDP port for discovery announcements |
| `STORAGE_PATH` | System temp dir | Local storage directory for Hypercore data |
