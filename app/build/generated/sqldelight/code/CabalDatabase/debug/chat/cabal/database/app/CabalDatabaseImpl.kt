package chat.cabal.database.app

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import chat.cabal.database.CabalDatabase
import chat.cabal.database.CabalQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<CabalDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = CabalDatabaseImpl.Schema

internal fun KClass<CabalDatabase>.newInstance(driver: SqlDriver): CabalDatabase = CabalDatabaseImpl(driver)

private class CabalDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver),
    CabalDatabase {
  override val cabalQueries: CabalQueries = CabalQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE cabal (
          |    key TEXT NOT NULL PRIMARY KEY,
          |    name TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE message (
          |    hash BLOB NOT NULL PRIMARY KEY,
          |    publicKey BLOB NOT NULL,
          |    channel TEXT NOT NULL,
          |    timestamp INTEGER NOT NULL,
          |    text TEXT NOT NULL,
          |    rawPost BLOB NOT NULL, -- The original signed post bytes
          |    status INTEGER NOT NULL DEFAULT 0 -- 0: Sending, 1: Sent to network, 2: Confirmed
          |)
          """.trimMargin(), 0)
      driver.execute(null, "CREATE INDEX message_channel_timestamp ON message(channel, timestamp)", 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
