package chat.cabal.mobile.core

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import chat.cabal.protocol.Crypto
import java.security.KeyPair
import java.util.*

class CabalManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "cabal_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var keyPair: KeyPair
        private set

    init {
        val savedPublicKey = sharedPrefs.getString("public_key", null)
        if (savedPublicKey == null) {
            val newKp = Crypto.generateKeyPair()
            keyPair = newKp
            // In a real app, we'd save the private key safely too.
            // For now, we regenerate if missing (simplified).
            sharedPrefs.edit().putString("public_key", newKp.public.encoded.toHex()).apply()
        } else {
            // Simplified: in a real app, we load from seed or keystore
            keyPair = Crypto.generateKeyPair() 
        }
    }

    fun getPublicKeyHex(): String {
        return keyPair.public.encoded.takeLast(32).toByteArray().toHex()
    }
}
