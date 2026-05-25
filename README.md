# Cabal Android (Zipline Branch)

<p align="center">
  <img src="cabal_brand_pack/png/cabal_lockup_horizontal_final_optical_stronger_dark_2400px.png" alt="Cabal Banner">
</p>

Cabal is a **privacy-first, peer-to-peer (P2P)** chat application for Android. This specialized branch uses **app.cash.zipline** as its JavaScript engine to ensure full compatibility with modern Android standards and hardware.

---

## 📖 Table of Contents

- [Introduction](#introduction)
- [Why Zipline?](#why-zipline)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Comparison with v2](#comparison-with-v2)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [P2P Testing](#p2p-testing)
- [Troubleshooting](#troubleshooting)
- [License](#license)

---

## Introduction

Cabal is decentralized communication for the modern era. No central servers, no tracking, and no middleman. By using the **Cable protocol**, Cabal allows devices to find each other on local networks and sync chat history directly, ensuring that your data stays on your device.

This project is a native Android implementation designed for the year 2026, targeting **Android 17 (API 37)** and emphasizing security, transparency, and a premium user experience.

<p align="center">
  <img src="cabal_brand_pack/png/cabal_icon_v2a_final_dark_preview_1024px.png" alt="Cabal Icon">
</p>

---

## Why Zipline?

This branch is specifically engineered for **Android 17 (API 37)** and devices that require **16KB Page Size Alignment**. 

Legacy JavaScript engines like `quickjs-android` are physically incompatible with 16KB devices because their native binaries are aligned to 4KB. **Zipline** provides a modern, 16KB-ready successor that ensures Cabal remains operational on the newest Android hardware while maintaining the same protocol logic.

---

## Key Features

- **100% Serverless**: Direct P2P communication via TCP and mDNS (NSD).
- **16KB Compatibility**: Engineered for the latest Android memory alignment standards.
- **E2EE Security**: Messages are signed with **Ed25519** and secured with ChaCha20-Poly1305.
- **Material 3 UI**: A sleek, modern interface with dynamic colors and adaptive icons.
- **Identity Management**: Secure identity storage using the **Android KeyStore**.

---

## Architecture

The Zipline branch leverages `ZiplineService` interfaces to create a secure, type-safe bridge between Kotlin and the Cable protocol logic running in JavaScript.

- **`:app`**: The Android layer. Handles UI (Compose) and Zipline service orchestration.
- **`:cable-protocol`**: Pure Kotlin interfaces and shared logic.
- **`:cable-network`**: Handles discovery (NSD) and low-level socket communication using **Ktor**.

### Secure Bridges
- **`NetworkBridge`**: Real-time TCP broadcasting.
- **`StorageBridge`**: Interface with the native SQLDelight `kv_store`.
- **`UIBridge`**: Triggers Compose UI updates upon receiving new chat events.

---

## Comparison with v2

| Feature | Original (v2) | This Rewrite (Zipline) |
| :--- | :--- | :--- |
| **Framework** | React Native | **100% Native Kotlin** |
| **UI Engine** | WebView/Native Components | **Jetpack Compose (Material 3)** |
| **Performance** | High Overhead | **Ultra-Low Latency** |
| **JS Engine** | Standard JSC | **Zipline (16KB support)** |
| **Android 17** | Incompatible | **Fully Ready** |

---

## Tech Stack

- **Kotlin 2.3.21** (K2 Compiler)
- **Jetpack Compose** for UI
- **Zipline 1.18.0** for JS Bridge
- **SQLDelight 2.3.2** for local SQLite storage
- **Ktor 3.5.0** for P2P networking
- **Koin 4.2.1** for Dependency Injection
- **Android 17 Ready** (supports 16KB page sizes)

---

## Getting Started

### Prerequisites
- **Android Studio Ladybug** (or newer)
- **JDK 21/25**

### Build Instructions
1. Clone the repo and switch to the zipline branch:
   ```bash
   git checkout feature/zipline-16kb-compatibility
   ```
2. Open the project in Android Studio.
3. Build the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

---

## P2P Testing

To test the chat functionality between two devices:

1. **Local Network**: Connect two Android devices (or emulators) to the same Wi-Fi network.
2. **Discovery**: Both devices must have **Local Network Permissions** enabled.
3. **Synchronization**: Once the app is opened on both devices, they will automatically announce themselves via NSD and establish a TCP connection.
4. **Chat**: Send a message on one device; it will appear on the second device via the Cable sync cycle.

---

## Troubleshooting

### Peers Not Found (Emulators)
Emulators on the same machine often cannot "see" each other via mDNS (NSD) because they reside in separate virtual networks.
1. **IP Routing**: By default, an emulator cannot reach another emulator via its internal IP (e.g., `10.0.2.15`).
2. **ADB Port Forwarding**: To connect two emulators on the same PC:
   ```bash
   # On the first emulator
   adb -s emulator-5554 forward tcp:13333 tcp:13333
   ```
   Then the other emulator can connect to `10.0.2.2:13333`.
3. **Physical Hardware**: P2P discovery and sync work automatically when using real Android devices on the same Wi-Fi network with Local Network Permissions enabled.

---

## License

This project is licensed under the **GNU Affero General Public License v3 (AGPL-3.0)**.

---
*Ensuring Cabal stays compatible with the next generation of Android.*
