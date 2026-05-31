<p align="center">
  <img src="cabal_brand_pack/png/cabal_lockup_horizontal_final_optical_stronger_dark_2400px.png" width="2400" alt="Cabal Banner">
</p>

Cabal is a **privacy-first, peer-to-peer (P2P)** chat application for Android. This specialized branch uses **app.cash.zipline** as its JavaScript engine to ensure full compatibility with modern Android standards and hardware (including **16KB page size** devices).

---

## 📖 Table of Contents

- [Introduction](#introduction)
- [Why Zipline?](#why-zipline)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [P2P Testing](#p2p-testing)
- [License](#license)

---

## Introduction

Cabal is decentralized communication for the modern era. No central servers, no tracking, and no middleman. By using the **Cable protocol**, Cabal allows devices to find each other on local networks and sync chat history directly, ensuring that your data stays on your device.

This project is a native Android implementation designed for the year 2026, targeting **Android 17 (API 37)** and emphasizing security, transparency, and a premium user experience.

<p align="center">
  <img src="cabal_brand_pack/png/cabal_icon_v2a_final_dark_preview_1024px.png" width="1024" alt="Cabal Icon">
</p>

---

## Why Zipline?

This branch is specifically engineered for **Android 17 (API 37)** and devices that require **16KB Page Size Alignment**. 

Legacy JavaScript engines like `quickjs-android` are physically incompatible with 16KB devices because their native binaries are aligned to 4KB. **Zipline** provides a modern, 16KB-ready successor that ensures Cabal remains operational on the newest Android hardware while maintaining the same protocol logic.

---

## Key Features

- **100% Serverless**: Direct P2P communication via TCP and dual-stack discovery (NSD + UDP).
- **16KB Compatibility**: Engineered for the latest Android memory alignment standards.
- **E2EE Security**: Messages are signed with **Ed25519** and secured with ChaCha20-Poly1305.
- **Material 3 UI**: A sleek, modern interface with dynamic colors and adaptive icons.
- **Identity Management**: Secure identity storage using software-backed Ed25519 (BouncyCastle).

---

## Architecture

The Zipline branch leverages `ZiplineService` interfaces to create a secure, type-safe bridge between Kotlin and the Cable protocol logic running in JavaScript.

- **`:app`**: The Android layer. Handles UI (Compose) and Zipline service orchestration.
- **`:cable-protocol`**: Pure Kotlin interfaces and shared logic.
- **`:cable-network`**: Handles discovery (NSD/UDP) and low-level socket communication using **Ktor**.

---

## Tech Stack

- **Kotlin 2.3.21** (K2 Compiler)
- **Jetpack Compose** for UI
- **Zipline 1.27.0** for JS Bridge
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
2. **Synchronization**: Once the app is opened on both devices, they will automatically announce themselves via NSD/UDP and establish a TCP connection.
3. **Emulator Testing**:
   Emulators reside in isolated networks. To link them, use ADB port forwarding:
   ```bash
   # PC port 13330 -> Emulator 5554
   adb -s emulator-5554 forward tcp:13330 tcp:13330
   # Emulator 5556 -> PC port 13330
   adb -s emulator-5556 reverse tcp:13330 tcp:13330
   ```
   Then, in the app on `emulator-5556`, tap the **Link (🔗)** icon and connect to `10.0.2.2:13330`.
4. **Chat**: Send a message on one device; it will appear on the second device via the Cable sync cycle.

---

## License

This project is licensed under the **GNU Affero General Public License v3 (AGPL-3.0)**.

---
*Ensuring Cabal stays compatible with the next generation of Android.*
