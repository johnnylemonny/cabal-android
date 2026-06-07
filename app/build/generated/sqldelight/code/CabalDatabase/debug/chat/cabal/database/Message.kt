package chat.cabal.database

import kotlin.ByteArray
import kotlin.Long
import kotlin.String

public data class Message(
  public val hash: ByteArray,
  public val publicKey: ByteArray,
  public val channel: String,
  public val timestamp: Long,
  public val text: String,
  public val rawPost: ByteArray,
  public val status: Long,
  public val parentHash: ByteArray?,
  public val isEdited: Long,
  public val isDeleted: Long,
  public val ttl: Long?,
)
