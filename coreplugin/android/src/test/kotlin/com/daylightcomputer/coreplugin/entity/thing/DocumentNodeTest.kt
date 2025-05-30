package com.daylightcomputer.coreplugin.entity.thing

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.daylightcomputer.coreplugin.entity.AttributeValueRecord
import com.daylightcomputer.coreplugin.entity.Entity
import org.junit.Test

class DocumentNodeTest {
    @Test
    fun `document node constructs from entity with required fields`() {
        val entity =
            Entity(
                "doc1",
                mapOf(
                    "type" to AttributeValueRecord("DOCUMENT", "time1"),
                    "title" to AttributeValueRecord("My Document", "time2"),
                    "isDeleted" to AttributeValueRecord("false", "time3"),
                ),
            )

        val node = DocumentNode(entity)
        assertThat(node.type.name).isEqualTo("DOCUMENT")
        assertThat(node.title).isEqualTo("My Document")
        assertThat(node.author).isNull()
        assertThat(node.isDeleted).isFalse()
        assertThat(node.id).isEqualTo("doc1")
        assertThat(node.lastModifiedAtTimestamp).isEqualTo("time3")
    }

    @Test
    fun `document node constructs with optional author field`() {
        val entity =
            Entity(
                "doc2",
                mapOf(
                    "type" to AttributeValueRecord("DOCUMENT", "time1"),
                    "title" to AttributeValueRecord("Authored Document", "time2"),
                    "author" to AttributeValueRecord("Anjan", "time3"),
                    "isDeleted" to AttributeValueRecord("true", "time4"),
                ),
            )

        val node = DocumentNode(entity)
        assertThat(node.type.name).isEqualTo("DOCUMENT")
        assertThat(node.title).isEqualTo("Authored Document")
        assertThat(node.author).isEqualTo("Anjan")
        assertThat(node.isDeleted).isTrue()
        assertThat(node.lastModifiedAtTimestamp).isEqualTo("time4")
    }

    @Test
    fun `document node constructs without optional isDeleted field`() {
        val entity =
            Entity(
                "doc3",
                mapOf(
                    "type" to AttributeValueRecord("DOCUMENT", "time1"),
                    "title" to AttributeValueRecord("Basic Document", "time2"),
                ),
            )

        val node = DocumentNode(entity)
        assertThat(node.type.name).isEqualTo("DOCUMENT")
        assertThat(node.title).isEqualTo("Basic Document")
        assertThat(node.author).isNull()
        assertThat(node.isDeleted).isFalse()
        assertThat(node.lastModifiedAtTimestamp).isEqualTo("time2")
    }

    @Test
    fun `document node with extra attributes throws`() {
        val entity =
            Entity(
                "doc4",
                mapOf(
                    "type" to AttributeValueRecord("DOCUMENT", "time1"),
                    "title" to AttributeValueRecord("Document with extras", "time2"),
                    "isDeleted" to AttributeValueRecord("false", "time3"),
                    "extraField" to AttributeValueRecord("unexpected", "time4"),
                    "anotherExtra" to
                        AttributeValueRecord(
                            "also unexpected",
                            "time5",
                        ),
                ),
            )

        assertFailure {
            DocumentNode(entity)
        }.hasMessage(
            "Node has unknown attributes: `extraField`, `anotherExtra`",
        )
    }

    @Test
    fun `throws if required type field is missing`() {
        val entity =
            Entity(
                "doc5",
                mapOf(
                    "title" to AttributeValueRecord("Document without type", "time1"),
                    "isDeleted" to AttributeValueRecord("false", "time2"),
                ),
            )

        assertFailure {
            DocumentNode(entity)
        }.hasMessage("Node lacks a required attribute `type`")
    }

    @Test
    fun `throws if required title field is missing`() {
        val entity =
            Entity(
                "doc6",
                mapOf(
                    "type" to AttributeValueRecord("DOCUMENT", "time1"),
                    "isDeleted" to AttributeValueRecord("false", "time2"),
                ),
            )

        assertFailure {
            DocumentNode(entity)
        }.hasMessage("Node lacks a required attribute `title`")
    }

    @Test
    fun `lastModifiedAtTimestamp returns latest timestamp`() {
        val entity =
            Entity(
                "doc7",
                mapOf(
                    "type" to AttributeValueRecord("DOCUMENT", "time1"),
                    "title" to AttributeValueRecord("Timestamped Document", "time5"),
                    "author" to AttributeValueRecord("Tanuj", "time3"),
                    "isDeleted" to AttributeValueRecord("false", "time2"),
                ),
            )

        val node = DocumentNode(entity)
        assertThat(node.lastModifiedAtTimestamp).isEqualTo("time5")
    }
}
