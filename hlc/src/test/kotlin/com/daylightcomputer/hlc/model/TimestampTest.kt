package com.daylightcomputer.hlc.model

import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.HLCConfig
import com.daylightcomputer.hlc.exceptions.TimestampFormatException
import java.time.Instant

// AIUSE: AI generated tests

class TimestampTest {
    private val fixedTime = Instant.parse("2024-01-01T00:00:00.123456Z")

    @BeforeEach
    fun setup() {
        HLCEnvironment.resetForTests()
        HLCEnvironment.initialize(
            HLCConfig(
                logicalTimestampLength = 27,
                hexCounterLength = 4,
                clientNodeLength = 6
            )
        )
    }

    @Test
    fun `Timestamp initialization works`() {
        val timestamp = Timestamp(
            LogicalTimestamp(fixedTime),
            ClientNode("node01"),
            Counter(0)
        )
        assertThat(timestamp.logicalTime.instant).isEqualTo(fixedTime)
        assertThat(timestamp.clientNode.clientNodeId).isEqualTo("node01")
        assertThat(timestamp.counter.value).isEqualTo(0)
    }

    @Test
    fun `Timestamp comparison works correctly`() {
        val t1 = Timestamp(
            LogicalTimestamp(fixedTime),
            ClientNode("node01"),
            Counter(0)
        )
        val t2 = Timestamp(
            LogicalTimestamp(fixedTime.plusMillis(1000)),
            ClientNode("node01"),
            Counter(0)
        )
        val t3 = Timestamp(
            LogicalTimestamp(fixedTime),
            ClientNode("node01"),
            Counter(1)
        )
        val t4 = Timestamp(
            LogicalTimestamp(fixedTime),
            ClientNode("node02"),
            Counter(0)
        )

        assertThat(t1).isLessThan(t2)
        assertThat(t1).isLessThan(t3)
        assertThat(t1).isLessThan(t4)
    }

    @Test
    fun `Timestamp packing and unpacking works`() {
        val original = Timestamp(
            LogicalTimestamp(fixedTime),
            ClientNode("node01"),
            Counter(0x1234)
        )
        val packed = original.pack()
        val unpacked = Timestamp.fromPacked(packed)

        assertThat(unpacked.logicalTime.instant).isEqualTo(original.logicalTime.instant)
        assertThat(unpacked.clientNode.clientNodeId).isEqualTo(original.clientNode.clientNodeId)
        assertThat(unpacked.counter.value).isEqualTo(original.counter.value)
    }

    @Test
    fun `Timestamp packing produces correct format`() {
        val timestamp = Timestamp(
            LogicalTimestamp(fixedTime),
            ClientNode("node01"),
            Counter(0x1234)
        )
        assertThat(timestamp.pack()).isEqualTo("2024-01-01T00:00:00.123456Z-1234-node01")
    }

    @Test
    fun `Timestamp unpacking with invalid format throws exception`() {
        assertk.assertFailure {
            Timestamp.fromPacked("invalid-format")
        }.hasClass(TimestampFormatException::class.java)
    }

}