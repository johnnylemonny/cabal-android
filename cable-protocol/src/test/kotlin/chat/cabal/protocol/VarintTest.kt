package chat.cabal.protocol

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer

class VarintTest {

    @Test
    fun testSingleByteVarint() {
        val values = listOf(0L, 1L, 42L, 127L)
        for (v in values) {
            val encoded = Varint.encode(v)
            assertEquals(1, encoded.size)
            assertEquals(1, Varint.size(v))

            val decoded = Varint.decode(ByteBuffer.wrap(encoded))
            assertEquals(v, decoded)
        }
    }

    @Test
    fun testMultiByteVarint() {
        val testCases = listOf(
            128L,
            255L,
            300L,
            16383L,
            16384L,
            65535L,
            1048576L,
            Int.MAX_VALUE.toLong(),
            Long.MAX_VALUE
        )

        for (v in testCases) {
            val encoded = Varint.encode(v)
            assertEquals(Varint.size(v), encoded.size)

            val decoded = Varint.decode(ByteBuffer.wrap(encoded))
            assertEquals("Mismatch for value: $v", v, decoded)
        }
    }

    @Test
    fun testSequentialDecoding() {
        val values = listOf(5L, 300L, 0L, 99999L)
        val combined = values.flatMap { Varint.encode(it).toList() }.toByteArray()
        val buffer = ByteBuffer.wrap(combined)

        for (expected in values) {
            val decoded = Varint.decode(buffer)
            assertEquals(expected, decoded)
        }
    }
}
