package chat.cabal.protocol

import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.crypto.util.PublicKeyFactory
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.*
import javax.crypto.KeyAgreement

object Crypto {
    init {
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider())
    }

    private val secureRandom = SecureRandom()

    fun generateKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("Ed25519")
        return kpg.generateKeyPair()
    }

    fun sign(payload: ByteArray, privateKey: PrivateKey): ByteArray {
        val privKeyParams = PrivateKeyFactory.createKey(privateKey.encoded) as Ed25519PrivateKeyParameters
        val signer = Ed25519Signer()
        signer.init(true, privKeyParams)
        signer.update(payload, 0, payload.size)
        return signer.generateSignature()
    }

    @Suppress("unused")
    fun verify(payload: ByteArray, signature: ByteArray, publicKey: PublicKey): Boolean {
        val pubKeyParams = PublicKeyFactory.createKey(publicKey.encoded) as Ed25519PublicKeyParameters
        val signer = Ed25519Signer()
        signer.init(false, pubKeyParams)
        signer.update(payload, 0, payload.size)
        return signer.verifySignature(signature)
    }

    fun blake2b(data: ByteArray): ByteArray {
        val digest = Blake2bDigest(256) // Cable uses 32-byte (256-bit) hashes
        digest.update(data, 0, data.size)
        val out = ByteArray(digest.digestSize)
        digest.doFinal(out, 0)
        return out
    }
    
    fun randomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return bytes
    }

    /**
     * Performs a Diffie-Hellman key exchange using X25519.
     */
    @Suppress("unused")
    fun diffieHellman(privateKey: PrivateKey, publicKey: PublicKey): ByteArray {
        val ka = KeyAgreement.getInstance("X25519")
        ka.init(privateKey)
        ka.doPhase(publicKey, true)
        return ka.generateSecret()
    }

    /**
     * Encrypts data using ChaCha20-Poly1305.
     */
    fun encrypt(key: ByteArray, nonce: ByteArray, plaintext: ByteArray): ByteArray {
        val cipher = ChaCha20Poly1305()
        cipher.init(true, ParametersWithIV(KeyParameter(key), nonce))
        val ciphertext = ByteArray(plaintext.size + 16) // 16 bytes for Poly1305 tag
        val len = cipher.processBytes(plaintext, 0, plaintext.size, ciphertext, 0)
        cipher.doFinal(ciphertext, len)
        return ciphertext
    }

    /**
     * Decrypts data using ChaCha20-Poly1305.
     */
    fun decrypt(key: ByteArray, nonce: ByteArray, ciphertext: ByteArray): ByteArray {
        val cipher = ChaCha20Poly1305()
        cipher.init(false, ParametersWithIV(KeyParameter(key), nonce))
        val plaintext = ByteArray(ciphertext.size - 16)
        val len = cipher.processBytes(ciphertext, 0, ciphertext.size, plaintext, 0)
        cipher.doFinal(plaintext, len)
        return plaintext
    }
}
