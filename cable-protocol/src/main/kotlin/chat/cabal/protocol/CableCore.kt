package chat.cabal.protocol

import java.security.PrivateKey

class CableCore(
    val publicKey: ByteArray,
    private val privateKey: PrivateKey,
    private val cabalSecret: ByteArray // 32-byte key for the group
) {
    fun createTextPost(channel: String, text: String, links: List<ByteArray> = emptyList()): TextPost {
        // E2EE: Encrypt text before sending
        val nonce = Crypto.randomBytes(12)
        val encryptedBytes = Crypto.encrypt(cabalSecret, nonce, text.toByteArray(Charsets.UTF_8))
        
        // Combine nonce + encrypted data and encode to Base64 to fit in 'text' field
        val combined = ByteArray(nonce.size + encryptedBytes.size)
        System.arraycopy(nonce, 0, combined, 0, nonce.size)
        System.arraycopy(encryptedBytes, 0, combined, nonce.size, encryptedBytes.size)
        
        val encryptedText = "E2E:" + android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)

        val post = TextPost(
            publicKey = publicKey,
            links = links,
            channel = channel,
            timestamp = System.currentTimeMillis() / 1000,
            text = encryptedText
        )
        post.signature = Crypto.sign(post.serializePayload(), privateKey)
        return post
    }

    fun decryptText(encryptedText: String): String {
        if (!encryptedText.startsWith("E2E:")) return encryptedText
        
        return try {
            val combined = android.util.Base64.decode(encryptedText.removePrefix("E2E:"), android.util.Base64.DEFAULT)
            val nonce = combined.copyOfRange(0, 12)
            val encryptedBytes = combined.copyOfRange(12, combined.size)
            val decryptedBytes = Crypto.decrypt(cabalSecret, nonce, encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            "[Decryption Error]"
        }
    }
}

