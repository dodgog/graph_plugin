package com.daylightcomputer.graphplugin.dbdb

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.daylightcomputer.graphplugin.dbdb.graphplugin.graphplugin.main.newInstance
import com.daylightcomputer.graphplugin.dbdb.graphplugin.graphplugin.main.schema
import kotlin.Unit

public interface Database : Transacter {
  public val appDataQueries: AppDataQueries

  public val eventsQueries: EventsQueries

  public val userClientQueries: UserClientQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = Database::class.schema

    public operator fun invoke(driver: SqlDriver): Database = Database::class.newInstance(driver)
  }
}
