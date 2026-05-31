package chat.cabal.mobile.core

import android.content.Context
import android.util.Base64
import android.util.Log
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class KeyStoreManager(private val context: Context) {
    private val TAG = "KeyStoreManager"
    private val PREFS_NAME = "cabal_identity"
    private val KEY_PUB = "pub"
    private val KEY_PRIV = "priv"

    fun getOrCreateKeyPair(): KeyPair {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val pubBase64 = prefs.getString(KEY_PUB, null)
        val privBase64 = prefs.getString(KEY_PRIV, null)

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
        
        prefs.edit()
            .putString(KEY_PUB, Base64.encodeToString(kp.public.encoded, Base64.DEFAULT))
            .putString(KEY_PRIV, Base64.encodeToString(kp.private.encoded, Base64.DEFAULT))
            .apply()
            
        Log.i(TAG, "New Ed25519 identity generated and saved")
        return kp
    }
}
