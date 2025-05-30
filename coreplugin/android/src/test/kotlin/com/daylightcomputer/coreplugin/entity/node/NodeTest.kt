package com.daylightcomputer.coreplugin.entity.node

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.daylightcomputer.coreplugin.entity.AttributeValue
import com.daylightcomputer.coreplugin.entity.Entity
import org.junit.Test

class NodeTest {
    @Test
    fun `single foundation node constructs from entity`() {
        val entity =
            Entity(
                "1",
                mapOf(
                    "type" to AttributeValue("FOUNDATION", "time1"),
                    "isDeleted" to AttributeValue("false", "time2"),
                ),
            )

        val node = Node(entity)
        assertThat(node.type.name).isEqualTo("FOUNDATION")
        assertThat(node.isDeleted).isFalse()
        assertThat(node.lastModifiedAtTimestamp).isEqualTo("time2")
    }

    @Test
    fun `single foundation node with extra attributes throws`() {
        val entity =
            Entity(
                "1",
                mapOf(
                    "type" to AttributeValue("FOUNDATION", "time1"),
                    "isDeleted" to AttributeValue("false", "time2"),
                    "extra" to AttributeValue("wooohoo", "time3"),
                    "extra2" to AttributeValue("wooohoo", "time3"),
                ),
            )

        assertFailure {
            Node(entity)
        }.hasMessage("Node has unknown attributes: `extra`, `extra2`")
    }

    @Test
    fun `throws if required fields not there`() {
        val entity =
            Entity(
                "1",
                mapOf(
                    "isDeleted" to AttributeValue("false", "time2"),
                ),
            )

        assertFailure {
            Node(entity)
        }.hasMessage("Node lacks a required attribute `type`")
    }
}
