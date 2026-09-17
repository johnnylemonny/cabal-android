package chat.cabal.database

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.ByteArray
import kotlin.Long
import kotlin.String
import kotlin.collections.Collection

public class CabalQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> getAllCabals(mapper: (key: String, name: String) -> T): Query<T> = Query(69_562_146, arrayOf("cabal"), driver, "Cabal.sq", "getAllCabals", "SELECT cabal.key, cabal.name FROM cabal") { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!
    )
  }

  public fun getAllCabals(): Query<Cabal> = getAllCabals(::Cabal)

  public fun <T : Any> getMessagesByChannel(channel: String, mapper: (
    hash: ByteArray,
    publicKey: ByteArray,
    channel: String,
    timestamp: Long,
    text: String,
    rawPost: ByteArray,
    status: Long,
    parentHash: ByteArray?,
    isEdited: Long,
    isDeleted: Long,
    ttl: Long?,
  ) -> T): Query<T> = GetMessagesByChannelQuery(channel) { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getBytes(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getBytes(5)!!,
      cursor.getLong(6)!!,
      cursor.getBytes(7),
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)
    )
  }

  public fun getMessagesByChannel(channel: String): Query<Message> = getMessagesByChannel(channel, ::Message)

  public fun <T : Any> getMessagesByHashes(hash: Collection<ByteArray>, mapper: (
    hash: ByteArray,
    publicKey: ByteArray,
    channel: String,
    timestamp: Long,
    text: String,
    rawPost: ByteArray,
    status: Long,
    parentHash: ByteArray?,
    isEdited: Long,
    isDeleted: Long,
    ttl: Long?,
  ) -> T): Query<T> = GetMessagesByHashesQuery(hash) { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getBytes(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getBytes(5)!!,
      cursor.getLong(6)!!,
      cursor.getBytes(7),
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)
    )
  }

  public fun getMessagesByHashes(hash: Collection<ByteArray>): Query<Message> = getMessagesByHashes(hash, ::Message)

  public fun <T : Any> getMessagesInRange(
    channel: String,
    timeStart: Long,
    timeEnd: Long,
    limit: Long,
    mapper: (
      hash: ByteArray,
      publicKey: ByteArray,
      channel: String,
      timestamp: Long,
      text: String,
      rawPost: ByteArray,
      status: Long,
      parentHash: ByteArray?,
      isEdited: Long,
      isDeleted: Long,
      ttl: Long?,
    ) -> T,
  ): Query<T> = GetMessagesInRangeQuery(channel, timeStart, timeEnd, limit) { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getBytes(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getBytes(5)!!,
      cursor.getLong(6)!!,
      cursor.getBytes(7),
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)
    )
  }

  public fun getMessagesInRange(
    channel: String,
    timeStart: Long,
    timeEnd: Long,
    limit: Long,
  ): Query<Message> = getMessagesInRange(channel, timeStart, timeEnd, limit, ::Message)

  public fun <T : Any> getPeer(publicKey: ByteArray, mapper: (
    publicKey: ByteArray,
    name: String?,
    status: String?,
    lastSeen: Long,
    isIgnored: Long,
    isVerified: Long,
    role: Long,
  ) -> T): Query<T> = GetPeerQuery(publicKey) { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getString(1),
      cursor.getString(2),
      cursor.getLong(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getLong(6)!!
    )
  }

  public fun getPeer(publicKey: ByteArray): Query<Peer> = getPeer(publicKey, ::Peer)

  public fun <T : Any> getAllPeers(mapper: (
    publicKey: ByteArray,
    name: String?,
    status: String?,
    lastSeen: Long,
    isIgnored: Long,
    isVerified: Long,
    role: Long,
  ) -> T): Query<T> = Query(568_561_619, arrayOf("peer"), driver, "Cabal.sq", "getAllPeers", "SELECT peer.publicKey, peer.name, peer.status, peer.lastSeen, peer.isIgnored, peer.isVerified, peer.role FROM peer ORDER BY lastSeen DESC") { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getString(1),
      cursor.getString(2),
      cursor.getLong(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getLong(6)!!
    )
  }

  public fun getAllPeers(): Query<Peer> = getAllPeers(::Peer)

  public fun <T : Any> getAllChannels(mapper: (
    name: String,
    topic: String?,
    isJoined: Long,
  ) -> T): Query<T> = Query(30_730_254, arrayOf("channel"), driver, "Cabal.sq", "getAllChannels", "SELECT channel.name, channel.topic, channel.isJoined FROM channel ORDER BY name ASC") { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1),
      cursor.getLong(2)!!
    )
  }

  public fun getAllChannels(): Query<Channel> = getAllChannels(::Channel)

  public fun <T : Any> getJoinedChannels(mapper: (
    name: String,
    topic: String?,
    isJoined: Long,
  ) -> T): Query<T> = Query(-137_113_188, arrayOf("channel"), driver, "Cabal.sq", "getJoinedChannels", "SELECT channel.name, channel.topic, channel.isJoined FROM channel WHERE isJoined = 1") { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1),
      cursor.getLong(2)!!
    )
  }

  public fun getJoinedChannels(): Query<Channel> = getJoinedChannels(::Channel)

  public fun <T : Any> getLinkPreview(url: String, mapper: (
    url: String,
    title: String?,
    description: String?,
    image: ByteArray?,
    timestamp: Long,
  ) -> T): Query<T> = GetLinkPreviewQuery(url) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1),
      cursor.getString(2),
      cursor.getBytes(3),
      cursor.getLong(4)!!
    )
  }

  public fun getLinkPreview(url: String): Query<Link_preview> = getLinkPreview(url, ::Link_preview)

  /**
   * @return The number of rows updated.
   */
  public fun insertCabal(key: String, name: String): QueryResult<Long> {
    val result = driver.execute(872_081_379, """
        |INSERT OR IGNORE INTO cabal(key, name)
        |VALUES (?, ?)
        """.trimMargin(), 2) {
          var parameterIndex = 0
          bindString(parameterIndex++, key)
          bindString(parameterIndex++, name)
        }
    notifyQueries(872_081_379) { emit ->
      emit("cabal")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertMessage(
    hash: ByteArray,
    publicKey: ByteArray,
    channel: String,
    timestamp: Long,
    text: String,
    rawPost: ByteArray,
    status: Long,
    parentHash: ByteArray?,
    isEdited: Long,
    isDeleted: Long,
    ttl: Long?,
  ): QueryResult<Long> {
    val result = driver.execute(967_430_139, """
        |INSERT OR IGNORE INTO message(hash, publicKey, channel, timestamp, text, rawPost, status, parentHash, isEdited, isDeleted, ttl)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 11) {
          var parameterIndex = 0
          bindBytes(parameterIndex++, hash)
          bindBytes(parameterIndex++, publicKey)
          bindString(parameterIndex++, channel)
          bindLong(parameterIndex++, timestamp)
          bindString(parameterIndex++, text)
          bindBytes(parameterIndex++, rawPost)
          bindLong(parameterIndex++, status)
          bindBytes(parameterIndex++, parentHash)
          bindLong(parameterIndex++, isEdited)
          bindLong(parameterIndex++, isDeleted)
          bindLong(parameterIndex++, ttl)
        }
    notifyQueries(967_430_139) { emit ->
      emit("message")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updateMessageStatus(status: Long, hash: ByteArray): QueryResult<Long> {
    val result = driver.execute(-1_755_428_931, """UPDATE message SET status = ? WHERE hash = ?""", 2) {
          var parameterIndex = 0
          bindLong(parameterIndex++, status)
          bindBytes(parameterIndex++, hash)
        }
    notifyQueries(-1_755_428_931) { emit ->
      emit("message")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updateMessageContent(text: String, hash: ByteArray): QueryResult<Long> {
    val result = driver.execute(-30_034_418, """UPDATE message SET text = ?, isEdited = 1 WHERE hash = ?""", 2) {
          var parameterIndex = 0
          bindString(parameterIndex++, text)
          bindBytes(parameterIndex++, hash)
        }
    notifyQueries(-30_034_418) { emit ->
      emit("message")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun markMessageDeleted(hash: ByteArray): QueryResult<Long> {
    val result = driver.execute(1_989_912_946, """UPDATE message SET text = "[DELETED]", isDeleted = 1 WHERE hash = ?""", 1) {
          var parameterIndex = 0
          bindBytes(parameterIndex++, hash)
        }
    notifyQueries(1_989_912_946) { emit ->
      emit("message")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteExpiredMessages(ttl: Long?): QueryResult<Long> {
    val result = driver.execute(-960_598_445, """DELETE FROM message WHERE ttl > 0 AND ttl <= ?""", 1) {
          var parameterIndex = 0
          bindLong(parameterIndex++, ttl)
        }
    notifyQueries(-960_598_445) { emit ->
      emit("message")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertOrUpdatePeer(
    publicKey: ByteArray,
    name: String?,
    status: String?,
    lastSeen: Long,
    isIgnored: Long,
    isVerified: Long,
    role: Long,
  ): QueryResult<Long> {
    val result = driver.execute(-1_433_156_454, """
        |INSERT OR REPLACE INTO peer(publicKey, name, status, lastSeen, isIgnored, isVerified, role)
        |VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 7) {
          var parameterIndex = 0
          bindBytes(parameterIndex++, publicKey)
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, status)
          bindLong(parameterIndex++, lastSeen)
          bindLong(parameterIndex++, isIgnored)
          bindLong(parameterIndex++, isVerified)
          bindLong(parameterIndex++, role)
        }
    notifyQueries(-1_433_156_454) { emit ->
      emit("peer")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updatePeerIgnore(isIgnored: Long, publicKey: ByteArray): QueryResult<Long> {
    val result = driver.execute(-814_209_168, """UPDATE peer SET isIgnored = ? WHERE publicKey = ?""", 2) {
          var parameterIndex = 0
          bindLong(parameterIndex++, isIgnored)
          bindBytes(parameterIndex++, publicKey)
        }
    notifyQueries(-814_209_168) { emit ->
      emit("peer")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updatePeerVerification(isVerified: Long, publicKey: ByteArray): QueryResult<Long> {
    val result = driver.execute(-1_031_286_471, """UPDATE peer SET isVerified = ? WHERE publicKey = ?""", 2) {
          var parameterIndex = 0
          bindLong(parameterIndex++, isVerified)
          bindBytes(parameterIndex++, publicKey)
        }
    notifyQueries(-1_031_286_471) { emit ->
      emit("peer")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertChannel(
    name: String,
    topic: String?,
    isJoined: Long,
  ): QueryResult<Long> {
    val result = driver.execute(751_455_479, """
        |INSERT OR IGNORE INTO channel(name, topic, isJoined)
        |VALUES (?, ?, ?)
        """.trimMargin(), 3) {
          var parameterIndex = 0
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, topic)
          bindLong(parameterIndex++, isJoined)
        }
    notifyQueries(751_455_479) { emit ->
      emit("channel")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updateChannelTopic(topic: String?, name: String): QueryResult<Long> {
    val result = driver.execute(-1_621_823_352, """UPDATE channel SET topic = ? WHERE name = ?""", 2) {
          var parameterIndex = 0
          bindString(parameterIndex++, topic)
          bindString(parameterIndex++, name)
        }
    notifyQueries(-1_621_823_352) { emit ->
      emit("channel")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updateChannelJoinStatus(isJoined: Long, name: String): QueryResult<Long> {
    val result = driver.execute(-636_510_717, """UPDATE channel SET isJoined = ? WHERE name = ?""", 2) {
          var parameterIndex = 0
          bindLong(parameterIndex++, isJoined)
          bindString(parameterIndex++, name)
        }
    notifyQueries(-636_510_717) { emit ->
      emit("channel")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertLinkPreview(
    url: String,
    title: String?,
    description: String?,
    image: ByteArray?,
    timestamp: Long,
  ): QueryResult<Long> {
    val result = driver.execute(1_469_019_234, """
        |INSERT OR REPLACE INTO link_preview(url, title, description, image, timestamp)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          var parameterIndex = 0
          bindString(parameterIndex++, url)
          bindString(parameterIndex++, title)
          bindString(parameterIndex++, description)
          bindBytes(parameterIndex++, image)
          bindLong(parameterIndex++, timestamp)
        }
    notifyQueries(1_469_019_234) { emit ->
      emit("link_preview")
    }
    return result
  }

  private inner class GetMessagesByChannelQuery<out T : Any>(
    public val channel: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("message", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("message", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(1_200_775_805, """
    |SELECT message.hash, message.publicKey, message.channel, message.timestamp, message.text, message.rawPost, message.status, message.parentHash, message.isEdited, message.isDeleted, message.ttl FROM message
    |WHERE channel = ? AND isDeleted = 0
    |ORDER BY timestamp ASC
    """.trimMargin(), mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, channel)
    }

    override fun toString(): String = "Cabal.sq:getMessagesByChannel"
  }

  private inner class GetMessagesByHashesQuery<out T : Any>(
    public val hash: Collection<ByteArray>,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("message", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("message", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> {
      val hashIndexes = createArguments(count = hash.size)
      return driver.executeQuery(null, """
          |SELECT message.hash, message.publicKey, message.channel, message.timestamp, message.text, message.rawPost, message.status, message.parentHash, message.isEdited, message.isDeleted, message.ttl FROM message
          |WHERE hash IN $hashIndexes
          """.trimMargin(), mapper, hash.size) {
            var parameterIndex = 0
            hash.forEach { hash_ ->
              bindBytes(parameterIndex++, hash_)
            }
          }
    }

    override fun toString(): String = "Cabal.sq:getMessagesByHashes"
  }

  private inner class GetMessagesInRangeQuery<out T : Any>(
    public val channel: String,
    public val timeStart: Long,
    public val timeEnd: Long,
    public val limit: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("message", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("message", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(1_863_353_641, """
    |SELECT message.hash, message.publicKey, message.channel, message.timestamp, message.text, message.rawPost, message.status, message.parentHash, message.isEdited, message.isDeleted, message.ttl FROM message
    |WHERE channel = ? AND timestamp >= ? AND (timestamp <= ? OR ? = 0)
    |ORDER BY timestamp ASC
    |LIMIT ?
    """.trimMargin(), mapper, 5) {
      var parameterIndex = 0
      bindString(parameterIndex++, channel)
      bindLong(parameterIndex++, timeStart)
      bindLong(parameterIndex++, timeEnd)
      bindLong(parameterIndex++, timeEnd)
      bindLong(parameterIndex++, limit)
    }

    override fun toString(): String = "Cabal.sq:getMessagesInRange"
  }

  private inner class GetPeerQuery<out T : Any>(
    public val publicKey: ByteArray,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("peer", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("peer", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(1_931_680_901, """SELECT peer.publicKey, peer.name, peer.status, peer.lastSeen, peer.isIgnored, peer.isVerified, peer.role FROM peer WHERE publicKey = ?""", mapper, 1) {
      var parameterIndex = 0
      bindBytes(parameterIndex++, publicKey)
    }

    override fun toString(): String = "Cabal.sq:getPeer"
  }

  private inner class GetLinkPreviewQuery<out T : Any>(
    public val url: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("link_preview", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("link_preview", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(360_044_011, """SELECT link_preview.url, link_preview.title, link_preview.description, link_preview.image, link_preview.timestamp FROM link_preview WHERE url = ?""", mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, url)
    }

    override fun toString(): String = "Cabal.sq:getLinkPreview"
  }
}
