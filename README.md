# Cabal Mobile (Native Kotlin Rewrite)

A modern, native Android P2P chat client for the **Cable protocol**, built for privacy, transparency, and high performance in 2026.

## 🌟 Key Features
- **100% Native Kotlin**: Leverages the full power of Android with Jetpack Compose.
- **Privacy First (FOSS)**: Zero Google Play Services, zero tracking. All data is local-first.
- **True P2P Connectivity**: Direct communication via TCP sockets and MDNS local discovery.
- **End-to-End Encryption (E2EE)**: Messages are secured with ChaCha20-Poly1305.
- **Professional UI/UX**: Material 3 Design with dynamic colors, micro-interactions, and premium animations.
- **Persistent Identity**: Secure key management using Android KeyStore.

## 🛠️ Tech Stack (2026 Standard)
- **Language**: Kotlin 2.3.21 (K2 Compiler)
- **Java**: JDK 25 Adoptium
- **Build System**: Gradle 9.2 with AGP 9.2.0
- **UI**: Jetpack Compose (Material 3)
- **Dependency Injection**: Koin 4.2.1
- **Networking**: Ktor 3.5.0 (Sockets)
- **Local Database**: SQLDelight 2.3.2
- **Cryptography**: Bouncy Castle & standard Java security providers

## 🏗️ Project Structure
- `:app`: The main Android application module (UI, ViewModels, Sync Engine).
- `:cable-protocol`: Pure Kotlin implementation of the Cable binary protocol and serialization.
- `:cable-network`: Networking layer handling TCP transports and peer discovery.

## 🚀 Getting Started
1. **Clone the repository.**
2. **Open in Android Studio** (Ladybug or newer recommended).
3. **Build and Run**: Use a device or emulator running Android 7.0+ (optimized for API 37).
4. **Connect**: Open the app on two devices in the same Wi-Fi network to see them discover each other and sync history.

## 📖 Agent Instructions
This project uses **beads** (`bd`) for issue tracking. See [AGENTS.md](AGENTS.md) for full workflow context.

## 📜 License
MIT License - Open Source and Free Software.
