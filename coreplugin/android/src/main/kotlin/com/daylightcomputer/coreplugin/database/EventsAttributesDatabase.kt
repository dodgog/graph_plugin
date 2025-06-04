package com.daylightcomputer.coreplugin.database

import com.daylightcomputer.coreplugin.database.sqldefinitions.Database
import com.daylightcomputer.coreplugin.entity.Entity
import com.daylightcomputer.hlc.HLC
import com.daylightcomputer.hlc.HLCConfig
import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.events.issueLocalEventPacked
import com.daylightcomputer.hlc.model.DistributedNode
import kotlinx.coroutines.GlobalScope
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

    fun initialize(
        database: Database,
        clientId: String,
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

            EventsAttributesDatabase.clientId = clientId

            hlcInstance =
                HLC(
                    distributedNode = DistributedNode(clientId),
                )

            databaseInstance = database

            // TODO: initialize client somewhere, if stored -- good, if not then create
//            client_id = databaseInstance.configQueries.getCurrentClient().executeAsOneOrNull()

            isInitialized = true
        }
    }

    fun insertAttributeRecord(
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

    fun getEntity(entityId: String): Entity {
        val attributes = db.attributesQueries.getAttributesForEntity(entityId).executeAsList()
        val entity =
            Entity.fromAttributePool(
                entityId,
                attributes,
            ) { hlcInstance.issueLocalEventPacked() }
        // TODO: figure out how to scope out this so that when we stop using the entity, its scope also dissapears
        entity.attributeChanges
            .onEach {
                // insert it into the events and attributes
                println(it)
                insertAttributeRecord(
                    entityId,
                    it.first,
                    it.second.value,
                    it.second.timestamp,
                )
            }.launchIn(GlobalScope)

        return entity
    }

    fun getAllEntities(): Sequence<Entity> {
        val attributes = db.attributesQueries.getAttributes().executeAsList()
        val entities =
            Entity.allFromAttributePool(
                attributes,
            ) { hlcInstance.issueLocalEventPacked() }
        // TODO: figure out how to scope out this so that when we stop using the entity, its scope also dissapears
        entities.onEach { e ->
            e.attributeChanges
                .onEach {
                    println(it)
                    // insert into events and attributes
                    insertAttributeRecord(
                        e.id,
                        it.first,
                        it.second.value,
                        it.second.timestamp,
                    )
                }.launchIn(GlobalScope)
        }
        return entities
    }
}
