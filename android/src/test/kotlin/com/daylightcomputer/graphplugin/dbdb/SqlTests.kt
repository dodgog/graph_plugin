package com.daylightcomputer.graphplugin.dbdb

import org.junit.Assert.assertThat
import org.junit.Test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo


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
