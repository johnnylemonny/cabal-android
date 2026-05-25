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
  ) -> T): Query<T> = GetMessagesByChannelQuery(channel) { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getBytes(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getBytes(5)!!,
      cursor.getLong(6)!!
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
  ) -> T): Query<T> = GetMessagesByHashesQuery(hash) { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getBytes(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getBytes(5)!!,
      cursor.getLong(6)!!
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
    ) -> T,
  ): Query<T> = GetMessagesInRangeQuery(channel, timeStart, timeEnd, limit) { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getBytes(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!,
      cursor.getBytes(5)!!,
      cursor.getLong(6)!!
    )
  }

  public fun getMessagesInRange(
    channel: String,
    timeStart: Long,
    timeEnd: Long,
    limit: Long,
  ): Query<Message> = getMessagesInRange(channel, timeStart, timeEnd, limit, ::Message)

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
  ): QueryResult<Long> {
    val result = driver.execute(967_430_139, """
        |INSERT OR IGNORE INTO message(hash, publicKey, channel, timestamp, text, rawPost, status)
        |VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 7) {
          var parameterIndex = 0
          bindBytes(parameterIndex++, hash)
          bindBytes(parameterIndex++, publicKey)
          bindString(parameterIndex++, channel)
          bindLong(parameterIndex++, timestamp)
          bindString(parameterIndex++, text)
          bindBytes(parameterIndex++, rawPost)
          bindLong(parameterIndex++, status)
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
    |SELECT message.hash, message.publicKey, message.channel, message.timestamp, message.text, message.rawPost, message.status FROM message
    |WHERE channel = ?
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
          |SELECT message.hash, message.publicKey, message.channel, message.timestamp, message.text, message.rawPost, message.status FROM message
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
    |SELECT message.hash, message.publicKey, message.channel, message.timestamp, message.text, message.rawPost, message.status FROM message
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
}
