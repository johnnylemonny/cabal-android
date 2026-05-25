# Cabal Android

![Banner](banner.png)

Cabal is a **privacy-first, peer-to-peer (P2P)** chat application for Android. It is a modern, high-performance rewrite of the original Cabal Mobile client, built from the ground up using **Kotlin**, **Jetpack Compose**, and the **Cable protocol**.

---

## 📖 Table of Contents
- [Introduction](#introduction)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Comparison with v2](#comparison-with-v2)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [P2P Testing](#p2p-testing)
- [License](#license)

---

## 🚀 Introduction

Cabal is decentralized communication for the modern era. No central servers, no tracking, and no middleman. By using the **Cable protocol**, Cabal allows devices to find each other on local networks and sync chat history directly, ensuring that your data stays on your device.

This project is a native Android implementation designed for the year 2026, targeting **Android 17 (API 37)** and emphasizing security, transparency, and a premium user experience.

---

## 🌟 Key Features

- **100% Serverless**: Direct P2P communication via TCP and mDNS (NSD).
- **History Sync**: Automatic protocol-level reconciliation of missing messages between peers.
- **E2EE Security**: Messages are signed with **Ed25519** and encrypted where applicable.
- **Material 3 UI**: A sleek, modern interface with dynamic colors and adaptive icons.
- **Privacy First**: No Google Play Services required. Zero telemetry.
- **Identity Management**: Secure identity storage using the **Android KeyStore**.

---

## 🏗️ Architecture

The project is divided into three main modules:
- **`:app`**: The Android layer. Handles UI (Compose), ViewModels, and integration with the protocol bridge.
- **`:cable-protocol`**: Pure Kotlin library implementing the binary serialization and logic of the Cable protocol.
- **`:cable-network`**: Handles discovery (NSD) and low-level socket communication using **Ktor**.

### Hybrid Protocol Bridge
Cabal uses a unique hybrid approach:
- **Kotlin** for the system-level features and UI.
- **QuickJS** (or **Zipline** on the 16KB branch) as the protocol engine, ensuring logic parity with the desktop client while maintaining native performance.

---

## 🔄 Comparison with v2 (cabal-mobile)

| Feature | Original (v2) | This Rewrite (Native) |
| :--- | :--- | :--- |
| **Framework** | React Native | **100% Native Kotlin** |
| **UI Engine** | WebView/Native Components | **Jetpack Compose (Material 3)** |
| **Performance** | High Overhead | **Low Latency / High Performance** |
| **Startup Time** | Slow | **Instant** |
| **Battery Life** | Average | **Optimized for Sockets** |
| **JS Engine** | Standard JSC | **QuickJS / Zipline (16KB support)** |

---

## 🛠️ Tech Stack

- **Kotlin 2.3.21** (K2 Compiler)
- **Jetpack Compose** for UI
- **SQLDelight 2.3.2** for local SQLite storage
- **Ktor 3.5.0** for P2P networking
- **Koin 4.2.1** for Dependency Injection
- **Android 17 Ready** (supports 16KB page sizes)

---

## 🏁 Getting Started

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

## 📱 P2P Testing

To test the chat functionality between two devices:

1. **Local Network**: Connect two Android devices (or emulators) to the same Wi-Fi network.
2. **Discovery**: Both devices must have **Local Network Permissions** enabled.
3. **Synchronization**: Once the app is opened on both devices, they will automatically announce themselves via NSD and establish a TCP connection.
4. **Chat**: Send a message on one device; it will appear on the second device via the Cable sync cycle.

*Note: For testing between two emulators on the same PC, you may need to use `adb forward` if mDNS is not routed between virtual instances.*

---

## 📜 License

This project is licensed under the **GNU Affero General Public License v3 (AGPL-3.0)**. 

This is a **restrictive copyleft license** that ensures that if you modify the code and run it as a service or application, you must provide the source code of those modifications to the public. This keeps Cabal open and free for everyone.

---
*Built with ❤️ for the decentralized web.*
