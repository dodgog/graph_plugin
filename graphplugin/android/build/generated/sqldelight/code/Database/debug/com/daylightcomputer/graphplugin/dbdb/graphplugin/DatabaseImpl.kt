package com.daylightcomputer.graphplugin.dbdb.graphplugin

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.daylightcomputer.graphplugin.dbdb.BundlesQueries
import com.daylightcomputer.graphplugin.dbdb.ConfigQueries
import com.daylightcomputer.graphplugin.dbdb.Database
import com.daylightcomputer.graphplugin.dbdb.EventsQueries
import com.daylightcomputer.graphplugin.dbdb.UsersClientsQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<Database>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = DatabaseImpl.Schema

internal fun KClass<Database>.newInstance(driver: SqlDriver): Database = DatabaseImpl(driver)

private class DatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), Database {
  override val bundlesQueries: BundlesQueries = BundlesQueries(driver)

  override val configQueries: ConfigQueries = ConfigQueries(driver)

  override val eventsQueries: EventsQueries = EventsQueries(driver)

  override val usersClientsQueries: UsersClientsQueries = UsersClientsQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE bundles (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    user_id TEXT NOT NULL REFERENCES users (id),
          |    timestamp TEXT NOT NULL,
          |    payload TEXT
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE config (
          |    key TEXT NOT NULL PRIMARY KEY,
          |    value TEXT,
          |    time_modified TEXT DEFAULT (STRFTIME('%Y-%m-%dT%H:%M:%fZ', 'NOW'))
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE events (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    client_id TEXT NOT NULL REFERENCES clients(id),
          |    -- todo: potentially also add the user_id list to specify user groups in the future
          |
          |    -- part of the event which exactly matches the attributes table
          |    entity_id TEXT NOT NULL, -- doesn't always reference an existing node, so references is omitted
          |    attribute TEXT NOT NULL,
          |    value TEXT NOT NULL,
          |    timestamp TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE users (
          |  id TEXT NOT NULL PRIMARY KEY,
          |  name TEXT
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE clients (
          |  id TEXT NOT NULL PRIMARY KEY,
          |  user_id TEXT REFERENCES users (id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TRIGGER IF NOT EXISTS config_auto_update_time_modified
          |AFTER UPDATE ON config
          |FOR EACH ROW
          |WHEN old.value IS NOT new.value OR (old.value IS NULL AND new.value IS NOT NULL) OR (old.value IS NOT NULL AND new.value IS NULL)
          |BEGIN
          |    UPDATE config SET time_modified = STRFTIME('%Y-%m-%dT%H:%M:%fZ', 'NOW') WHERE key = new.key;
          |END
          """.trimMargin(), 0)
      driver.execute(null, "CREATE INDEX event_client_id_index ON events(client_id)", 0)
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
