# R8/ProGuard Rules for Cabal Mobile (2026)

# Bouncy Castle optimization
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**

# Ktor Sockets
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# SQLDelight
-keep class chat.cabal.database.** { *; }

# Java 25 Cryptography
-keep class java.security.** { *; }
-keep class javax.crypto.** { *; }

# Compose
-dontwarn androidx.compose.**
