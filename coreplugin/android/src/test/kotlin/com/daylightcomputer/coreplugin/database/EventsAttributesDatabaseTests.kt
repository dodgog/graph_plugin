package com.daylightcomputer.coreplugin.database

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isEqualToIgnoringGivenProperties
import com.daylightcomputer.coreplugin.database.sqldefinitions.Attributes
import com.daylightcomputer.coreplugin.database.sqldefinitions.Events
import com.daylightcomputer.coreplugin.database.testing
import org.junit.Test

class EventsAttributesDatabaseTests {
    @Test
    fun `inserting attributes into events and attribute tables`() {
        testing { db ->
            EventsAttributesDatabase.initialize(db, "client")
            EventsAttributesDatabase.insertAttributeRecord(
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
}
