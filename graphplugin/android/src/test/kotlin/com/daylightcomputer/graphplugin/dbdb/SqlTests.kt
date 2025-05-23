package com.daylightcomputer.graphplugin.dbdb

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.daylightcomputer.hlc.HLC
import com.daylightcomputer.hlc.HLCConfig
import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.model.ClientNode
import org.junit.Test


class SqlTests {
    @Test
    fun sample() = testing { db ->
        assertThat(db.eventsQueries.getEvents().executeAsList()).isEmpty()

        HLCEnvironment.initialize(HLCConfig())
        val a = HLC(ClientNode("abc123"))
        println(a.issueLocalEventPacked())

        db.eventsQueries.insertEvent("ab", "c", "d", "a","a", "s")
        assertThat(
            db.eventsQueries.getEvents().executeAsList()
        ).isEqualTo(listOf(Events("ab", "c", "d", "a","a", "s")))
    }
}
