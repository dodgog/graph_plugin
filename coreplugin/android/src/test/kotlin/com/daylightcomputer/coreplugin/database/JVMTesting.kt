package com.daylightcomputer.coreplugin.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.daylightcomputer.coreplugin.database.sqldefinitions.Database
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

// TODO This used to run in a coroutine scope, but then advance until idle
fun testing(block: suspend TestScope.(Database) -> Unit) =
    runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.Companion.IN_MEMORY)
        Database.Companion.Schema.create(driver)
        block(Database.Companion(driver))
        driver.close()
    }
