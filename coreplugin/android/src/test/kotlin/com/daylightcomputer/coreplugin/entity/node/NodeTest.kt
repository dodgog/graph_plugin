package com.daylightcomputer.coreplugin.entity.node

import assertk.assertThat
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

        val node = Node.fromEntity(entity)
        assertThat(node!!.type.name).isEqualTo("FOUNDATION")
        assertThat(node.isDeleted).isFalse()
        assertThat(node.lastModifiedAtTimestamp).isEqualTo("time2")
    }

    @Test
    fun `single foundation node with extra attributes modifies time`() {
        val entity =
            Entity(
                "1",
                mapOf(
                    "type" to AttributeValue("FOUNDATION", "time1"),
                    "isDeleted" to AttributeValue("false", "time2"),
                    "extra" to AttributeValue("false", "time3"),
                ),
            )

        val node = Node.fromEntity(entity)
        assertThat(node!!.type.name).isEqualTo("FOUNDATION")
        assertThat(node.isDeleted).isFalse()
        assertThat(node.lastModifiedAtTimestamp).isEqualTo("time3")
        val node2 = DocumentNode.fromEntity(entity)?.copy()
    }
}
