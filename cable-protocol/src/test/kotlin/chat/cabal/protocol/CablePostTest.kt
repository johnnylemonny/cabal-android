package chat.cabal.protocol

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CablePostTest {

    private val keyPair = Crypto.generateKeyPair()
    private val publicKeyBytes = (keyPair.public.encoded ?: ByteArray(32)).takeLast(32).toByteArray()
    private val cabalSecret = Crypto.blake2b("test-cabal".toByteArray(Charsets.UTF_8))
    private val cableCore = CableCore(publicKeyBytes, keyPair.private, cabalSecret)

    @Test
    fun testTextPostCreationAndSerialization() {
        val channel = "general"
        val text = "Hello P2P world!"
        val links = listOf(Crypto.blake2b("parent-message".toByteArray(Charsets.UTF_8)))

        val post = cableCore.createTextPost(channel, text, links)
        assertEquals(channel, post.channel)
        assertTrue(post.text.startsWith("E2E:"))
        assertEquals(text, cableCore.decryptText(post.text))
        assertEquals(1, post.links.size)
        assertArrayEquals(links[0], post.links[0])
        assertEquals(Constants.PUBLICKEY_SIZE, post.publicKey.size)
        assertEquals(Constants.SIGNATURE_SIZE, post.signature?.size)

        val hash = post.hash()
        assertEquals(Constants.HASH_SIZE, hash.size)

        val serialized = post.serialize()
        assertTrue(serialized.isNotEmpty())

        val parsed = CableParser.parsePost(serialized)
        assertTrue(parsed is TextPost)
        val parsedTextPost = parsed as TextPost
        assertEquals(post.channel, parsedTextPost.channel)
        assertEquals(post.text, parsedTextPost.text)
        assertEquals(text, cableCore.decryptText(parsedTextPost.text))
        assertEquals(post.timestamp, parsedTextPost.timestamp)
        assertArrayEquals(post.publicKey, parsedTextPost.publicKey)
        assertArrayEquals(post.signature, parsedTextPost.signature)
    }

    @Test
    fun testInfoPostCreationAndParsing() {
        val info = mapOf("name" to "Alice", "status" to "Decentralized & Free")
        val post = cableCore.createInfoPost(info)

        val serialized = post.serialize()
        val parsed = CableParser.parsePost(serialized)

        assertTrue(parsed is InfoPost)
        val parsedInfoPost = parsed as InfoPost
        assertEquals("Alice", parsedInfoPost.info["name"])
        assertEquals("Decentralized & Free", parsedInfoPost.info["status"])
    }

    @Test
    fun testTopicPostCreationAndParsing() {
        val post = cableCore.createTopicPost("general", "Welcome to Cabal")
        val serialized = post.serialize()
        val parsed = CableParser.parsePost(serialized)

        assertTrue(parsed is TopicPost)
        val parsedTopic = parsed as TopicPost
        assertEquals("general", parsedTopic.channel)
        assertEquals("Welcome to Cabal", parsedTopic.topic)
    }

    @Test
    fun testDeletePostCreationAndParsing() {
        val targets = listOf(Crypto.randomBytes(32), Crypto.randomBytes(32))
        val post = cableCore.createDeletePost(targets)
        val serialized = post.serialize()
        val parsed = CableParser.parsePost(serialized)

        assertTrue(parsed is DeletePost)
        val parsedDelete = parsed as DeletePost
        assertEquals(2, parsedDelete.links.size)
    }
}
