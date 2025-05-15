package com.daylightcomputer.graphplugin.dbdb

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.String

public class EventsQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> getEvents(mapper: (
    id: String,
    client_id: String,
    entity_id: String,
    attribute: String,
    value_: String,
    timestamp: String,
  ) -> T): Query<T> = Query(776_104_547, arrayOf("events"), driver, "Events.sq", "getEvents",
      "SELECT events.id, events.client_id, events.entity_id, events.attribute, events.value, events.timestamp FROM events") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!
    )
  }

  public fun getEvents(): Query<Events> = getEvents { id, client_id, entity_id, attribute, value_,
      timestamp ->
    Events(
      id,
      client_id,
      entity_id,
      attribute,
      value_,
      timestamp
    )
  }

  public fun <T : Any> getLocalEventsToPush(mapper: (
    id: String,
    client_id: String,
    entity_id: String,
    attribute: String,
    value_: String,
    timestamp: String,
  ) -> T): Query<T> = Query(1_731_093_039, arrayOf("events", "config"), driver, "Events.sq",
      "getLocalEventsToPush", """
  |SELECT e.id, e.client_id, e.entity_id, e.attribute, e.value, e.timestamp
  |FROM events e
  |WHERE e.timestamp > (
  |  SELECT COALESCE(
  |    (SELECT value FROM config WHERE key = 'last_server_issued_timestamp'),
  |    (SELECT value FROM config WHERE key = 'hlc_absolute_zero')
  |  )
  |)
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!
    )
  }

  public fun getLocalEventsToPush(): Query<Events> = getLocalEventsToPush { id, client_id,
      entity_id, attribute, value_, timestamp ->
    Events(
      id,
      client_id,
      entity_id,
      attribute,
      value_,
      timestamp
    )
  }

  public fun insertEvent(
    id: String,
    client_id: String,
    entity_id: String,
    attribute: String,
    `value`: String,
    timestamp: String,
  ) {
    driver.execute(323_988_661, """
        |INSERT OR IGNORE INTO events (
        |    id,
        |    client_id,
        |    entity_id,
        |    attribute,
        |    value,
        |    timestamp
        |)
        |VALUES (
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?,
        |    ?
        |)
        """.trimMargin(), 6) {
          bindString(0, id)
          bindString(1, client_id)
          bindString(2, entity_id)
          bindString(3, attribute)
          bindString(4, value)
          bindString(5, timestamp)
        }
    notifyQueries(323_988_661) { emit ->
      emit("events")
    }
  }
}
