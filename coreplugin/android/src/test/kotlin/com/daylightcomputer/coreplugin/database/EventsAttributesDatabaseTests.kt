package com.daylightcomputer.coreplugin.database

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isEqualToIgnoringGivenProperties
import com.daylightcomputer.coreplugin.database.EventsAttributesDatabase.initialize
import com.daylightcomputer.coreplugin.database.EventsAttributesDatabase.insertAttributeRecord
import com.daylightcomputer.coreplugin.database.sqldefinitions.Attributes
import com.daylightcomputer.coreplugin.database.sqldefinitions.Events
import com.daylightcomputer.coreplugin.database.testing
import com.daylightcomputer.coreplugin.entity.Entity
import com.daylightcomputer.hlc.events.issueLocalEventPacked
import org.junit.Test

class EventsAttributesDatabaseTests {
    @Test
    fun `inserting attributes into events and attribute tables`() {
        testing { db ->
            EventsAttributesDatabase.resetForTesting()
            initialize(db, "client")
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
        }
    }

    @Test
    fun `get entity by id after inserting attribute record`() {
        testing { db ->
            EventsAttributesDatabase.resetForTesting()
            initialize(db, "client")
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
        }
    }
}
