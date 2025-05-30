package com.daylightcomputer.coreplugin.entity

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.daylightcomputer.coreplugin.database.sqldefinitions.Attributes
import org.junit.Test

/**
 * AIUSE: genai tests
 *
 * Comprehensive test suite for Entity class covering:
 * - Basic functionality
 * - Edge cases and boundary conditions
 * - Performance with large datasets
 * - Error handling
 */
class EntityTests {
    // Helper function to create test Attributes
    private fun createAttribute(
        entityId: String,
        attribute: String,
        value: String?,
        timestamp: String,
    ): Attributes = Attributes(entityId, attribute, value, timestamp)

    @Test
    fun `should create entity with valid parameters`() {
        val attributes =
            mapOf(
                "title" to AttributeValue("Test Title", "2024-01-01"),
                "description" to
                    AttributeValue("Test Description", "2024-01-02"),
            )

        val entity = Entity("test-id", attributes)

        assertThat(entity.id).isEqualTo("test-id")
        assertThat(entity.attributes).hasSize(2)
        assertThat(entity.attributes["title"]?.value).isEqualTo("Test Title")
        assertThat(
            entity.attributes["description"]?.value,
        ).isEqualTo("Test Description")
    }

    @Test
    fun `should create entity with empty attributes map`() {
        val entity = Entity("test-id", emptyMap())

        assertThat(entity.id).isEqualTo("test-id")
        assertThat(entity.attributes).isEmpty()
    }

    @Test
    fun `should handle null attribute values in constructor`() {
        val attributes =
            mapOf(
                "title" to AttributeValue(null, "2024-01-01"),
                "description" to
                    AttributeValue("Valid Description", "2024-01-02"),
            )

        val entity = Entity("test-id", attributes)

        assertThat(entity.attributes["title"]?.value).isNull()
        assertThat(
            entity.attributes["description"]?.value,
        ).isEqualTo("Valid Description")
    }

    @Test
    fun `should create entity from single attribute`() {
        val attributes =
            sequenceOf(
                createAttribute("entity1", "title", "Test Title", "2024-01-01"),
            )

        val entity = Entity.fromAttributePool("entity1", attributes)

        assertThat(entity.id).isEqualTo("entity1")
        assertThat(entity.attributes).hasSize(1)
        assertThat(entity.attributes["title"]?.value).isEqualTo("Test Title")
        assertThat(
            entity.attributes["title"]?.timestamp,
        ).isEqualTo("2024-01-01")
    }

    @Test
    fun `should create entity from multiple attributes`() {
        val attributes =
            sequenceOf(
                createAttribute("entity1", "title", "Test Title", "2024-01-01"),
                createAttribute(
                    "entity1",
                    "description",
                    "Test Description",
                    "2024-01-02",
                ),
                createAttribute("entity1", "type", "document", "2024-01-03"),
            )

        val entity = Entity.fromAttributePool("entity1", attributes)

        assertThat(entity.id).isEqualTo("entity1")
        assertThat(entity.attributes).hasSize(3)
        assertThat(entity.attributes["title"]?.value).isEqualTo("Test Title")
        assertThat(
            entity.attributes["description"]?.value,
        ).isEqualTo("Test Description")
        assertThat(entity.attributes["type"]?.value).isEqualTo("document")
    }

    @Test
    fun `should filter attributes by entity id`() {
        val attributes =
            sequenceOf(
                createAttribute("entity1", "title", "Title 1", "2024-01-01"),
                createAttribute("entity2", "title", "Title 2", "2024-01-02"),
                createAttribute(
                    "entity1",
                    "description",
                    "Description 1",
                    "2024-01-03",
                ),
                createAttribute("entity3", "title", "Title 3", "2024-01-04"),
            )

        val entity = Entity.fromAttributePool("entity1", attributes)

        assertThat(entity.id).isEqualTo("entity1")
        assertThat(entity.attributes).hasSize(2)
        assertThat(entity.attributes["title"]?.value).isEqualTo("Title 1")
        assertThat(
            entity.attributes["description"]?.value,
        ).isEqualTo("Description 1")
    }

    @Test
    fun `should return empty attributes when no matching entity id`() {
        val attributes =
            sequenceOf(
                createAttribute("entity1", "title", "Title 1", "2024-01-01"),
                createAttribute("entity2", "title", "Title 2", "2024-01-02"),
            )

        val entity = Entity.fromAttributePool("nonexistent", attributes)

        assertThat(entity.id).isEqualTo("nonexistent")
        assertThat(entity.attributes).isEmpty()
    }

    @Test
    fun `should handle null attribute values in fromAttributePool`() {
        val attributes =
            sequenceOf(
                createAttribute("entity1", "title", null, "2024-01-01"),
                createAttribute(
                    "entity1",
                    "description",
                    "Valid Description",
                    "2024-01-02",
                ),
            )

        val entity = Entity.fromAttributePool("entity1", attributes)

        assertThat(entity.attributes).hasSize(2)
        assertThat(entity.attributes["title"]?.value).isNull()
        assertThat(
            entity.attributes["description"]?.value,
        ).isEqualTo("Valid Description")
    }

    @Test
    fun `should handle empty sequence for single entity`() {
        val attributes = emptySequence<Attributes>()

        val entity = Entity.fromAttributePool("entity1", attributes)

        assertThat(entity.id).isEqualTo("entity1")
        assertThat(entity.attributes).isEmpty()
    }

    @Test
    fun `should create sequence of entities from mixed attributes`() {
        val attributes =
            sequenceOf(
                createAttribute("entity1", "title", "Title 1", "2024-01-01"),
                createAttribute("entity2", "title", "Title 2", "2024-01-02"),
                createAttribute(
                    "entity1",
                    "description",
                    "Description 1",
                    "2024-01-03",
                ),
                createAttribute("entity3", "title", "Title 3", "2024-01-04"),
                createAttribute("entity2", "type", "document", "2024-01-05"),
            )

        val entities = Entity.fromAttributePool(attributes).toList()

        assertThat(entities).hasSize(3)

        val entity1 = entities.find { it.id == "entity1" }
        val entity2 = entities.find { it.id == "entity2" }
        val entity3 = entities.find { it.id == "entity3" }

        assertThat(entity1).isNotNull()
        assertThat(entity1!!.attributes).hasSize(2)
        assertThat(entity1.attributes["title"]?.value).isEqualTo("Title 1")
        assertThat(
            entity1.attributes["description"]?.value,
        ).isEqualTo("Description 1")

        assertThat(entity2).isNotNull()
        assertThat(entity2!!.attributes).hasSize(2)
        assertThat(entity2.attributes["title"]?.value).isEqualTo("Title 2")
        assertThat(entity2.attributes["type"]?.value).isEqualTo("document")

        assertThat(entity3).isNotNull()
        assertThat(entity3!!.attributes).hasSize(1)
        assertThat(entity3.attributes["title"]?.value).isEqualTo("Title 3")
    }

    @Test
    fun `should handle single entity with multiple attributes`() {
        val attributes =
            sequenceOf(
                createAttribute("entity1", "title", "Title", "2024-01-01"),
                createAttribute(
                    "entity1",
                    "description",
                    "Description",
                    "2024-01-02",
                ),
                createAttribute("entity1", "type", "document", "2024-01-03"),
                createAttribute("entity1", "author", "John Doe", "2024-01-04"),
            )

        val entities = Entity.fromAttributePool(attributes).toList()

        assertThat(entities).hasSize(1)
        val entity = entities.first()
        assertThat(entity.id).isEqualTo("entity1")
        assertThat(entity.attributes).hasSize(4)
    }

    @Test
    fun `should handle empty sequence for multiple entities`() {
        val attributes = emptySequence<Attributes>()

        val entities = Entity.fromAttributePool(attributes).toList()

        assertThat(entities).isEmpty()
    }

    @Test
    fun `should handle very long entity ids`() {
        val longId = "entity_" + "x".repeat(1000)
        val attributes =
            sequenceOf(
                createAttribute(longId, "title", "Title", "2024-01-01"),
            )

        val entity = Entity.fromAttributePool(longId, attributes)

        assertThat(entity.id).isEqualTo(longId)
        assertThat(entity.attributes).hasSize(1)
    }

    @Test
    fun `should handle very long attribute names`() {
        val longAttribute = "attribute_" + "x".repeat(1000)
        val attributes =
            sequenceOf(
                createAttribute(
                    "entity1",
                    longAttribute,
                    "Value",
                    "2024-01-01",
                ),
            )

        val entity = Entity.fromAttributePool("entity1", attributes)

        assertThat(entity.attributes).hasSize(1)
        assertThat(entity.attributes[longAttribute]?.value).isEqualTo("Value")
    }

    @Test
    fun `should handle very long attribute values`() {
        val longValue = "x".repeat(10000)
        val attributes =
            sequenceOf(
                createAttribute("entity1", "content", longValue, "2024-01-01"),
            )

        val entity = Entity.fromAttributePool("entity1", attributes)

        assertThat(entity.attributes["content"]?.value).isEqualTo(longValue)
    }

    @Test
    fun `should handle special characters in entity ids`() {
        val specialId = "entity!@#$%^&*()[]{}|\\:;\"'<>?,./"
        val attributes =
            sequenceOf(
                createAttribute(specialId, "title", "Title", "2024-01-01"),
            )

        val entity = Entity.fromAttributePool(specialId, attributes)

        assertThat(entity.id).isEqualTo(specialId)
        assertThat(entity.attributes).hasSize(1)
    }

    @Test
    fun `should handle unicode characters`() {
        val unicodeId = "测试实体🚀"
        val unicodeAttribute = "标题αβγ"
        val unicodeValue = "测试值 🎉 emojis αβγδε"

        val attributes =
            sequenceOf(
                createAttribute(
                    unicodeId,
                    unicodeAttribute,
                    unicodeValue,
                    "2024-01-01",
                ),
            )

        val entity = Entity.fromAttributePool(unicodeId, attributes)

        assertThat(entity.id).isEqualTo(unicodeId)
        assertThat(
            entity.attributes[unicodeAttribute]?.value,
        ).isEqualTo(unicodeValue)
    }

    @Test
    fun `should handle empty string values`() {
        val attributes =
            sequenceOf(
                createAttribute("", "title", "", "2024-01-01"),
                createAttribute("entity1", "", "value", "2024-01-02"),
            )

        val entities = Entity.fromAttributePool(attributes).toList()

        assertThat(entities).hasSize(2)

        val emptyIdEntity = entities.find { it.id == "" }
        val normalEntity = entities.find { it.id == "entity1" }

        assertThat(emptyIdEntity).isNotNull()
        assertThat(emptyIdEntity!!.attributes["title"]?.value).isEqualTo("")

        assertThat(normalEntity).isNotNull()
        assertThat(normalEntity!!.attributes[""]?.value).isEqualTo("value")
    }

    @Test
    fun `should handle large number of attributes efficiently`() {
        val attributeCount = 1000
        val attributes =
            sequence {
                repeat(attributeCount) { i ->
                    yield(
                        createAttribute(
                            "entity1",
                            "attr_$i",
                            "value_$i",
                            "2024-01-${i.toString().padStart(2, '0')}",
                        ),
                    )
                }
            }

        val entity = Entity.fromAttributePool("entity1", attributes)

        assertThat(entity.attributes).hasSize(attributeCount)
        assertThat(entity.attributes["attr_0"]?.value).isEqualTo("value_0")
        assertThat(entity.attributes["attr_999"]?.value).isEqualTo("value_999")
    }

    @Test
    fun `should handle large number of entities efficiently`() {
        val entityCount = 1000
        val attributes =
            sequence {
                repeat(entityCount) { i ->
                    yield(
                        createAttribute(
                            "entity_$i",
                            "title",
                            "Title $i",
                            "2024-01-01",
                        ),
                    )
                    yield(
                        createAttribute(
                            "entity_$i",
                            "description",
                            "Description $i",
                            "2024-01-02",
                        ),
                    )
                }
            }

        val entities = Entity.fromAttributePool(attributes).toList()

        assertThat(entities).hasSize(entityCount)

        val firstEntity = entities.find { it.id == "entity_0" }
        val lastEntity = entities.find { it.id == "entity_999" }

        assertThat(firstEntity).isNotNull()
        assertThat(firstEntity!!.attributes).hasSize(2)
        assertThat(lastEntity).isNotNull()
        assertThat(lastEntity!!.attributes).hasSize(2)
    }

    @Test
    fun `should preserve timestamp information`() {
        val attributes =
            sequenceOf(
                createAttribute(
                    "entity1",
                    "title",
                    "Title",
                    "2024-01-01T10:00:00Z",
                ),
                createAttribute(
                    "entity1",
                    "description",
                    "Description",
                    "2024-01-02T15:30:45Z",
                ),
            )

        val entity = Entity.fromAttributePool("entity1", attributes)

        assertThat(
            entity.attributes["title"]?.timestamp,
        ).isEqualTo("2024-01-01T10:00:00Z")
        assertThat(
            entity.attributes["description"]?.timestamp,
        ).isEqualTo("2024-01-02T15:30:45Z")
    }

    @Test
    fun `should handle various timestamp formats`() {
        val attributes =
            sequenceOf(
                createAttribute("entity1", "attr1", "value1", "2024-01-01"),
                createAttribute("entity1", "attr2", "value2", "1704067200000"),
                createAttribute(
                    "entity1",
                    "attr3",
                    "value3",
                    "2024-01-01T10:00:00.123Z",
                ),
                createAttribute("entity1", "attr4", "value4", "1-o-clock"),
            )

        val entity = Entity.fromAttributePool("entity1", attributes)

        assertThat(entity.attributes).hasSize(4)
        assertThat(
            entity.attributes["attr1"]?.timestamp,
        ).isEqualTo("2024-01-01")
        assertThat(
            entity.attributes["attr2"]?.timestamp,
        ).isEqualTo("1704067200000")
        assertThat(
            entity.attributes["attr3"]?.timestamp,
        ).isEqualTo("2024-01-01T10:00:00.123Z")
        assertThat(entity.attributes["attr4"]?.timestamp).isEqualTo("1-o-clock")
    }

    @Test
    fun `should handle duplicate attribute names for same entity`() {
        // This tests the groupBy behavior - last one should win in the associate call
        val attributes =
            sequenceOf(
                createAttribute(
                    "entity1",
                    "title",
                    "First Title",
                    "2024-01-01",
                ),
                createAttribute(
                    "entity1",
                    "title",
                    "Second Title",
                    "2024-01-02",
                ),
            )

        val entity = Entity.fromAttributePool("entity1", attributes)

        assertThat(entity.attributes).hasSize(1)
        // The associate function will use the last occurrence
        assertThat(entity.attributes["title"]?.value).isEqualTo("Second Title")
        assertThat(
            entity.attributes["title"]?.timestamp,
        ).isEqualTo("2024-01-02")
    }

    @Test
    fun `should maintain attribute isolation between entities`() {
        val attributes =
            sequenceOf(
                createAttribute(
                    "entity1",
                    "shared_attr",
                    "Value 1",
                    "2024-01-01",
                ),
                createAttribute(
                    "entity2",
                    "shared_attr",
                    "Value 2",
                    "2024-01-02",
                ),
                createAttribute(
                    "entity1",
                    "unique_attr",
                    "Unique Value",
                    "2024-01-03",
                ),
            )

        val entities = Entity.fromAttributePool(attributes).toList()

        val entity1 = entities.find { it.id == "entity1" }
        val entity2 = entities.find { it.id == "entity2" }

        assertThat(
            entity1!!.attributes["shared_attr"]?.value,
        ).isEqualTo("Value 1")
        assertThat(
            entity2!!.attributes["shared_attr"]?.value,
        ).isEqualTo("Value 2")
        assertThat(
            entity1.attributes["unique_attr"]?.value,
        ).isEqualTo("Unique Value")
        assertThat(entity2.attributes["unique_attr"]).isNull()
    }
}
