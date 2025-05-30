package com.daylightcomputer.hlc.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import com.daylightcomputer.hlc.HLCConfig
import com.daylightcomputer.hlc.HLCEnvironment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlinx.datetime.Instant

// AIUSE: AI generated tests

class LogicalTimestampTest {
    private val fixedTime = Instant.parse("2024-01-01T00:00:00.123456Z")

    @BeforeEach
    fun setup() {
        HLCEnvironment.resetForTests()
        HLCEnvironment.initialize(
            HLCConfig(
                logicalTimestampLength = 27,
            ),
        )
    }

    @Test
    fun `LogicalTimestamp initialization works`() {
        val timestamp = LogicalTimestamp(fixedTime)
        assertThat(timestamp.instant).isEqualTo(fixedTime)
    }

    @Test
    fun `LogicalTimestamp fromMillis works`() {
        val timestamp =
            LogicalTimestamp.fromMillisForTests(
                fixedTime.toEpochMilliseconds(),
            )
        assertThat(timestamp.millisForTests).isEqualTo(fixedTime.toEpochMilliseconds())
    }

    @Test
    fun `LogicalTimestamp comparison works correctly`() {
        val earlier = LogicalTimestamp(fixedTime)
        val later = LogicalTimestamp(Instant.fromEpochMilliseconds(fixedTime.toEpochMilliseconds() + 1000))

        assertThat(earlier).isLessThan(later)
        assertThat(earlier).isEqualTo(LogicalTimestamp(fixedTime))
    }

    @Test
    fun `LogicalTimestamp absDifferenceInMillis works`() {
        val t1 = LogicalTimestamp(fixedTime)
        val t2 = LogicalTimestamp(Instant.fromEpochMilliseconds(fixedTime.toEpochMilliseconds() + 1000))
        val t3 = LogicalTimestamp(Instant.fromEpochMilliseconds(fixedTime.toEpochMilliseconds() - 1000))

        assertThat(t1.absDifferenceInMillis(t2)).isEqualTo(1000)
        assertThat(t1.absDifferenceInMillis(t3)).isEqualTo(1000)
    }

    @Test
    fun `LogicalTimestamp packing and unpacking works`() {
        val original = LogicalTimestamp(fixedTime)
        val packed = original.encode()
        val unpacked = LogicalTimestamp.fromEncoded(packed)

        assertThat(original.instant).isEqualTo(unpacked.instant)
    }

    @Test
    fun `LogicalTimestamp packing maintains correct length`() {
        val timestamp = LogicalTimestamp(fixedTime)
        assertThat(timestamp.encode().length).isEqualTo(27)
    }

    @Test
    fun `LogicalTimestamp packing produces correct ISO format`() {
        val timestamp = LogicalTimestamp(fixedTime)
        assertThat(timestamp.encode()).isEqualTo("2024-01-01T00:00:00.123456Z")
    }
}
