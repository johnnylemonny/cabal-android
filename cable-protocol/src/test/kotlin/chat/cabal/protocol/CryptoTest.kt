package chat.cabal.protocol

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoTest {

    @Test
    fun testBlake2bHashing() {
        val input = "cabal-p2p-test".toByteArray(Charsets.UTF_8)
        val hash1 = Crypto.blake2b(input)
        val hash2 = Crypto.blake2b(input)

        assertEquals(32, hash1.size)
        assertArrayEquals(hash1, hash2)

        val hashOther = Crypto.blake2b("other".toByteArray(Charsets.UTF_8))
        assertFalse(hash1.contentEquals(hashOther))
    }

    @Test
    fun testEd25519SigningAndVerification() {
        val keyPair = Crypto.generateKeyPair()
        assertNotNull(keyPair.public)
        assertNotNull(keyPair.private)

        val message = "Decentralized Cabal Message".toByteArray(Charsets.UTF_8)
        val signature = Crypto.sign(message, keyPair.private)

        assertEquals(64, signature.size)

        val isValid = Crypto.verify(message, signature, keyPair.public)
        assertTrue("Signature should be valid", isValid)

        val tamperedMessage = "Tampered Message".toByteArray(Charsets.UTF_8)
        val isInvalid = Crypto.verify(tamperedMessage, signature, keyPair.public)
        assertFalse("Tampered message signature should be invalid", isInvalid)

        val otherKeyPair = Crypto.generateKeyPair()
        val isWrongKey = Crypto.verify(message, signature, otherKeyPair.public)
        assertFalse("Signature verified with wrong key should fail", isWrongKey)
    }

    @Test
    fun testChaCha20Poly1305EncryptionAndDecryption() {
        val key = Crypto.randomBytes(32)
        val nonce = Crypto.randomBytes(12)
        val plaintext = "Top secret P2P payload with UTF-8 characters: ąćęłńóśźż".toByteArray(Charsets.UTF_8)

        val ciphertext = Crypto.encrypt(key, nonce, plaintext)
        assertEquals(plaintext.size + 16, ciphertext.size)
        assertFalse(plaintext.contentEquals(ciphertext))

        val decrypted = Crypto.decrypt(key, nonce, ciphertext)
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun testRandomBytes() {
        val b1 = Crypto.randomBytes(16)
        val b2 = Crypto.randomBytes(16)

        assertEquals(16, b1.size)
        assertEquals(16, b2.size)
        assertFalse(b1.contentEquals(b2))
    }
}
