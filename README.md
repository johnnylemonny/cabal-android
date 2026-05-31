# Cabal Android

<p align="center">
  <img src="cabal_brand_pack/png/cabal_lockup_horizontal_final_optical_stronger_dark_2400px.png" width="2400" alt="Cabal Banner">
</p>

Cabal is a **privacy-first, peer-to-peer (P2P)** chat application for Android. It is a modern, high-performance rewrite of the original [cabal-mobile](https://github.com/cabal-club/cabal-mobile) client, built from the ground up using **Kotlin**, **Jetpack Compose**, and the **Cable protocol**.

---

## 📖 Table of Contents

- [Introduction](#introduction)
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
  <img src="cabal_brand_pack/png/cabal_icon_v2a_final_dark_preview_1024px.png" width="1024" alt="Cabal Icon">
</p>

---

## Key Features

- **100% Serverless**: Direct P2P communication via TCP and mDNS (NSD).
- **History Sync**: Automatic protocol-level reconciliation of missing messages between peers.
- **E2EE Security**: Messages are signed with **Ed25519** and encrypted where applicable.
- **Material 3 UI**: A sleek, modern interface with dynamic colors and adaptive icons.
- **Privacy First**: No Google Play Services required. Zero telemetry.
- **Identity Management**: Secure identity storage using the **Android KeyStore**.

---

## Architecture

The project is divided into three main modules:
- **`:app`**: The Android layer. Handles UI (Compose), ViewModels, and integration with the protocol logic.
- **`:cable-protocol`**: Pure Kotlin library implementing the binary serialization and logic of the Cable protocol.
- **`:cable-network`**: Handles discovery (NSD) and low-level socket communication using **Ktor**.

---

## Comparison with v2

This project is based on the [Cable Protocol Specification](https://github.com/cabal-club/cable-spec) and is designed to provide a native alternative to the original React Native implementation.

| Feature | Original (v2) | This Rewrite (Native) |
| :--- | :--- | :--- |
| **Framework** | React Native | **100% Native Kotlin** |
| **UI Engine** | WebView/Native Components | **Jetpack Compose (Material 3)** |
| **Performance** | High Overhead | **Low Latency / High Performance** |
| **Startup Time** | Slow | **Instant** |
| **Battery Life** | Average | **Optimized for Sockets** |
| **Security** | JS-based Crypto | **Hardware-backed KeyStore** |

---

## Tech Stack

- **Kotlin 2.3.21** (K2 Compiler)
- **Jetpack Compose** for UI
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
1. Clone the repo:
   ```bash
   git clone https://github.com/johnnylemonny/cabal-android.git
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
2. **Synchronization**: Once the app is opened on both devices, they will automatically announce themselves via NSD/UDP and establish a TCP connection.
3. **Emulator Testing**:
   Emulators reside in isolated networks. To link them, use ADB port forwarding:
   ```bash
   # PC port 13330 -> Emulator 5554
   adb -s emulator-5554 forward tcp:13330 tcp:13330
   # Emulator 5556 -> PC port 13330
   adb -s emulator-5556 reverse tcp:13330 tcp:13330
   ```
   Then, in the app on `emulator-5556`, tap the **Link** icon and connect to `10.0.2.2:13330`.
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
*Built with ❤️ for the decentralized web.*
