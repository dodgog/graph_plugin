package com.daylightcomputer.coreplugin.entity.thing

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.daylightcomputer.coreplugin.entity.AttributeValueRecord
import com.daylightcomputer.coreplugin.entity.Entity
import org.junit.Test

class ThingTest {
    @Test
    fun `single foundation thing constructs from entity`() {
        val entity =
            Entity(
                "1",
                mutableMapOf(
                    "type" to AttributeValueRecord("FOUNDATION", "time1"),
                    "isDeleted" to AttributeValueRecord("false", "time2"),
                ),
            )

        val thing = Thing(entity)
        assertThat(thing.type.name).isEqualTo("FOUNDATION")
        assertThat(thing.isDeleted).isFalse()
        assertThat(thing.lastModifiedAtTimestamp).isEqualTo("time2")
    }

    @Test
    fun `single foundation thing with extra attributes throws`() {
        val entity =
            Entity(
                "1",
                mutableMapOf(
                    "type" to AttributeValueRecord("FOUNDATION", "time1"),
                    "isDeleted" to AttributeValueRecord("false", "time2"),
                    "extra" to AttributeValueRecord("wooohoo", "time3"),
                    "extra2" to AttributeValueRecord("wooohoo", "time3"),
                ),
            )

        assertFailure {
            Thing(entity)
        }.hasMessage("thing has unknown attributes: `extra`, `extra2`")
    }

    @Test
    fun `throws if required fields not there`() {
        val entity =
            Entity(
                "1",
                mutableMapOf(
                    "isDeleted" to AttributeValueRecord("false", "time2"),
                ),
            )

        assertFailure {
            Thing(entity)
        }.hasMessage("thing lacks a required attribute `type`")
    }
}
