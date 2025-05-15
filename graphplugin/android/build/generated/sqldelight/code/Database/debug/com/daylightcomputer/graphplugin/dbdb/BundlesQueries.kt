package com.daylightcomputer.graphplugin.dbdb

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.SqlDriver
import kotlin.String

public class BundlesQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun getAllBundlesIds(): Query<String> = Query(-1_443_350_356, arrayOf("bundles"), driver,
      "Bundles.sq", "getAllBundlesIds", "SELECT id FROM bundles") { cursor ->
    cursor.getString(0)!!
  }

  public fun insertBundle(
    id: String,
    user_id: String,
    timestamp: String,
    payload: String?,
  ) {
    driver.execute(113_722_549, """
        |INSERT OR IGNORE INTO bundles (id, user_id, timestamp, payload)
        |VALUES(?, ?, ?, ?)
        """.trimMargin(), 4) {
          bindString(0, id)
          bindString(1, user_id)
          bindString(2, timestamp)
          bindString(3, payload)
        }
    notifyQueries(113_722_549) { emit ->
      emit("bundles")
    }
  }
}
