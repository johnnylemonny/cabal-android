package chat.cabal.database

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import chat.cabal.database.app.newInstance
import chat.cabal.database.app.schema
import kotlin.Unit

public interface CabalDatabase : Transacter {
  public val cabalQueries: CabalQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = CabalDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): CabalDatabase = CabalDatabase::class.newInstance(driver)
  }
}
