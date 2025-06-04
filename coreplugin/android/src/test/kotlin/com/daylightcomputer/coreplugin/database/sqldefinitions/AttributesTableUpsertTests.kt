package com.daylightcomputer.coreplugin.database.sqldefinitions

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.daylightcomputer.coreplugin.database.testing
import org.junit.Test

class AttributesTableUpsertTests {
    @Test
    fun `attribute conflicts should keep newer timestamp`() =
        testing { db ->
            val olderTimestamp = "a-o-clock"
            val newerTimestamp = "b-o-clock"

            db.attributesQueries.upsertEventIntoAttributes(
                entity_id = "node1",
                attr_name = "title",
                attr_val = "Old Title",
                timestamp = olderTimestamp,
            )

            // Verify older event was inserted
            val attributesAfterFirst =
                db.attributesQueries
                    .getAttributes()
                    .executeAsList()
            assertThat(attributesAfterFirst).hasSize(1)
            assertThat(attributesAfterFirst.first().attr_val)
                .isEqualTo("Old Title")
            assertThat(attributesAfterFirst.first().timestamp)
                .isEqualTo(olderTimestamp)

            // Insert newer event - should update the existing attribute
            db.attributesQueries.upsertEventIntoAttributes(
                entity_id = "node1",
                attr_name = "title",
                attr_val = "New Title",
                timestamp = newerTimestamp,
            )

            // Verify newer event replaced the older one
            val attributesAfterSecond =
                db.attributesQueries
                    .getAttributes()
                    .executeAsList()
            assertThat(attributesAfterSecond).hasSize(1)
            assertThat(
                attributesAfterSecond.first().attr_val,
            ).isEqualTo("New Title")
            assertThat(
                attributesAfterSecond.first().timestamp,
            ).isEqualTo(newerTimestamp)

            // Try inserting older event again - should NOT override newer attr_val
            db.attributesQueries.upsertEventIntoAttributes(
                entity_id = "node1",
                attr_name = "title",
                attr_val = "Old Title",
                timestamp = olderTimestamp,
            )

            // Verify that newer attr_val is still preserved
            val attributesAfterOldInsert =
                db.attributesQueries
                    .getAttributes()
                    .executeAsList()
            assertThat(attributesAfterOldInsert).hasSize(1)
            assertThat(
                attributesAfterOldInsert.first().attr_val,
            ).isEqualTo("New Title")
            assertThat(
                attributesAfterOldInsert.first().timestamp,
            ).isEqualTo(newerTimestamp)
        }

    @Test
    fun `multiple attributes for same entity should coexist`() =
        testing { db ->
            val timestamp = "1-o-clock"

            // Insert different attributes for the same entity
            db.attributesQueries.upsertEventIntoAttributes(
                entity_id = "node1",
                attr_name = "title",
                attr_val = "Node Title",
                timestamp = timestamp,
            )

            db.attributesQueries.upsertEventIntoAttributes(
                entity_id = "node1",
                attr_name = "description",
                attr_val = "Node Description",
                timestamp = timestamp,
            )

            // Both attributes should exist
            val attributes =
                db.attributesQueries
                    .getAttributes()
                    .executeAsList()
            assertThat(attributes).hasSize(2)

            // Verify specific entity attributes
            val entityAttributes =
                db.attributesQueries
                    .getAttributesForEntity(
                        "node1",
                    ).executeAsList()
            assertThat(entityAttributes).hasSize(2)

            val titleAttr = entityAttributes.find { it.attr_name == "title" }
            val descAttr =
                entityAttributes.find {
                    it.attr_name == "description"
                }

            assertThat(titleAttr?.attr_val).isEqualTo("Node Title")
            assertThat(descAttr?.attr_val).isEqualTo("Node Description")
        }

    @Test
    fun `different entities with same attribute should not conflict`() =
        testing { db ->
            val timestamp = "1-o-clock"

            // Insert same attribute name for different entities
            db.attributesQueries.upsertEventIntoAttributes(
                entity_id = "node1",
                attr_name = "title",
                attr_val = "Node 1 Title",
                timestamp = timestamp,
            )

            db.attributesQueries.upsertEventIntoAttributes(
                entity_id = "node2",
                attr_name = "title",
                attr_val = "Node 2 Title",
                timestamp = timestamp,
            )

            // Both should exist independently
            val allAttributes =
                db.attributesQueries
                    .getAttributes()
                    .executeAsList()
            assertThat(allAttributes).hasSize(2)

            val node1Attrs =
                db.attributesQueries
                    .getAttributesForEntity(
                        "node1",
                    ).executeAsList()
            val node2Attrs =
                db.attributesQueries
                    .getAttributesForEntity(
                        "node2",
                    ).executeAsList()

            assertThat(node1Attrs).hasSize(1)
            assertThat(node2Attrs).hasSize(1)
            assertThat(node1Attrs.first().attr_val).isEqualTo("Node 1 Title")
            assertThat(node2Attrs.first().attr_val).isEqualTo("Node 2 Title")
        }

    @Test
    fun `empty database should return no attributes`() =
        testing { db ->

            val attributes =
                db.attributesQueries
                    .getAttributes()
                    .executeAsList()
            assertThat(attributes).isEmpty()

            val entityAttributes =
                db.attributesQueries
                    .getAttributesForEntity(
                        "nonexistent",
                    ).executeAsList()
            assertThat(entityAttributes).isEmpty()
        }

    @Test
    fun `same timestamp should not insert`() =
        testing { db ->
            val timestamp = "1-o-clock"

            // First insertion with timestamp
            db.attributesQueries.upsertEventIntoAttributes(
                entity_id = "node1",
                attr_name = "title",
                attr_val = "First Value",
                timestamp = timestamp,
            )

            // Second insertion with exact same timestamp should not update
            // (since timestamp is not greater)
            db.attributesQueries.upsertEventIntoAttributes(
                entity_id = "node1",
                attr_name = "title",
                attr_val = "Second Value",
                timestamp = timestamp,
            )

            val attributes =
                db.attributesQueries
                    .getAttributes()
                    .executeAsList()
            assertThat(attributes).hasSize(1)
            // Should keep first attr_val
            assertThat(attributes.first().attr_val).isEqualTo("First Value")
        }
}
