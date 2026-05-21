package chat.cabal.protocol

import java.nio.ByteBuffer

/**
 * Variable-length integer encoding (varint) as used in the Cable protocol.
 * This implementation follows the unsigned varint specification (MSB).
 */
object Varint {
    fun encode(value: Long): ByteArray {
        var v = value
        val out = mutableListOf<Byte>()
        while (v >= 0x80) {
            out.add(((v and 0x7F) or 0x80).toByte())
            v = v ushr 7
        }
        out.add(v.toByte())
        return out.toByteArray()
    }

    fun decode(buffer: ByteBuffer): Long {
        var result = 0L
        var shift = 0
        while (true) {
            val b = buffer.get().toInt()
            result = result or ((b and 0x7F).toLong() shl shift)
            if (b and 0x80 == 0) break
            shift += 7
            if (shift > 63) throw IllegalArgumentException("Varint too long")
        }
        return result
    }

    fun size(value: Long): Int {
        var v = value
        var count = 0
        while (v >= 0x80) {
            v = v ushr 7
            count++
        }
        return count + 1
    }
}
