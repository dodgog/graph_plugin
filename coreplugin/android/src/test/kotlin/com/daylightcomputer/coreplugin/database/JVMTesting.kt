package com.daylightcomputer.coreplugin.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.daylightcomputer.coreplugin.database.sqldefinitions.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest

fun testing(block: suspend CoroutineScope.(Database) -> Unit) =
    runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.Companion.IN_MEMORY)
        Database.Companion.Schema.create(driver)
        block(Database.Companion(driver))
        driver.close()
    }
