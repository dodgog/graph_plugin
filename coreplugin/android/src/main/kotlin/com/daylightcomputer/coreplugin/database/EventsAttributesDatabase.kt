package com.daylightcomputer.coreplugin.database

import app.cash.sqldelight.db.SqlDriver
import com.daylightcomputer.coreplugin.database.sqldefinitions.Database
import com.daylightcomputer.hlc.HLC
import com.daylightcomputer.hlc.HLCConfig
import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.model.DistributedNode

/** Singleton providing low level database operations */
object EventsAttributesDatabase {
    private lateinit var hlcInstance: HLC
    private lateinit var databaseInstance: Database

    @Volatile
    private var isInitialized = false

    val hlc: HLC
        get() {
            if (!isInitialized) {
                throw IllegalStateException(
                    "HLC not initialized." +
                        "Call EventsAttributesDatabase.initialize() first.",
                )
            }
            return hlcInstance
        }

    val db: Database
        get() {
            if (!isInitialized) {
                throw IllegalStateException(
                    "Database not initialized. " +
                        "Call EventsAttributesDatabase.initialize() first.",
                )
            }
            return databaseInstance
        }

    fun initialize(
        driver: SqlDriver,
        nodeId: String,
    ) {
        synchronized(this) {
            if (isInitialized) {
                println("EventsAttributesDatabase already initialized.")
                return
            }

            try {
                HLCEnvironment.initialize(HLCConfig())
            } catch (e: IllegalStateException) {
                println("HLCEnvironment was already initialized: ${e.message}")
            }

            hlcInstance =
                HLC(
                    distributedNode = DistributedNode(nodeId),
                )

            databaseInstance = Database(driver)

            isInitialized = true
        }
    }
}
