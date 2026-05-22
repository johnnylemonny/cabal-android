# Cabal Mobile (Native Kotlin Rewrite)

A modern, native Android P2P chat client for the **Cable protocol**, built for privacy, transparency, and high performance in 2026.

## 🌟 Key Features
- **100% Native Kotlin**: Leverages the full power of Android with Jetpack Compose and Material 3.
- **Privacy First (FOSS)**: Zero Google Play Services, zero tracking. All data is local-first.
- **True P2P Connectivity**: Direct communication via TCP sockets and MDNS local discovery.
- **Android 17 Ready**: Fully compatible with the latest Local Network Permission requirements (LNP).
- **Full History Sync**: Automatic request/serve cycle for missing messages between peers.
- **End-to-End Encryption (E2EE)**: Messages are secured with ChaCha20-Poly1305 and Ed25519 signatures.
- **Premium UI/UX**: Professional Material 3 interface with dynamic color support, adaptive icons, and smooth splash screens.
- **Persistent Identity**: Secure key management using Android KeyStore (with software fallback).

## 🛠️ Tech Stack (2026 Standard)
- **Language**: Kotlin 2.3.21 (K2 Compiler)
- **Java**: JDK 25 Adoptium
- **Build System**: Gradle 9.5 with AGP 9.2.1
- **UI**: Jetpack Compose (Material 3)
- **Dependency Injection**: Koin 4.2.1
- **Networking**: Ktor 3.5.0 (Sockets)
- **Local Database**: SQLDelight 2.3.2 (SQLite)
- **Cryptography**: Standard Java providers with Ed25519 and ChaCha20 support.

## 🏗️ Project Structure
- `:app`: The main Android application module (UI, ViewModels, Sync Engine, DI Modules).
- `:cable-protocol`: Pure Kotlin implementation of the Cable binary protocol and serialization.
- `:cable-network`: Networking layer handling TCP transports and peer discovery via Android NSD.

## 🚀 Getting Started
1. **Clone the repository.**
2. **Open in Android Studio** (Ladybug or newer recommended).
3. **Build and Run**: Use a device or emulator running Android 7.0+ (optimized for API 37 / Android 15+).
4. **Connect**: Open the app on two devices in the same Wi-Fi network to see them discover each other and sync history.

## 📖 Architecture Visualization
See `graphify-out/graph.html` for an interactive knowledge graph of the project architecture.

## 📜 License
MIT License - Open Source and Free Software.
