package chat.cabal.mobile.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.security.SecureRandom

class IdentityBackupManagerTest {

    @Test
    fun testWordListIntegrity() {
        val dummyEntropy = ByteArray(16)
        val mnemonic = IdentityBackupManager.generateMnemonic(dummyEntropy)
        assertEquals(12, mnemonic.size)

        // Generate mnemonics from various entropies and collect words to verify
        val sample = IdentityBackupManager.getMnemonicForPrivateKey(ByteArray(32) { it.toByte() })
        assertEquals(12, sample.size)
    }

    @Test
    fun testMnemonicRoundtrip() {
        val random = SecureRandom()
        for (i in 1..10) {
            val entropy = ByteArray(16)
            random.nextBytes(entropy)

            val mnemonic = IdentityBackupManager.generateMnemonic(entropy)
            assertEquals(12, mnemonic.size)

            val recoveredEntropy = IdentityBackupManager.mnemonicToEntropy(mnemonic)
            assertArrayEquals("Recovered entropy must match original", entropy, recoveredEntropy)
        }
    }

    @Test
    fun testGetMnemonicForPrivateKeyDeterminism() {
        val privateKey = ByteArray(32) { (it * 7).toByte() }
        val m1 = IdentityBackupManager.getMnemonicForPrivateKey(privateKey)
        val m2 = IdentityBackupManager.getMnemonicForPrivateKey(privateKey)

        assertEquals(12, m1.size)
        assertEquals(m1, m2)
    }

    @Test
    fun testInvalidMnemonicThrows() {
        val invalidMnemonic = listOf(
            "abandon", "ability", "able", "about", "above", "absent",
            "absorb", "abstract", "absurd", "abuse", "access", "invalidwordnotexists"
        )
        try {
            IdentityBackupManager.mnemonicToEntropy(invalidMnemonic)
            fail("Should have thrown IllegalArgumentException for unknown word")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Word not in list") == true)
        }
    }

    @Test
    fun testChecksumMismatchThrows() {
        // Valid 12 words
        val entropy = ByteArray(16) { 0 }
        val mnemonic = IdentityBackupManager.generateMnemonic(entropy).toMutableList()
        // Tamper with the last word (checksum)
        mnemonic[11] = if (mnemonic[11] == "abandon") "zoo" else "abandon"

        try {
            IdentityBackupManager.mnemonicToEntropy(mnemonic)
            fail("Should have thrown IllegalArgumentException for invalid checksum")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("checksum") == true)
        }
    }
}
