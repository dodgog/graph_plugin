package com.daylightcomputer.coreplugin.database.sqldefinitions

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest

fun testing(block: suspend CoroutineScope.(Database) -> Unit) =
    runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.Companion.IN_MEMORY)
        Database.Companion.Schema.create(driver)
        block(Database.Companion(driver))
        driver.close()
    }
