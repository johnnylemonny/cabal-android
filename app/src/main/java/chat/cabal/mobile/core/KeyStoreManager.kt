package chat.cabal.mobile.core

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey

class KeyStoreManager {
    private val keyAlias = "CabalIdentityKey"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    fun getOrCreateKeyPair(): KeyPair {
        try {
            if (!keyStore.containsAlias(keyAlias)) {
                generateKey()
            }
            val entry = keyStore.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry
            return KeyPair(entry.certificate.publicKey, entry.privateKey)
        } catch (e: Exception) {
            Log.e("KeyStoreManager", "Failed to get/create key: ${e.message}")
            // Fallback to non-KeyStore key if KeyStore is broken (e.g. some emulators)
            return generateSoftwareKey()
        }
    }

    private fun generateKey() {
        val kpg = KeyPairGenerator.getInstance(
            "Ed25519", "AndroidKeyStore"
        )
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .build()
        kpg.initialize(spec)
        kpg.generateKeyPair()
    }

    private fun generateSoftwareKey(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("Ed25519")
        return kpg.generateKeyPair()
    }
}
