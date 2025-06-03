package com.daylightcomputer.coreplugin.entity.thing

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isGreaterThan
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.daylightcomputer.coreplugin.database.sqldefinitions.Database.Companion.invoke
import com.daylightcomputer.coreplugin.entity.AttributeValueRecord
import com.daylightcomputer.coreplugin.entity.Entity
import com.daylightcomputer.coreplugin.entity.TimestampProvider
import org.junit.Before
import org.junit.Test

class DocumentNodeTest {
    var counter: Int = 0
    val incrementingProvider = TimestampProvider { "timestamp-${++counter}" }

    @Before
    fun setUp() {
        counter = 0
    }

    @Test
    fun `document node constructs from entity with required fields`() {
        val entity =
            Entity(
                "doc1",
                mutableMapOf(
                    "type" to AttributeValueRecord("DOCUMENT_NODE", "timestamp-1"),
                    "title" to AttributeValueRecord("My Document", "timestamp-2"),
                    "isDeleted" to AttributeValueRecord("false", "timestamp-3"),
                ),
                incrementingProvider,
            )

        val node = DocumentNode(entity)
        assertThat(node.type.name).isEqualTo("DOCUMENT_NODE")
        assertThat(node.title).isEqualTo("My Document")
        assertThat(node.author).isNull()
        assertThat(node.isDeleted).isFalse()
        assertThat(node.id).isEqualTo("doc1")
        assertThat(node.lastModifiedAtTimestamp).isEqualTo("timestamp-3")
    }

    @Test
    fun `document node constructs with optional author field`() {
        val entity =
            Entity(
                "doc2",
                mutableMapOf(
                    "type" to AttributeValueRecord("DOCUMENT_NODE", "timestamp-1"),
                    "title" to AttributeValueRecord("Authored Document", "timestamp-2"),
                    "author" to AttributeValueRecord("Anjan", "timestamp-3"),
                    "isDeleted" to AttributeValueRecord("true", "timestamp-4"),
                ),
                incrementingProvider,
            )

        val node = DocumentNode(entity)
        assertThat(node.type.name).isEqualTo("DOCUMENT_NODE")
        assertThat(node.title).isEqualTo("Authored Document")
        assertThat(node.author).isEqualTo("Anjan")
        assertThat(node.isDeleted).isTrue()
        assertThat(node.lastModifiedAtTimestamp).isEqualTo("timestamp-4")
    }

    @Test
    fun `document node constructs without optional isDeleted field`() {
        val entity =
            Entity(
                "doc3",
                mutableMapOf(
                    "type" to
                        AttributeValueRecord(
                            "DOCUMENT_NODE",
                            incrementingProvider.issueTimestamp(),
                        ),
                    "title" to
                        AttributeValueRecord(
                            "Basic Document",
                            incrementingProvider.issueTimestamp(),
                        ),
                ),
                incrementingProvider,
            )

        val node = DocumentNode(entity)
        assertThat(node.type.name).isEqualTo("DOCUMENT_NODE")
        assertThat(node.title).isEqualTo("Basic Document")
        assertThat(node.author).isNull()
        assertThat(node.isDeleted).isFalse()
        assertThat(node.lastModifiedAtTimestamp).isEqualTo("timestamp-2")
    }

    // TODO: check extra attributes perhaps
//    @Test
//    fun `document node with extra attributes throws`() {
//        val entity =
//            Entity(
//                "doc4",
//                mutableMapOf(
//                    "type" to AttributeValueRecord("DOCUMENT_NODE", "timestamp-1"),
//                    "title" to AttributeValueRecord("Document with extras", "timestamp-2"),
//                    "isDeleted" to AttributeValueRecord("false", "timestamp-3"),
//                    "extraField" to AttributeValueRecord("unexpected", "timestamp-4"),
//                    "anotherExtra" to
//                        AttributeValueRecord(
//                            "also unexpected",
//                            "timestamp-5",
//                        ),
//                ),
//            )
//
//        assertFailure {
//            DocumentNode(entity)
//        }.hasMessage(
//            "Node has unknown attributes: `extraField`, `anotherExtra`",
//        )
//    }

    @Test
    fun `throws if required type field is missing`() {
        val entity =
            Entity(
                "doc5",
                mutableMapOf(
                    "title" to AttributeValueRecord("Document without type", "timestamp-1"),
                    "isDeleted" to AttributeValueRecord("false", "timestamp-2"),
                ),
                incrementingProvider,
            )

        assertFailure {
            DocumentNode(entity)
        }.hasMessage("Entity lacks a required attribute `type`")
    }

    @Test
    fun `throws if required title field is missing`() {
        val entity =
            Entity(
                "doc6",
                mutableMapOf(
                    "type" to AttributeValueRecord("DOCUMENT_NODE", "timestamp-1"),
                    "isDeleted" to AttributeValueRecord("false", "timestamp-2"),
                ),
                incrementingProvider,
            )

        assertFailure {
            DocumentNode(entity)
        }.hasMessage("Entity lacks a required attribute `title`")
    }

    @Test
    fun `lastModifiedAtTimestamp returns latest timestamp`() {
        val entity =
            Entity(
                "doc7",
                mutableMapOf(
                    "type" to AttributeValueRecord("DOCUMENT_NODE", "timestamp-1"),
                    "title" to AttributeValueRecord("Timestamped Document", "timestamp-4"),
                    "author" to AttributeValueRecord("Tanuj", "timestamp-3"),
                    "isDeleted" to AttributeValueRecord("false", "timestamp-2"),
                ),
                incrementingProvider,
            )

        val node = DocumentNode(entity)
        assertThat(node.lastModifiedAtTimestamp).isEqualTo("timestamp-4")
    }

    @Test
    fun `throws if title field is encoded as null`() {
        val entity =
            Entity(
                "doc8",
                mutableMapOf(
                    "type" to AttributeValueRecord("DOCUMENT_NODE", "timestamp-1"),
                    "title" to AttributeValueRecord(null, "timestamp-2"),
                    "isDeleted" to AttributeValueRecord("false", "timestamp-3"),
                ),
                incrementingProvider,
            )

        assertFailure {
            DocumentNode(entity)
        }.hasMessage("Title cannot be null for Node entity")
    }

    @Test
    fun `mutable required field can be reassigned`() {
        val entity =
            Entity(
                "doc8",
                mutableMapOf(
                    "type" to AttributeValueRecord("DOCUMENT_NODE", "timestamp-1"),
                    "title" to AttributeValueRecord("Timestamped Document", "timestamp-4"),
                    "author" to AttributeValueRecord("Tanuj", "timestamp-3"),
                    "thought" to AttributeValueRecord("i think 1", "timestamp-2"),
                ),
                incrementingProvider,
            )

        val node = DocumentNode(entity)
        assertThat(node.thought).isEqualTo("i think 1")
        node.thought = "i think 2"
        assertThat(node.thought).isEqualTo("i think 2")
    }

    @Test
    fun `mutable required field reassign increases timestamp`() {
        val entity =
            Entity(
                "doc8",
                mutableMapOf(
                    "type" to
                        AttributeValueRecord(
                            "DOCUMENT_NODE",
                            incrementingProvider.issueTimestamp(),
                        ),
                    "title" to
                        AttributeValueRecord(
                            "Timestamped Document",
                            incrementingProvider.issueTimestamp(),
                        ),
                    "author" to
                        AttributeValueRecord(
                            "Tanuj",
                            incrementingProvider.issueTimestamp(),
                        ),
                    "thought" to
                        AttributeValueRecord("i think 1", incrementingProvider.issueTimestamp()),
                ),
                incrementingProvider,
            )

        val node = DocumentNode(entity)
        val firstTime = node.lastModifiedAtTimestamp
        node.thought = "i think 2"
        val secondTime = node.lastModifiedAtTimestamp
        assertThat(secondTime).isGreaterThan(firstTime)
    }

    @Test
    fun `comment is null by default and can be reassigned`() {
        val entity =
            Entity(
                "doc8",
                mutableMapOf(
                    "type" to
                        AttributeValueRecord(
                            "DOCUMENT_NODE",
                            incrementingProvider.issueTimestamp(),
                        ),
                    "title" to
                        AttributeValueRecord(
                            "Timestamped Document",
                            incrementingProvider.issueTimestamp(),
                        ),
                    "author" to
                        AttributeValueRecord(
                            "Tanuj",
                            incrementingProvider.issueTimestamp(),
                        ),
                    "thought" to
                        AttributeValueRecord("i think 1", incrementingProvider.issueTimestamp()),
                ),
                incrementingProvider,
            )

        val node = DocumentNode(entity)
        val firstTime = node.lastModifiedAtTimestamp
        assertThat(node.comment).isEqualTo(null)
        node.comment = "comment"
        assertThat(node.comment).isEqualTo("comment")
        node.comment = null
        assertThat(node.comment).isEqualTo(null)
        val secondTime = node.lastModifiedAtTimestamp
        assertThat(secondTime).isGreaterThan(firstTime)
    }
}
