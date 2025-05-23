package com.daylightcomputer.hlc.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.HLCConfig
import java.time.Instant

// AIUSE: AI generated tests

class LogicalTimestampTest {
    private val fixedTime = Instant.parse("2024-01-01T00:00:00.123456Z")

    @BeforeEach
    fun setup() {
        HLCEnvironment.resetForTests()
        HLCEnvironment.initialize(
            HLCConfig(
                numberOfCharactersInLogicalTimestampRepresentation = 27
            )
        )
    }

    @Test
    fun `LogicalTimestamp initialization works`() {
        val timestamp = LogicalTimestamp(fixedTime)
        assertThat(timestamp.instant).isEqualTo(fixedTime)
    }

    @Test
    fun `LogicalTimestamp fromMillis works`() {
        val timestamp = LogicalTimestamp.fromMillis(fixedTime.toEpochMilli())
        assertThat(timestamp.millis).isEqualTo(fixedTime.toEpochMilli())
    }

    @Test
    fun `LogicalTimestamp comparison works correctly`() {
        val earlier = LogicalTimestamp(fixedTime)
        val later = LogicalTimestamp(fixedTime.plusMillis(1000))

        assertThat(earlier).isLessThan(later)
        assertThat(earlier).isEqualTo(LogicalTimestamp(fixedTime))
    }

    @Test
    fun `LogicalTimestamp absDifferenceInMillis works`() {
        val t1 = LogicalTimestamp(fixedTime)
        val t2 = LogicalTimestamp(fixedTime.plusMillis(1000))
        val t3 = LogicalTimestamp(fixedTime.minusMillis(1000))

        assertThat(t1.absDifferenceInMillis(t2)).isEqualTo(1000)
        assertThat(t1.absDifferenceInMillis(t3)).isEqualTo(1000)
    }

    @Test
    fun `LogicalTimestamp packing and unpacking works`() {
        val original = LogicalTimestamp(fixedTime)
        val packed = original.pack()
        val unpacked = LogicalTimestamp.fromPacked(packed)

        assertThat(original.instant).isEqualTo(unpacked.instant)
    }

    @Test
    fun `LogicalTimestamp packing maintains correct length`() {
        val timestamp = LogicalTimestamp(fixedTime)
        assertThat(timestamp.pack().length).isEqualTo(27)
    }

    @Test
    fun `LogicalTimestamp packing produces correct ISO format`() {
        val timestamp = LogicalTimestamp(fixedTime)
        assertThat(timestamp.pack()).isEqualTo("2024-01-01T00:00:00.123456Z")
    }
} 