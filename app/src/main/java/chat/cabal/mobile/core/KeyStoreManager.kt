package chat.cabal.mobile.core

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey

class KeyStoreManager {
    private val keyAlias = "CabalIdentityKey"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    fun getOrCreateKeyPair(): KeyPair {
        if (!keyStore.containsAlias(keyAlias)) {
            generateKey()
        }
        val entry = keyStore.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry
        return KeyPair(entry.certificate.publicKey, entry.privateKey)
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
}
