# Cabal Mobile (Hybrid QuickJS Migration)

A modern, hybrid Android P2P chat client for the **Cable protocol**, utilizing the original JavaScript protocol implementation via **QuickJS** while maintaining a 100% native Jetpack Compose UI.

## 🌟 Key Features
- **Hybrid Architecture**: Runs the original `cable-core.js` logic in a high-performance **QuickJS** engine.
- **100% Native UI**: Jetpack Compose and Material 3 for a premium, fast, and responsive user experience.
- **Privacy First (FOSS)**: Zero Google Play Services, zero tracking. All data is local-first.
- **True P2P Connectivity**: Direct communication via TCP sockets and MDNS local discovery.
- **Android 17 Ready**: Fully compatible with the latest Local Network Permission requirements (LNP) and 16 KB page size optimization.
- **Persistence**: JS storage calls are seamlessly bridged to native **SQLDelight** (SQLite).
- **Persistent Identity**: Secure key management using Android KeyStore.

## 🛠️ Tech Stack (2026 Standard)
- **Language**: Kotlin 2.3.21 (K2 Compiler)
- **JS Engine**: QuickJS (CashApp's `quickjs-android:0.9.2`)
- **Java**: JDK 25 Adoptium
- **Build System**: Gradle 9.5 with AGP 9.2.1
- **UI**: Jetpack Compose (Material 3)
- **Dependency Injection**: Koin 4.2.1
- **Networking**: Ktor 3.5.0 (Sockets)
- **Local Database**: SQLDelight 2.3.2 (SQLite)
- **Serialization**: Kotlinx Serialization 1.11.0 (for Kotlin-JS bridge)

## 🏗️ Project Structure
- `:app`: The main Android application module (UI, ViewModels, Sync Engine, QuickJS Bridge).
- `:cable-network`: Networking layer handling TCP transports and peer discovery via Android NSD.
- `app/src/main/assets/cable-protocol.bundle.js`: Pre-compiled bundle of the JS protocol logic.

## 🚀 Getting Started
1. **Clone the repository.**
2. **Open in Android Studio** (Ladybug or newer recommended).
3. **Build and Run**: Use a device or emulator running Android 7.0+ (optimized for API 37 / Android 15+).
4. **Connect**: Open the app on two devices in the same Wi-Fi network to see them discover each other and sync history.

## 📖 Architecture Visualization
See `graphify-out/graph.html` for an interactive knowledge graph of the project architecture.

## 📜 License
MIT License - Open Source and Free Software.
