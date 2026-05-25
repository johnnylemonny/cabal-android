# Cabal Android (Zipline Branch)

![Banner](banner.png)

Cabal is a **privacy-first, peer-to-peer (P2P)** chat application for Android. This is a specialized branch that uses **app.cash.zipline** as the JavaScript engine to ensure compatibility with modern Android standards.

---

## 📖 Table of Contents
- [Introduction](#introduction)
- [Why Zipline?](#why-zipline)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Comparison with v2](#comparison-with-v2)
- [Getting Started](#getting-started)
- [License](#license)

---

## 🚀 Introduction

Cabal is decentralized communication for the modern era. No central servers, no tracking, and no middleman. By using the **Cable protocol**, Cabal allows devices to find each other on local networks and sync chat history directly.

---

## 🔧 Why Zipline?

This branch is specifically engineered for **Android 17 (API 37)** and devices that require **16KB Page Size Alignment**. 

Legacy JavaScript engines like `quickjs-android` are physically incompatible with 16KB devices because their native binaries are aligned to 4KB. **Zipline** provides a modern, 16KB-ready successor that ensures Cabal remains operational on the newest Android hardware.

---

## 🌟 Key Features

- **100% Serverless**: Direct P2P communication via TCP and mDNS (NSD).
- **16KB Compatibility**: Engineered for the latest Android memory alignment standards.
- **E2EE Security**: Messages are signed with **Ed25519** and secured with ChaCha20-Poly1305.
- **Material 3 UI**: A sleek, modern interface with dynamic colors and adaptive icons.
- **Identity Management**: Secure identity storage using the **Android KeyStore**.

---

## 🏗️ Architecture

The Zipline branch leverages `ZiplineService` interfaces to create a secure, type-safe bridge between Kotlin and the Cable protocol logic running in JavaScript.

### Secure Most (Bridges)
- **`NetworkBridge`**: Real-time TCP broadcasting.
- **`StorageBridge`**: Interface with the native SQLDelight `kv_store`.
- **`UIBridge`**: Triggers Compose UI updates upon receiving new chat events.

---

## 🔄 Comparison with v2 (cabal-mobile)

| Feature | Original (v2) | This Rewrite (Zipline) |
| :--- | :--- | :--- |
| **Framework** | React Native | **100% Native Kotlin** |
| **UI Engine** | WebView/Native Components | **Jetpack Compose (Material 3)** |
| **JS Engine** | Standard JSC | **Zipline (16KB support)** |
| **Performance** | High Overhead | **Ultra-Low Latency** |
| **Android 17** | Incompatible | **Fully Ready** |

---

## 🏁 Getting Started

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

## 📜 License

This project is licensed under the **GNU Affero General Public License v3 (AGPL-3.0)**.

---
*Ensuring Cabal stays compatible with the next generation of Android.*
