package com.daylightcomputer.coreplugin.database

import com.daylightcomputer.coreplugin.database.sqldefinitions.Database
import com.daylightcomputer.coreplugin.entity.AttributeValueRecord
import com.daylightcomputer.coreplugin.entity.Entity
import com.daylightcomputer.hlc.HLC
import com.daylightcomputer.hlc.HLCConfig
import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.events.issueLocalEventPacked
import com.daylightcomputer.hlc.model.DistributedNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.UUID

/**
 * Singleton providing low level database operations
 *
 * - Triggers reduction of events into attributes where needed
 * - Creates entities from attributes
 * - Manages mutation of entities inserting them into event and attribute tables
 * */
object EventsAttributesDatabase {
    private lateinit var hlcInstance: HLC
    private lateinit var databaseInstance: Database
    private lateinit var scope: CoroutineScope

    // TODO: initialize client
    private lateinit var clientId: String

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

    fun close() {
        scope.cancel()
    }

    fun initialize(
        database: Database,
        clientId: String,
        scope: CoroutineScope,
    ) {
        synchronized(this) {
            if (isInitialized) {
                println("EventsAttributesDatabase already initialized.")
                return
            }

            try {
                HLCEnvironment.initialize(HLCConfig())
            } catch (e: IllegalStateException) {
                throw IllegalStateException("HLCEnvironment was already initialized: ${e.message}")
            }

            EventsAttributesDatabase.clientId = clientId

            hlcInstance =
                HLC(
                    distributedNode = DistributedNode(clientId),
                )

            databaseInstance = database

            EventsAttributesDatabase.scope = scope

            // TODO: initialize client somewhere, if stored -- good, if not then create
//            client_id = databaseInstance.configQueries.getCurrentClient().executeAsOneOrNull()

            isInitialized = true
        }
    }

    /**
     * Reset the singleton state for testing purposes.
     * Should only be used in test environments.
     */
    internal fun resetForTesting() {
        synchronized(this) {
            isInitialized = false
            HLCEnvironment.uninitialize()
        }
    }

    private fun insertAttributeRecord(
        entityId: String,
        attrName: String,
        attrVal: String?,
        timestamp: String,
    ) {
        db.eventsQueries.insertEvent(
            UUID.randomUUID().toString(),
            clientId,
            entityId,
            attrName,
            attrVal,
            timestamp,
        )

        db.attributesQueries.upsertEventIntoAttributes(
            attrVal,
            timestamp,
            entityId,
            attrName,
        )
    }

    fun insertAttributeRecord(
        entityId: String,
        attribute: Pair<String, AttributeValueRecord>,
    ) {
        insertAttributeRecord(
            entityId,
            attribute.first,
            attribute.second.value,
            attribute.second.timestamp,
        )
    }

    fun getEntity(entityId: String): Entity {
        val attributes = db.attributesQueries.getAttributesForEntity(entityId).executeAsList()
        val entity =
            Entity.fromAttributePool(entityId, attributes) { hlcInstance.issueLocalEventPacked() }

        entity.attributeChanges
            .onEach { change -> insertAttributeRecord(entityId, change) }
            .launchIn(scope)

        return entity
    }

    fun getAllEntities(): Sequence<Entity> {
        val attrs = db.attributesQueries.getAttributes().executeAsList()
        return Entity
            .allFromAttributePool(attrs) { hlcInstance.issueLocalEventPacked() }
            .onEach { entity ->
                entity.attributeChanges
                    .onEach { change -> insertAttributeRecord(entity.id, change) }
                    .launchIn(scope)
            }
    }
}
