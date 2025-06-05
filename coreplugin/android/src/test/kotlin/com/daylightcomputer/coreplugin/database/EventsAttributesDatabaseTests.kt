package com.daylightcomputer.coreplugin.database

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isEqualToIgnoringGivenProperties
import com.daylightcomputer.coreplugin.database.EventsAttributesDatabase.getAllEntities
import com.daylightcomputer.coreplugin.database.EventsAttributesDatabase.initialize
import com.daylightcomputer.coreplugin.database.EventsAttributesDatabase.insertAttributeRecord
import com.daylightcomputer.coreplugin.database.EventsAttributesDatabase.resetForTesting
import com.daylightcomputer.coreplugin.database.sqldefinitions.Attributes
import com.daylightcomputer.coreplugin.database.sqldefinitions.Events
import com.daylightcomputer.coreplugin.entity.Entity
import com.daylightcomputer.hlc.events.issueLocalEventPacked
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test

class EventsAttributesDatabaseTests {
    @OptIn(
        ExperimentalStdlibApi::class,
        ExperimentalCoroutinesApi::class,
    )
    @Test
    fun `inserting attributes into events and attribute tables`() {
        testing { db ->
            resetForTesting()
            val databaseScope =
                CoroutineScope(
                    // supervisor job, so that it doesn't blow up when closed
                    // because the blow up radius will take down the test scope too
                    SupervisorJob(this.coroutineContext.job) +
                        // use the test dispatcher, TODO: this is probably bad code
                        this.coroutineContext[CoroutineDispatcher]!!,
                )

            initialize(db, "client", databaseScope)
            insertAttributeRecord(
                "entity_id",
                "type",
                "bob",
                "time1",
            )

            val events = db.eventsQueries.getEvents().executeAsList()
            assertThat(events.first()).isEqualToIgnoringGivenProperties(
                Events(
                    "uuid",
                    "client",
                    "entity_id",
                    "type",
                    "bob",
                    "time1",
                ),
                Events::id,
            )

            val attribute = db.attributesQueries.getAttributes().executeAsList()
            assertThat(attribute.first()).isEqualTo(
                Attributes(
                    "entity_id",
                    "type",
                    "bob",
                    "time1",
                ),
            )
            EventsAttributesDatabase.close()
        }
    }

    @OptIn(
        ExperimentalStdlibApi::class,
        ExperimentalCoroutinesApi::class,
    )
    @Test
    fun `get entity by id after inserting attribute record`() {
        testing { db ->
            resetForTesting()

            val databaseScope =
                CoroutineScope(
                    // supervisor job, so that it doesn't blow up when closed
                    // because the blow up radius will take down the test scope too
                    SupervisorJob(this.coroutineContext.job) +
                        // use the test dispatcher, TODO: this is probably bad code
                        this.coroutineContext[CoroutineDispatcher]!!,
                )

            initialize(db, "client", databaseScope)

            val entity =
                Entity.fromAttributePool(
                    "entity_id",
                    listOf(
                        Attributes(
                            "entity_id",
                            "name",
                            "value",
                            "time1",
                        ),
                    ),
                ) { EventsAttributesDatabase.hlc.issueLocalEventPacked() }

            db.transaction {
                entity.attributesList.onEach { insertAttributeRecord(it) }
            }

            assertThat(EventsAttributesDatabase.getAllEntities().toList()).isEqualTo(listOf(entity))
            EventsAttributesDatabase.close()
        }
    }

    @OptIn(
        ExperimentalStdlibApi::class,
        ExperimentalCoroutinesApi::class,
    )
    @Test
    fun `get entity and mutate it`() {
        testing { db ->
            // Mark as uninitialized
            resetForTesting()

            val databaseScope =
                CoroutineScope(
                    // supervisor job, so that it doesn't blow up when closed
                    // because the blow up radius will take down the test scope too
                    SupervisorJob(this.coroutineContext.job) +
                        // use the test dispatcher, TODO: this is probably bad code
                        this.coroutineContext[CoroutineDispatcher]!!,
                )

            initialize(db, "client", databaseScope)

            // Mock an existing entity
            val entity =
                Entity.fromAttributePool(
                    "entity_id",
                    listOf(
                        Attributes(
                            "entity_id",
                            "name",
                            "value",
                            EventsAttributesDatabase.hlc.issueLocalEventPacked(),
                        ),
                    ),
                ) { EventsAttributesDatabase.hlc.issueLocalEventPacked() }

            // Insert the entity by disassembling into attributes
            db.transaction {
                entity.attributesList.onEach { insertAttributeRecord(it) }
            }

            // Get the single entity back from the db
            val retrievedEntities = getAllEntities().toList()
            assertThat(retrievedEntities.size).isEqualTo(1)
            val retrievedEntity = retrievedEntities.first()

            // Mutate the entity
            retrievedEntity.setAttribute("name", "value2")

            // The entity object should've changed
            assertThat(retrievedEntity.getAttribute("name")).isEqualTo("value2")

            // Make test scope check for tasks, such as shared flow of the entity
            // TODO: revisit this approach, perhaps if i use regular flow and not shared flow,
            // then i won't have coroutine issues
            advanceUntilIdle()

            // The change should've propagated to the database
            val retrievedEntitiesAfterMutation = getAllEntities().toList()
            assertThat(retrievedEntitiesAfterMutation).containsExactly(retrievedEntity)

            val retrievedEntityAfterMutation = EventsAttributesDatabase.getEntity(entity.id)
            assertThat(retrievedEntityAfterMutation).equals(retrievedEntity)

            // Close the databaseScope coroutine
            EventsAttributesDatabase.close()
        }
    }
}
