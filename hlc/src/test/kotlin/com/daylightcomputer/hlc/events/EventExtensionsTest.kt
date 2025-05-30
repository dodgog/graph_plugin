package com.daylightcomputer.hlc.events

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.daylightcomputer.hlc.HLC
import com.daylightcomputer.hlc.HLCConfig
import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.model.Counter
import com.daylightcomputer.hlc.model.DistributedNode
import com.daylightcomputer.hlc.model.LogicalTimestamp
import com.daylightcomputer.hlc.model.Timestamp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/* AIUSE: Ai gen used to help generate tests */

class EventExtensionsTest {
    private val fixedTime = Clock.System.now()

    @BeforeEach
    fun setup() {
        HLCEnvironment.initialize(
            HLCConfig(
                getPhysicalTime = { fixedTime },
            ),
        )
    }

    @AfterEach
    fun tearDown() {
        HLCEnvironment.resetForTests()
    }

    @Test
    fun `getZeroTimestamp returns timestamp with epoch zero`() {
        val hlc = HLC(DistributedNode("node01"))
        val zeroTimestamp = hlc.getZeroTimestamp()

        val expectedTimestamp = Timestamp(
            LogicalTimestamp(Instant.fromEpochMilliseconds(0)),
            DistributedNode("node01"),
            Counter(0),
        )

        assertThat(zeroTimestamp.logicalTime.instant).isEqualTo(Instant.fromEpochMilliseconds(0))
        assertThat(zeroTimestamp.distributedNode.clientNodeId).isEqualTo("node01")
        assertThat(zeroTimestamp.counter.value).isEqualTo(0)
        assertThat(zeroTimestamp).isEqualTo(expectedTimestamp)
    }

    @Test
    fun `getZeroTimestampPacked returns encoded timestamp with epoch zero`() {
        val hlc = HLC(DistributedNode("node01"))
        val packedZeroTimestamp = hlc.getZeroTimestampPacked()

        val expectedTimestamp = Timestamp(
            LogicalTimestamp(Instant.fromEpochMilliseconds(0)),
            DistributedNode("node01"),
            Counter(0),
        )
        val expectedPacked = expectedTimestamp.encode()

        assertThat(packedZeroTimestamp).isEqualTo(expectedPacked)
    }

    @Test
    fun `getZeroTimestampPacked can be decoded back to original timestamp`() {
        val hlc = HLC(DistributedNode("node01"))
        val packedZeroTimestamp = hlc.getZeroTimestampPacked()
        val decodedTimestamp = Timestamp.fromEncoded(packedZeroTimestamp)

        assertThat(decodedTimestamp.logicalTime.instant).isEqualTo(Instant.fromEpochMilliseconds(0))
        assertThat(decodedTimestamp.distributedNode.clientNodeId).isEqualTo("node01")
        assertThat(decodedTimestamp.counter.value).isEqualTo(0)
    }

    @Test
    fun `getZeroTimestamp with different node IDs produces different timestamps`() {
        val hlc1 = HLC(DistributedNode("node01"))
        val hlc2 = HLC(DistributedNode("node02"))

        val zeroTimestamp1 = hlc1.getZeroTimestamp()
        val zeroTimestamp2 = hlc2.getZeroTimestamp()

        assertThat(zeroTimestamp1.distributedNode.clientNodeId).isEqualTo("node01")
        assertThat(zeroTimestamp2.distributedNode.clientNodeId).isEqualTo("node02")
        assertThat(zeroTimestamp1.logicalTime.instant).isEqualTo(zeroTimestamp2.logicalTime.instant)
        assertThat(zeroTimestamp1.counter.value).isEqualTo(zeroTimestamp2.counter.value)
    }
}
