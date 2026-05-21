package chat.cabal.protocol

import java.nio.ByteBuffer

/**
 * Prepends the length of the data as a varint.
 */
fun prependLength(data: ByteArray): ByteArray {
    val len = Varint.encode(data.size.toLong())
    val result = ByteArray(len.size + data.size)
    System.arraycopy(len, 0, result, 0, len.size)
    System.arraycopy(data, 0, result, len.size, data.size)
    return result
}

sealed class CableMessage {
    abstract fun serialize(): ByteArray
}

data class PostRequest(
    val reqId: ByteArray,
    val ttl: Int,
    val hashes: List<ByteArray>
) : CableMessage() {
    override fun serialize(): ByteArray {
        val hashCount = hashes.size.toLong()
        val size = Varint.size(Constants.POST_REQUEST.toLong()) +
                Constants.CIRCUITID_SIZE +
                Constants.REQID_SIZE +
                Varint.size(ttl.toLong()) +
                Varint.size(hashCount) +
                (hashes.size * Constants.HASH_SIZE)
        
        val buffer = ByteBuffer.allocate(size)
        buffer.put(Varint.encode(Constants.POST_REQUEST.toLong()))
        buffer.put(ByteArray(Constants.CIRCUITID_SIZE)) // Circuit ID (unused)
        buffer.put(reqId)
        buffer.put(Varint.encode(ttl.toLong()))
        buffer.put(Varint.encode(hashCount))
        hashes.forEach { buffer.put(it) }
        
        return prependLength(buffer.array())
    }
}

data class HashResponse(
    val reqId: ByteArray,
    val hashes: List<ByteArray>
) : CableMessage() {
    override fun serialize(): ByteArray {
        val hashCount = hashes.size.toLong()
        val size = Varint.size(Constants.HASH_RESPONSE.toLong()) +
                Constants.CIRCUITID_SIZE +
                Constants.REQID_SIZE +
                Varint.size(hashCount) +
                (hashes.size * Constants.HASH_SIZE)
        
        val buffer = ByteBuffer.allocate(size)
        buffer.put(Varint.encode(Constants.HASH_RESPONSE.toLong()))
        buffer.put(ByteArray(Constants.CIRCUITID_SIZE))
        buffer.put(reqId)
        buffer.put(Varint.encode(hashCount))
        hashes.forEach { buffer.put(it) }
        
        return prependLength(buffer.array())
    }
}

data class PostResponse(
    val reqId: ByteArray,
    val posts: List<ByteArray>
) : CableMessage() {
    override fun serialize(): ByteArray {
        var size = Varint.size(Constants.POST_RESPONSE.toLong()) +
                Constants.CIRCUITID_SIZE +
                Constants.REQID_SIZE
        
        posts.forEach { size += Varint.size(it.size.toLong()) + it.size }
        size += Varint.size(0L) // Terminating 0
        
        val buffer = ByteBuffer.allocate(size)
        buffer.put(Varint.encode(Constants.POST_RESPONSE.toLong()))
        buffer.put(ByteArray(Constants.CIRCUITID_SIZE))
        buffer.put(reqId)
        posts.forEach {
            buffer.put(Varint.encode(it.size.toLong()))
            buffer.put(it)
        }
        buffer.put(Varint.encode(0L))
        
        return prependLength(buffer.array())
    }
}

data class TimeRangeRequest(
    val reqId: ByteArray,
    val ttl: Int,
    val channel: String,
    val timeStart: Long,
    val timeEnd: Long,
    val limit: Int
) : CableMessage() {
    override fun serialize(): ByteArray {
        val channelBytes = channel.toByteArray(Charsets.UTF_8)
        val size = Varint.size(Constants.TIME_RANGE_REQUEST.toLong()) +
                Constants.CIRCUITID_SIZE +
                Constants.REQID_SIZE +
                Varint.size(ttl.toLong()) +
                Varint.size(channelBytes.size.toLong()) +
                channelBytes.size +
                Varint.size(timeStart) +
                Varint.size(timeEnd) +
                Varint.size(limit.toLong())
        
        val buffer = ByteBuffer.allocate(size)
        buffer.put(Varint.encode(Constants.TIME_RANGE_REQUEST.toLong()))
        buffer.put(ByteArray(Constants.CIRCUITID_SIZE))
        buffer.put(reqId)
        buffer.put(Varint.encode(ttl.toLong()))
        buffer.put(Varint.encode(channelBytes.size.toLong()))
        buffer.put(channelBytes)
        buffer.put(Varint.encode(timeStart))
        buffer.put(Varint.encode(timeEnd))
        buffer.put(Varint.encode(limit.toLong()))
        
        return prependLength(buffer.array())
    }
}

data class ChannelStateRequest(
    val reqId: ByteArray,
    val ttl: Int,
    val channel: String,
    val future: Int
) : CableMessage() {
    override fun serialize(): ByteArray {
        val channelBytes = channel.toByteArray(Charsets.UTF_8)
        val size = Varint.size(Constants.CHANNEL_STATE_REQUEST.toLong()) +
                Constants.CIRCUITID_SIZE +
                Constants.REQID_SIZE +
                Varint.size(ttl.toLong()) +
                Varint.size(channelBytes.size.toLong()) +
                channelBytes.size +
                Varint.size(future.toLong())
        
        val buffer = ByteBuffer.allocate(size)
        buffer.put(Varint.encode(Constants.CHANNEL_STATE_REQUEST.toLong()))
        buffer.put(ByteArray(Constants.CIRCUITID_SIZE))
        buffer.put(reqId)
        buffer.put(Varint.encode(ttl.toLong()))
        buffer.put(Varint.encode(channelBytes.size.toLong()))
        buffer.put(channelBytes)
        buffer.put(Varint.encode(future.toLong()))
        
        return prependLength(buffer.array())
    }
}

data class ChannelListRequest(
    val reqId: ByteArray,
    val ttl: Int,
    val offset: Int,
    val limit: Int
) : CableMessage() {
    override fun serialize(): ByteArray {
        val size = Varint.size(Constants.CHANNEL_LIST_REQUEST.toLong()) +
                Constants.CIRCUITID_SIZE +
                Constants.REQID_SIZE +
                Varint.size(ttl.toLong()) +
                Varint.size(offset.toLong()) +
                Varint.size(limit.toLong())
        
        val buffer = ByteBuffer.allocate(size)
        buffer.put(Varint.encode(Constants.CHANNEL_LIST_REQUEST.toLong()))
        buffer.put(ByteArray(Constants.CIRCUITID_SIZE))
        buffer.put(reqId)
        buffer.put(Varint.encode(ttl.toLong()))
        buffer.put(Varint.encode(offset.toLong()))
        buffer.put(Varint.encode(limit.toLong()))
        
        return prependLength(buffer.array())
    }
}

sealed class CablePost {
    abstract val publicKey: ByteArray
    abstract var signature: ByteArray?
    abstract val links: List<ByteArray>
    abstract val timestamp: Long

    abstract fun serializePayload(): ByteArray
    
    fun serialize(): ByteArray {
        val payload = serializePayload()
        val size = Constants.PUBLICKEY_SIZE + Constants.SIGNATURE_SIZE + payload.size
        val buffer = ByteBuffer.allocate(size)
        buffer.put(publicKey)
        if (signature != null) {
            buffer.put(signature)
        } else {
            buffer.put(ByteArray(Constants.SIGNATURE_SIZE))
        }
        buffer.put(payload)
        return buffer.array()
    }

    fun hash(): ByteArray {
        return Crypto.blake2b(serialize())
    }
}

data class TextPost(
    override val publicKey: ByteArray,
    override var signature: ByteArray? = null,
    override val links: List<ByteArray>,
    val channel: String,
    override val timestamp: Long,
    val text: String
) : CablePost() {
    override fun serializePayload(): ByteArray {
        val channelBytes = channel.toByteArray(Charsets.UTF_8)
        val textBytes = text.toByteArray(Charsets.UTF_8)
        val linksCount = links.size.toLong()
        
        val size = Varint.size(linksCount) +
                (links.size * Constants.HASH_SIZE) +
                Varint.size(Constants.TEXT_POST.toLong()) +
                Varint.size(timestamp) +
                Varint.size(channelBytes.size.toLong()) +
                channelBytes.size +
                Varint.size(textBytes.size.toLong()) +
                textBytes.size
        
        val buffer = ByteBuffer.allocate(size)
        buffer.put(Varint.encode(linksCount))
        links.forEach { buffer.put(it) }
        buffer.put(Varint.encode(Constants.TEXT_POST.toLong()))
        buffer.put(Varint.encode(timestamp))
        buffer.put(Varint.encode(channelBytes.size.toLong()))
        buffer.put(channelBytes)
        buffer.put(Varint.encode(textBytes.size.toLong()))
        buffer.put(textBytes)
        
        return buffer.array()
    }
}

data class InfoPost(
    override val publicKey: ByteArray,
    override var signature: ByteArray? = null,
    override val links: List<ByteArray>,
    override val timestamp: Long,
    val info: Map<String, String>
) : CablePost() {
    override fun serializePayload(): ByteArray {
        val linksCount = links.size.toLong()
        var size = Varint.size(linksCount) +
                (links.size * Constants.HASH_SIZE) +
                Varint.size(Constants.INFO_POST.toLong()) +
                Varint.size(timestamp)
        
        val pairs = info.toList().map { (k, v) ->
            val kBytes = k.toByteArray(Charsets.UTF_8)
            val vBytes = v.toByteArray(Charsets.UTF_8)
            size += Varint.size(kBytes.size.toLong()) + kBytes.size +
                    Varint.size(vBytes.size.toLong()) + vBytes.size
            kBytes to vBytes
        }
        size += Varint.size(0L) // Final keyN_len = 0
        
        val buffer = ByteBuffer.allocate(size)
        buffer.put(Varint.encode(linksCount))
        links.forEach { buffer.put(it) }
        buffer.put(Varint.encode(Constants.INFO_POST.toLong()))
        buffer.put(Varint.encode(timestamp))
        
        pairs.forEach { (k, v) ->
            buffer.put(Varint.encode(k.size.toLong()))
            buffer.put(k)
            buffer.put(Varint.encode(v.size.toLong()))
            buffer.put(v)
        }
        buffer.put(Varint.encode(0L))
        
        return buffer.array()
    }
}

data class JoinPost(
    override val publicKey: ByteArray,
    override var signature: ByteArray? = null,
    override val links: List<ByteArray>,
    val channel: String,
    override val timestamp: Long
) : CablePost() {
    override fun serializePayload(): ByteArray {
        val channelBytes = channel.toByteArray(Charsets.UTF_8)
        val linksCount = links.size.toLong()
        val size = Varint.size(linksCount) +
                (links.size * Constants.HASH_SIZE) +
                Varint.size(Constants.JOIN_POST.toLong()) +
                Varint.size(timestamp) +
                Varint.size(channelBytes.size.toLong()) +
                channelBytes.size
        
        val buffer = ByteBuffer.allocate(size)
        buffer.put(Varint.encode(linksCount))
        links.forEach { buffer.put(it) }
        buffer.put(Varint.encode(Constants.JOIN_POST.toLong()))
        buffer.put(Varint.encode(timestamp))
        buffer.put(Varint.encode(channelBytes.size.toLong()))
        buffer.put(channelBytes)
        
        return buffer.array()
    }
}

data class LeavePost(
    override val publicKey: ByteArray,
    override var signature: ByteArray? = null,
    override val links: List<ByteArray>,
    val channel: String,
    override val timestamp: Long
) : CablePost() {
    override fun serializePayload(): ByteArray {
        val channelBytes = channel.toByteArray(Charsets.UTF_8)
        val linksCount = links.size.toLong()
        val size = Varint.size(linksCount) +
                (links.size * Constants.HASH_SIZE) +
                Varint.size(Constants.LEAVE_POST.toLong()) +
                Varint.size(timestamp) +
                Varint.size(channelBytes.size.toLong()) +
                channelBytes.size
        
        val buffer = ByteBuffer.allocate(size)
        buffer.put(Varint.encode(linksCount))
        links.forEach { buffer.put(it) }
        buffer.put(Varint.encode(Constants.LEAVE_POST.toLong()))
        buffer.put(Varint.encode(timestamp))
        buffer.put(Varint.encode(channelBytes.size.toLong()))
        buffer.put(channelBytes)
        
        return buffer.array()
    }
}

data class TopicPost(
    override val publicKey: ByteArray,
    override var signature: ByteArray? = null,
    override val links: List<ByteArray>,
    val channel: String,
    override val timestamp: Long,
    val topic: String
) : CablePost() {
    override fun serializePayload(): ByteArray {
        val channelBytes = channel.toByteArray(Charsets.UTF_8)
        val topicBytes = topic.toByteArray(Charsets.UTF_8)
        val linksCount = links.size.toLong()
        val size = Varint.size(linksCount) +
                (links.size * Constants.HASH_SIZE) +
                Varint.size(Constants.TOPIC_POST.toLong()) +
                Varint.size(timestamp) +
                Varint.size(channelBytes.size.toLong()) +
                channelBytes.size +
                Varint.size(topicBytes.size.toLong()) +
                topicBytes.size
        
        val buffer = ByteBuffer.allocate(size)
        buffer.put(Varint.encode(linksCount))
        links.forEach { buffer.put(it) }
        buffer.put(Varint.encode(Constants.TOPIC_POST.toLong()))
        buffer.put(Varint.encode(timestamp))
        buffer.put(Varint.encode(channelBytes.size.toLong()))
        buffer.put(channelBytes)
        buffer.put(Varint.encode(topicBytes.size.toLong()))
        buffer.put(topicBytes)
        
        return buffer.array()
    }
}

object CableParser {
    fun parsePost(data: ByteArray): CablePost {
        val buffer = ByteBuffer.wrap(data)
        val publicKey = ByteArray(Constants.PUBLICKEY_SIZE)
        buffer.get(publicKey)
        val signature = ByteArray(Constants.SIGNATURE_SIZE)
        buffer.get(signature)
        
        val numLinks = Varint.decode(buffer).toInt()
        val links = mutableListOf<ByteArray>()
        for (i in 0 until numLinks) {
            val link = ByteArray(Constants.HASH_SIZE)
            buffer.get(link)
            links.add(link)
        }
        
        val postType = Varint.decode(buffer).toInt()
        val timestamp = Varint.decode(buffer)
        
        return when (postType) {
            Constants.TEXT_POST -> {
                val channelLen = Varint.decode(buffer).toInt()
                val channelBytes = ByteArray(channelLen)
                buffer.get(channelBytes)
                val channel = String(channelBytes, Charsets.UTF_8)
                
                val textLen = Varint.decode(buffer).toInt()
                val textBytes = ByteArray(textLen)
                buffer.get(textBytes)
                val text = String(textBytes, Charsets.UTF_8)
                
                TextPost(publicKey, signature, links, channel, timestamp, text)
            }
            Constants.INFO_POST -> {
                val info = mutableMapOf<String, String>()
                while (true) {
                    val keyLen = Varint.decode(buffer).toInt()
                    if (keyLen == 0) break
                    val keyBytes = ByteArray(keyLen)
                    buffer.get(keyBytes)
                    val key = String(keyBytes, Charsets.UTF_8)
                    
                    val valLen = Varint.decode(buffer).toInt()
                    val valBytes = ByteArray(valLen)
                    buffer.get(valBytes)
                    val value = String(valBytes, Charsets.UTF_8)
                    info[key] = value
                }
                InfoPost(publicKey, signature, links, timestamp, info)
            }
            Constants.JOIN_POST -> {
                val channelLen = Varint.decode(buffer).toInt()
                val channelBytes = ByteArray(channelLen)
                buffer.get(channelBytes)
                val channel = String(channelBytes, Charsets.UTF_8)
                JoinPost(publicKey, signature, links, channel, timestamp)
            }
            Constants.LEAVE_POST -> {
                val channelLen = Varint.decode(buffer).toInt()
                val channelBytes = ByteArray(channelLen)
                buffer.get(channelBytes)
                val channel = String(channelBytes, Charsets.UTF_8)
                LeavePost(publicKey, signature, links, channel, timestamp)
            }
            Constants.TOPIC_POST -> {
                val channelLen = Varint.decode(buffer).toInt()
                val channelBytes = ByteArray(channelLen)
                buffer.get(channelBytes)
                val channel = String(channelBytes, Charsets.UTF_8)
                
                val topicLen = Varint.decode(buffer).toInt()
                val topicBytes = ByteArray(topicLen)
                buffer.get(topicBytes)
                val topic = String(topicBytes, Charsets.UTF_8)
                
                TopicPost(publicKey, signature, links, channel, timestamp, topic)
            }
            else -> throw IllegalArgumentException("Unknown post type: $postType")
        }
    }

    fun parseMessage(data: ByteArray): CableMessage {
        val buffer = ByteBuffer.wrap(data)
        val msgLen = Varint.decode(buffer)
        val msgType = Varint.decode(buffer).toInt()
        
        // Skip Circuit ID
        buffer.get(ByteArray(Constants.CIRCUITID_SIZE))
        
        val reqId = ByteArray(Constants.REQID_SIZE)
        buffer.get(reqId)
        
        return when (msgType) {
            Constants.POST_REQUEST -> {
                val ttl = Varint.decode(buffer).toInt()
                val hashCount = Varint.decode(buffer).toInt()
                val hashes = mutableListOf<ByteArray>()
                for (i in 0 until hashCount) {
                    val h = ByteArray(Constants.HASH_SIZE)
                    buffer.get(h)
                    hashes.add(h)
                }
                PostRequest(reqId, ttl, hashes)
            }
            Constants.HASH_RESPONSE -> {
                val hashCount = Varint.decode(buffer).toInt()
                val hashes = mutableListOf<ByteArray>()
                for (i in 0 until hashCount) {
                    val h = ByteArray(Constants.HASH_SIZE)
                    buffer.get(h)
                    hashes.add(h)
                }
                HashResponse(reqId, hashes)
            }
            Constants.POST_RESPONSE -> {
                val posts = mutableListOf<ByteArray>()
                while (true) {
                    val postLen = Varint.decode(buffer).toInt()
                    if (postLen == 0) break
                    val p = ByteArray(postLen)
                    buffer.get(p)
                    posts.add(p)
                }
                PostResponse(reqId, posts)
            }
            Constants.CHANNEL_LIST_REQUEST -> {
                val ttl = Varint.decode(buffer).toInt()
                val offset = Varint.decode(buffer).toInt()
                val limit = Varint.decode(buffer).toInt()
                ChannelListRequest(reqId, ttl, offset, limit)
            }
            else -> throw IllegalArgumentException("Unknown message type: $msgType")
        }
    }
}
