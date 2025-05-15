package com.daylightcomputer.graphplugin.dbdb

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.String

public class ConfigQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> getValueForKey(key: String, mapper: (value_: String?) -> T): Query<T> =
      GetValueForKeyQuery(key) { cursor ->
    mapper(
      cursor.getString(0)
    )
  }

  public fun getValueForKey(key: String): Query<GetValueForKey> = getValueForKey(key) { value_ ->
    GetValueForKey(
      value_
    )
  }

  public fun <T : Any> getAllConfigValues(mapper: (
    key: String,
    value_: String?,
    time_modified: String?,
  ) -> T): Query<T> = Query(-1_385_468_252, arrayOf("config"), driver, "Config.sq",
      "getAllConfigValues", "SELECT key, value, time_modified FROM config") { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1),
      cursor.getString(2)
    )
  }

  public fun getAllConfigValues(): Query<Config> = getAllConfigValues { key, value_,
      time_modified ->
    Config(
      key,
      value_,
      time_modified
    )
  }

  public fun <T : Any> getCurrentClient(mapper: (id: String, user_id: String?) -> T): Query<T> =
      Query(-869_916_221, arrayOf("clients", "config"), driver, "Config.sq", "getCurrentClient", """
  |SELECT cl.id, cl.user_id
  |FROM clients cl
  |WHERE cl.id = (SELECT value FROM config WHERE key = 'client_id')
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)
    )
  }

  public fun getCurrentClient(): Query<Clients> = getCurrentClient { id, user_id ->
    Clients(
      id,
      user_id
    )
  }

  public fun <T : Any> getCurrentUser(mapper: (id: String, name: String?) -> T): Query<T> =
      Query(-1_582_483_517, arrayOf("users", "config"), driver, "Config.sq", "getCurrentUser", """
  |SELECT u.id, u.name
  |FROM users u
  |WHERE u.id = (SELECT value FROM config WHERE key = 'user_id')
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)
    )
  }

  public fun getCurrentUser(): Query<Users> = getCurrentUser { id, name ->
    Users(
      id,
      name
    )
  }

  public fun initializeConfig() {
    driver.execute(-945_826_361, """
        |INSERT OR IGNORE INTO config (key, value) VALUES
        |    ('client_id', NULL),
        |    ('last_server_issued_timestamp', '1970-01-01T00:00:01.000Z-0000-serverId'),
        |    ('user_id', NULL),
        |    ('user_token', NULL),
        |    ('hlc_absolute_zero', '1970-01-01T00:00:01.000Z-0000')
        """.trimMargin(), 0)
    notifyQueries(-945_826_361) { emit ->
      emit("config")
    }
  }

  public fun setValueForKey(key: String, `value`: String?) {
    driver.execute(-1_461_956_614, """INSERT OR REPLACE INTO config (key, value) VALUES (?, ?)""",
        2) {
          bindString(0, key)
          bindString(1, value)
        }
    notifyQueries(-1_461_956_614) { emit ->
      emit("config")
    }
  }

  public fun setLastSyncTime(last_sync_time: String?) {
    driver.execute(2_030_007_499,
        """INSERT OR REPLACE INTO config (key, value) VALUES ('last_server_issued_timestamp', ?)""",
        1) {
          bindString(0, last_sync_time)
        }
    notifyQueries(2_030_007_499) { emit ->
      emit("config")
    }
  }

  public fun setClientId(client_id: String?) {
    driver.execute(1_699_192_019,
        """INSERT OR REPLACE INTO config (key, value) VALUES ('client_id', ?)""", 1) {
          bindString(0, client_id)
        }
    notifyQueries(1_699_192_019) { emit ->
      emit("config")
    }
  }

  public fun setUserId(user_id: String?) {
    driver.execute(-1_746_938_861,
        """INSERT OR REPLACE INTO config (key, value) VALUES ('user_id', ?)""", 1) {
          bindString(0, user_id)
        }
    notifyQueries(-1_746_938_861) { emit ->
      emit("config")
    }
  }

  public fun setUserToken(user_token: String?) {
    driver.execute(-926_289_919,
        """INSERT OR REPLACE INTO config (key, value) VALUES ('user_token', ?)""", 1) {
          bindString(0, user_token)
        }
    notifyQueries(-926_289_919) { emit ->
      emit("config")
    }
  }

  private inner class GetValueForKeyQuery<out T : Any>(
    public val key: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("config", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("config", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(309_779_590, """SELECT value FROM config WHERE key = ?""", mapper, 1) {
      bindString(0, key)
    }

    override fun toString(): String = "Config.sq:getValueForKey"
  }
}
