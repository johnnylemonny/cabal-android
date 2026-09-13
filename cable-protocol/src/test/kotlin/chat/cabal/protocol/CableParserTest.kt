package chat.cabal.protocol

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CableParserTest {

    @Test
    fun testPostRequestMessageRoundtrip() {
        val reqId = Crypto.randomBytes(Constants.REQID_SIZE)
        val hashes = listOf(Crypto.randomBytes(Constants.HASH_SIZE), Crypto.randomBytes(Constants.HASH_SIZE))
        val message = PostRequest(reqId = reqId, ttl = 3, hashes = hashes)

        val serialized = message.serialize()
        val parsed = CableParser.parseMessage(serialized)

        assertTrue(parsed is PostRequest)
        val parsedReq = parsed as PostRequest
        assertArrayEquals(reqId, parsedReq.reqId)
        assertEquals(3, parsedReq.ttl)
        assertEquals(2, parsedReq.hashes.size)
        assertArrayEquals(hashes[0], parsedReq.hashes[0])
        assertArrayEquals(hashes[1], parsedReq.hashes[1])
    }

    @Test
    fun testHashResponseMessageRoundtrip() {
        val reqId = Crypto.randomBytes(Constants.REQID_SIZE)
        val hashes = listOf(Crypto.randomBytes(Constants.HASH_SIZE))
        val message = HashResponse(reqId = reqId, hashes = hashes)

        val serialized = message.serialize()
        val parsed = CableParser.parseMessage(serialized)

        assertTrue(parsed is HashResponse)
        val parsedHashResp = parsed as HashResponse
        assertArrayEquals(reqId, parsedHashResp.reqId)
        assertEquals(1, parsedHashResp.hashes.size)
        assertArrayEquals(hashes[0], parsedHashResp.hashes[0])
    }

    @Test
    fun testPostResponseMessageRoundtrip() {
        val reqId = Crypto.randomBytes(Constants.REQID_SIZE)
        val samplePost = "sample-post-data-bytes".toByteArray(Charsets.UTF_8)
        val message = PostResponse(reqId = reqId, posts = listOf(samplePost))

        val serialized = message.serialize()
        val parsed = CableParser.parseMessage(serialized)

        assertTrue(parsed is PostResponse)
        val parsedPostResp = parsed as PostResponse
        assertArrayEquals(reqId, parsedPostResp.reqId)
        assertEquals(1, parsedPostResp.posts.size)
        assertArrayEquals(samplePost, parsedPostResp.posts[0])
    }

    @Test
    fun testTimeRangeRequestRoundtrip() {
        val reqId = Crypto.randomBytes(Constants.REQID_SIZE)
        val message = TimeRangeRequest(
            reqId = reqId,
            ttl = 5,
            channel = "general",
            timeStart = 1000L,
            timeEnd = 2000L,
            limit = 50
        )

        val serialized = message.serialize()
        val parsed = CableParser.parseMessage(serialized)

        assertTrue(parsed is TimeRangeRequest)
        val parsedReq = parsed as TimeRangeRequest
        assertArrayEquals(reqId, parsedReq.reqId)
        assertEquals(5, parsedReq.ttl)
        assertEquals("general", parsedReq.channel)
        assertEquals(1000L, parsedReq.timeStart)
        assertEquals(2000L, parsedReq.timeEnd)
        assertEquals(50, parsedReq.limit)
    }
}
