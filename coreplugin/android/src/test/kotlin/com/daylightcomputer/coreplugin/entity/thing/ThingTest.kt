package com.daylightcomputer.coreplugin.entity.thing

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.daylightcomputer.coreplugin.entity.AttributeValueRecord
import com.daylightcomputer.coreplugin.entity.Entity
import com.daylightcomputer.coreplugin.entity.TimestampProvider
import org.junit.Before
import org.junit.Test

class ThingTest {
    private var counter: Int = 0
    private val timestampProvider = TimestampProvider { "timestamp-${++counter}" }

    @Before
    fun setUp() {
        counter = 0
    }

    @Test
    fun `single base thing constructs from entity`() {
        val entity =
            Entity(
                "1",
                mutableMapOf(
                    "type" to AttributeValueRecord("BASE", timestampProvider.issueTimestamp()),
                    "isDeleted" to
                        AttributeValueRecord(
                            "false",
                            timestampProvider.issueTimestamp(),
                        ),
                ),
                timestampProvider,
            )

        val thing = Thing(entity)
        assertThat(thing.type.name).isEqualTo("BASE")
        assertThat(thing.isDeleted).isFalse()
        assertThat(thing.lastModifiedAtTimestamp).isEqualTo("timestamp-2")
    }

    // TODO: check extra attributes perhaps
//    @Test
//    fun `single base thing with extra attributes throws`() {
//        val entity =
//            Entity(
//                "1",
//                mutableMapOf(
//                    "type" to AttributeValueRecord("BASE", "time1"),
//                    "isDeleted" to AttributeValueRecord("false", "time2"),
//                    "extra" to AttributeValueRecord("wooohoo", "time3"),
//                    "extra2" to AttributeValueRecord("wooohoo", "time3"),
//                ),
//            )
//
//        assertFailure {
//            Thing(entity)
//        }.hasMessage("thing has unknown attributes: `extra`, `extra2`")
//    }

    @Test
    fun `throws if required fields not there`() {
        val entity =
            Entity(
                "1",
                mutableMapOf(
                    "isDeleted" to
                        AttributeValueRecord(
                            "false",
                            timestampProvider.issueTimestamp(),
                        ),
                ),
                timestampProvider,
            )

        assertFailure {
            Thing(entity)
        }.hasMessage("Entity lacks a required attribute `type`")
    }

    @Test
    fun `throws if type field is encoded as null`() {
        val entity =
            Entity(
                "1",
                mutableMapOf(
                    "type" to AttributeValueRecord(null, timestampProvider.issueTimestamp()),
                    "isDeleted" to
                        AttributeValueRecord(
                            "false",
                            timestampProvider.issueTimestamp(),
                        ),
                ),
                timestampProvider,
            )

        assertFailure {
            Thing(entity)
        }.hasMessage("Type encoding cannot be null")
    }
}
