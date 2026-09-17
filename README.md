<div align="center">
  <img src="cabal_brand_pack/png/cabal_lockup_horizontal_final_optical_stronger_dark_2400px.png" width="800" alt="Cabal Banner">

  <p><strong>Decentralized, peer-to-peer, encrypted chat client for Android</strong></p>

  <p>
    <a href="https://www.gnu.org/licenses/agpl-3.0"><img src="https://img.shields.io/badge/License-AGPL%20v3-blue.svg" alt="License: AGPL v3"></a>
    <img src="https://img.shields.io/badge/Platform-Android%2017%20(API%2037)-green.svg" alt="Android API 37">
    <img src="https://img.shields.io/badge/Kotlin-2.3.21-purple.svg" alt="Kotlin">
    <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20(Material%203)-blueviolet.svg" alt="Compose Material 3">
    <img src="https://img.shields.io/badge/Security-AES--256--GCM%20%7C%20Ed25519-brightgreen.svg" alt="Security">
  </p>
</div>

---

## 📖 Table of Contents

- [Introduction](#introduction)
- [Key Features](#key-features)
- [Protocol & Network Architecture](#protocol--network-architecture)
- [Ecosystem Compatibility](#ecosystem-compatibility-cabalchat)
- [Security & Privacy Model](#security--privacy-model)
- [Tech Stack](#tech-stack)
- [Project Modules](#project-modules)
- [Getting Started](#getting-started)
- [P2P Testing & Relays](#p2p-testing--relays)
- [Quality & Verification](#quality--verification)
- [License](#license)

---

## Introduction

**Cabal Android** is a privacy-first, serverless communication client engineered natively for Android. It eliminates centralized servers, accounts, metadata surveillance, and third-party cloud infrastructure.

By implementing the binary **Cable Protocol (draft-8)**, devices discover each other locally (via mDNS and UDP broadcast) or connect over configurable TCP relays, syncing immutable append-only message graphs directly peer-to-peer.

Designed for modern Android with **Jetpack Compose (Material 3)**, edge-to-edge system integration, hardware-backed identity protection, and zero cloud dependencies.

---

## Key Features

- **100% Serverless & Offline First**: True P2P synchronization over local Wi-Fi, hotspot, or routed TCP connections.
- **Hardware-Backed Identity Security**: Cryptographic private keys protected via `EncryptedSharedPreferences` (AES-256-GCM) and Android Keystore.
- **Biometric App Lock**: Optional biometric authentication (BiometricPrompt) to unlock chat history and session keys.
- **BIP-39 Identity Backup**: Deterministic 12-word recovery seed phrase generation and restoration.
- **Material 3 Interface**: High-contrast "Deep Dark" aesthetic with glassmorphism, responsive navigation drawer, and smooth edge-to-edge support.
- **Offline Typography**: Pre-bundled, local Inter font assets — zero network font queries for strict offline security (compatible with GrapheneOS and CalyxOS).
- **Threaded Conversations**: Reply directly to specific messages with parent reference linking.
- **Periodic Message TTL Cleanup**: Automated background garbage collection of expired messages via Android WorkManager.

---

## Protocol & Network Architecture

Cabal operates as a multi-writer, append-only graph synchronized using the **Cable Protocol**:

1. **Identity & Signatures**: Every client generates an **Ed25519** keypair upon first launch. All posts (text, info, topic, deletion) are cryptographically signed.
2. **Hashing & Content Addressing**: Posts are uniquely identified by their **BLAKE2b-256** hash.
3. **Peer Discovery**:
   - **Network Service Discovery (mDNS / NSD)** for standard zero-configuration local network discovery.
   - **UDP Broadcast** as an automatic fallback for networks where multicast DNS packets are dropped or restricted.
4. **Transport**: Direct TCP streaming sockets (`TcpTransport`) with binary framed Varint-encoded Cable messages.

---

## Ecosystem Compatibility (`cabal.chat`)

| Environment | Protocol / Transport | Compatibility Status |
| :--- | :--- | :--- |
| **Cabal Android (This Client)** | Cable Protocol (draft-8) over TCP + NSD/UDP | **Native P2P** (Android-to-Android, LAN, TCP Relays) |
| **Cabal Desktop / CLI (`cabal.chat`)** | Hypercore Protocol / Hyperswarm DHT | **Different protocol stack** (requires bridge or adapter) |

> [!NOTE]
> The traditional desktop and terminal clients hosted at [cabal.chat](https://cabal.chat) rely on JavaScript Hypercore/Hyperswarm distributed append-only logs. This native Android client implements the newer **Cable Protocol specification**. Direct synchronization between this client and public `cabal.chat` desktop swarms requires an active bridge node or the upcoming Noise handshake transport layer.

---

## Security & Privacy Model

- **No Remote Telemetry**: Zero analytics, trackers, crash-reporting SDKs, or third-party web endpoints.
- **No Google Play Services Dependency**: Operates seamlessly on de-Googled ROMs (GrapheneOS, CalyxOS, LineageOS).
- **Secure Storage**: Identity keys are stored using `MasterKey.KeyScheme.AES256_GCM` via AndroidX Security.
- **App Sandboxing**: Backups are disabled (`android:allowBackup="false"`) to prevent unencrypted ADB extraction of private keys and chat databases.

---

## Tech Stack

- **Language**: Kotlin 2.3.21 (K2 compiler, JVM 25 target)
- **UI Framework**: Jetpack Compose with Material 3 & Compose BOM 2026.09.00
- **Database**: SQLDelight 2.3.2 with Android SQLite Driver
- **Dependency Injection**: Koin 4.2.1
- **Background Tasks**: AndroidX WorkManager 2.11.2
- **Cryptography**: Bouncy Castle (Ed25519, BLAKE2b, ChaCha20-Poly1305) & AndroidX Security Crypto

---

## Project Modules

```
├── app/               # Android application layer (UI, Compose Screens, ViewModels, DI, Workers)
├── cable-protocol/    # Pure Kotlin Cable Protocol (draft-8) binary serialization & crypto
└── cable-network/     # P2P TCP socket transport & composite peer discovery (NSD + UDP)
```

---

## Getting Started

### Prerequisites

- **Android Studio Ladybug (2024.2+)** or newer
- **JDK 21 or 25** (e.g. Eclipse Temurin 25)
- Android SDK with Platform 37 installed

### Build & Install

```bash
# Clone the repository
git clone https://github.com/johnnylemonny/cabal-android.git
cd cabal-android

# Build Debug APK
./gradlew assembleDebug

# Install on connected device or emulator
./gradlew installDebug
```

---

## P2P Testing & Relays

### Local Wi-Fi / Hotspot Testing
1. Connect two Android devices to the same local Wi-Fi access point or phone hotspot.
2. Launch Cabal on both devices.
3. Peer discovery will automatically detect the counterpart and establish a TCP connection.

### Android Emulator Testing
Because emulator instances exist on isolated virtual subnets, link them via ADB:
```bash
# Forward port from Host to Emulator 1
adb -s emulator-5554 forward tcp:13330 tcp:13330

# Reverse port from Emulator 2 to Host
adb -s emulator-5556 reverse tcp:13330 tcp:13330
```
In the app on `emulator-5556`, tap the **Link** icon in the top app bar and connect to `10.0.2.2:13330`.

---

## Quality & Verification

Run automated test suites and linters:

```bash
# Execute unit tests across all modules
./gradlew test

# Run Android Lint analysis
./gradlew lintDebug
```

---

## License

Copyright (C) 2026 johnnylemonny

Licensed under the **GNU Affero General Public License v3 (AGPL-3.0)**. See the [LICENSE](LICENSE) file for details.

