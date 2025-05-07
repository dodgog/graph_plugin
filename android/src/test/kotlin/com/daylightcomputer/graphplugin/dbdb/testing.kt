package com.daylightcomputer.graphplugin.dbdb

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest

fun testing(block: suspend CoroutineScope.(Database) -> Unit) = runTest {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    Database.Schema.create(driver)
    block(Database(driver))
    driver.close()
}
