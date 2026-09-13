package chat.cabal.mobile.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class KeyStoreManager(private val context: Context) {
    private val TAG = "KeyStoreManager"
    private val PREFS_NAME = "cabal_identity_secure"
    private val LEGACY_PREFS_NAME = "cabal_identity"
    private val KEY_PUB = "pub"
    private val KEY_PRIV = "priv"

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences, using private SharedPreferences", e)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getOrCreateKeyPair(): KeyPair {
        var pubBase64 = prefs.getString(KEY_PUB, null)
        var privBase64 = prefs.getString(KEY_PRIV, null)

        if (pubBase64 == null || privBase64 == null) {
            val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            val legacyPub = legacyPrefs.getString(KEY_PUB, null)
            val legacyPriv = legacyPrefs.getString(KEY_PRIV, null)
            if (legacyPub != null && legacyPriv != null) {
                pubBase64 = legacyPub
                privBase64 = legacyPriv
                saveKeyPair(legacyPub, legacyPriv)
                legacyPrefs.edit { clear() }
            }
        }

        if (pubBase64 != null && privBase64 != null) {
            try {
                val kf = KeyFactory.getInstance("Ed25519")
                val publicKey = kf.generatePublic(X509EncodedKeySpec(Base64.decode(pubBase64, Base64.DEFAULT)))
                val privateKey = kf.generatePrivate(PKCS8EncodedKeySpec(Base64.decode(privBase64, Base64.DEFAULT)))
                return KeyPair(publicKey, privateKey)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore key, generating new", e)
            }
        }

        val kpg = KeyPairGenerator.getInstance("Ed25519")
        val kp = kpg.generateKeyPair()

        saveKeyPair(
            Base64.encodeToString(kp.public.encoded, Base64.DEFAULT),
            Base64.encodeToString(kp.private.encoded, Base64.DEFAULT)
        )

        Log.i(TAG, "New Ed25519 identity generated and securely stored")
        return kp
    }

    fun saveKeyPair(pubBase64: String, privBase64: String) {
        prefs.edit {
            putString(KEY_PUB, pubBase64)
            putString(KEY_PRIV, privBase64)
        }
    }

    fun getPrivateKeyBytes(): ByteArray? {
        val privBase64 = prefs.getString(KEY_PRIV, null) ?: return null
        return Base64.decode(privBase64, Base64.DEFAULT)
    }
}
