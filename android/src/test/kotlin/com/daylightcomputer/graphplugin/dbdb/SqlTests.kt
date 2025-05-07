package com.daylightcomputer.graphplugin.dbdb

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import org.junit.Test


class SqlTests {
    @Test
    fun sample() = testing { db ->
        assertThat(db.eventsQueries.getEvents().executeAsList()).isEmpty()

        db.eventsQueries.insertEvent("ab", "c", "d", "a","a")
        assertThat(
            db.eventsQueries.getEvents().executeAsList()
        ).isEqualTo(listOf(Events("ab", "c", "d", "a","a")))
    }
}
