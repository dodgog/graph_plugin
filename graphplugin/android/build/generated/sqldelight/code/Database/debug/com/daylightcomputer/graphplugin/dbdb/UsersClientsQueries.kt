package com.daylightcomputer.graphplugin.dbdb

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.String

public class UsersClientsQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> getUserFromClientId(client_id: String, mapper: (id: String,
      name: String?) -> T): Query<T> = GetUserFromClientIdQuery(client_id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)
    )
  }

  public fun getUserFromClientId(client_id: String): Query<Users> = getUserFromClientId(client_id) {
      id, name ->
    Users(
      id,
      name
    )
  }

  public fun addKnownClient(client_id: String, user_id: String?) {
    driver.execute(335_980_032, """INSERT INTO clients (id, user_id) VALUES (?, ?)""", 2) {
          bindString(0, client_id)
          bindString(1, user_id)
        }
    notifyQueries(335_980_032) { emit ->
      emit("clients")
    }
  }

  public fun addKnownUser(id: String, name: String?) {
    driver.execute(1_600_890_688, """INSERT INTO users (id, name) VALUES (?, ?)""", 2) {
          bindString(0, id)
          bindString(1, name)
        }
    notifyQueries(1_600_890_688) { emit ->
      emit("users")
    }
  }

  private inner class GetUserFromClientIdQuery<out T : Any>(
    public val client_id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("users", "clients", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("users", "clients", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-2_003_552_674, """
    |SELECT u.id, u.name
    |FROM users u
    |INNER JOIN clients c ON c.user_id = u.id
    |WHERE c.id = ?
    """.trimMargin(), mapper, 1) {
      bindString(0, client_id)
    }

    override fun toString(): String = "UsersClients.sq:getUserFromClientId"
  }
}
