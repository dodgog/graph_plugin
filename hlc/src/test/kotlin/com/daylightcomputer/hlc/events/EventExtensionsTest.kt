package com.daylightcomputer.hlc.events

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
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

// AIUSE: Ai gen used to help generate tests

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
        HLCEnvironment.uninitialize()
    }

    // === ZERO TIMESTAMP TESTS ===
    @Test
    fun `getZeroTimestamp returns timestamp with epoch zero`() {
        val hlc = HLC(DistributedNode("node01"))
        val zeroTimestamp = hlc.getZeroTimestamp()

        val expectedTimestamp =
            Timestamp(
                LogicalTimestamp(Instant.fromEpochMilliseconds(0)),
                DistributedNode("node01"),
                Counter(0),
            )

        assertThat(
            zeroTimestamp.logicalTime.instant,
        ).isEqualTo(Instant.fromEpochMilliseconds(0))
        assertThat(
            zeroTimestamp.distributedNode.clientNodeId,
        ).isEqualTo("node01")
        assertThat(zeroTimestamp.counter.value).isEqualTo(0)
        assertThat(zeroTimestamp).isEqualTo(expectedTimestamp)
    }

    @Test
    fun `getZeroTimestampPacked returns encoded timestamp with epoch zero`() {
        val hlc = HLC(DistributedNode("node01"))
        val packedZeroTimestamp = hlc.getZeroTimestampPacked()

        val expectedTimestamp =
            Timestamp(
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

        assertThat(
            decodedTimestamp.logicalTime.instant,
        ).isEqualTo(Instant.fromEpochMilliseconds(0))
        assertThat(
            decodedTimestamp.distributedNode.clientNodeId,
        ).isEqualTo("node01")
        assertThat(decodedTimestamp.counter.value).isEqualTo(0)
    }

    @Test
    fun `getZeroTimestamp with different nodes yields different timestamps`() {
        val hlc1 = HLC(DistributedNode("node01"))
        val hlc2 = HLC(DistributedNode("node02"))

        val zeroTimestamp1 = hlc1.getZeroTimestamp()
        val zeroTimestamp2 = hlc2.getZeroTimestamp()

        assertThat(
            zeroTimestamp1.distributedNode.clientNodeId,
        ).isEqualTo("node01")

        assertThat(
            zeroTimestamp2.distributedNode.clientNodeId,
        ).isEqualTo("node02")

        assertThat(
            zeroTimestamp1.logicalTime.instant,
        ).isEqualTo(zeroTimestamp2.logicalTime.instant)

        assertThat(
            zeroTimestamp1.counter.value,
        ).isEqualTo(zeroTimestamp2.counter.value)
    }

    // === ISSUE LOCAL EVENT TESTS ===
    @Test
    fun `issueLocalEvent returns unpacked timestamp`() {
        val hlc = HLC(DistributedNode("node01"))
        val timestamp = hlc.issueLocalEvent()

        assertThat(timestamp.logicalTime.instant).isEqualTo(fixedTime)
        assertThat(timestamp.distributedNode.clientNodeId).isEqualTo("node01")
        assertThat(timestamp.counter.value).isEqualTo(0)
    }

    @Test
    fun `issueLocalEventPacked returns packed timestamp string`() {
        val hlc = HLC(DistributedNode("node01"))
        val packedTimestamp = hlc.issueLocalEventPacked()

        // Verify it's a valid packed timestamp by decoding it
        val decodedTimestamp = Timestamp.fromEncoded(packedTimestamp)
        assertThat(decodedTimestamp.logicalTime.instant).isEqualTo(fixedTime)
        assertThat(
            decodedTimestamp.distributedNode.clientNodeId,
        ).isEqualTo("node01")
        assertThat(decodedTimestamp.counter.value).isEqualTo(0)
    }

    @Test
    fun `issueLocalEventPacked round trip correctness`() {
        val hlc = HLC(DistributedNode("node01"))
        val originalPacked = hlc.issueLocalEventPacked()
        val decoded = Timestamp.fromEncoded(originalPacked)
        val reencoded = decoded.encode()

        assertThat(reencoded).isEqualTo(originalPacked)
    }

    // === SEND TESTS ===
    @Test
    fun `send returns unpacked timestamp`() {
        val hlc = HLC(DistributedNode("node01"))
        val timestamp = hlc.send()

        assertThat(timestamp.logicalTime.instant).isEqualTo(fixedTime)
        assertThat(timestamp.distributedNode.clientNodeId).isEqualTo("node01")
        assertThat(timestamp.counter.value).isEqualTo(0)
    }

    @Test
    fun `sendPacked returns packed timestamp string`() {
        val hlc = HLC(DistributedNode("node01"))
        val packedTimestamp = hlc.sendPacked()

        // Verify it's a valid packed timestamp by decoding it
        val decodedTimestamp = Timestamp.fromEncoded(packedTimestamp)
        assertThat(decodedTimestamp.logicalTime.instant).isEqualTo(fixedTime)
        assertThat(
            decodedTimestamp.distributedNode.clientNodeId,
        ).isEqualTo("node01")
        assertThat(decodedTimestamp.counter.value).isEqualTo(0)
    }

    @Test
    fun `sendPacked maintains round trip correctness`() {
        val hlc = HLC(DistributedNode("node01"))
        val originalPacked = hlc.sendPacked()
        val decoded = Timestamp.fromEncoded(originalPacked)
        val reencoded = decoded.encode()

        assertThat(reencoded).isEqualTo(originalPacked)
    }

    @Test
    fun `sendPacked is equivalent to send encode`() {
        val hlc = HLC(DistributedNode("node01"))
        val sendPacked = hlc.sendPacked()
        val sendThenEncode = hlc.send().encode()

        // Note: These won't be equal since each call advances the counter
        // Instead verify they both decode to valid timestamps with sequential counters
        val decodedSendPacked = Timestamp.fromEncoded(sendPacked)
        val decodedSendThenEncode = Timestamp.fromEncoded(sendThenEncode)

        assertThat(
            decodedSendPacked.logicalTime,
        ).isEqualTo(decodedSendThenEncode.logicalTime)
        assertThat(
            decodedSendPacked.distributedNode,
        ).isEqualTo(decodedSendThenEncode.distributedNode)
        assertThat(
            decodedSendThenEncode.counter.value,
        ).isEqualTo(decodedSendPacked.counter.value + 1)
    }

    @Test
    fun `multiple sendPacked calls increment counter`() {
        val hlc = HLC(DistributedNode("node01"))
        val packed1 = hlc.sendPacked()
        val packed2 = hlc.sendPacked()
        val packed3 = hlc.sendPacked()

        val timestamp1 = Timestamp.fromEncoded(packed1)
        val timestamp2 = Timestamp.fromEncoded(packed2)
        val timestamp3 = Timestamp.fromEncoded(packed3)

        assertThat(timestamp1.counter.value).isEqualTo(0)
        assertThat(timestamp2.counter.value).isEqualTo(1)
        assertThat(timestamp3.counter.value).isEqualTo(2)
    }

    // === RECEIVE TESTS ===
    @Test
    fun `receivePacked correctly decodes and processes timestamp`() {
        val hlc1 = HLC(DistributedNode("node01"))
        val hlc2 = HLC(DistributedNode("node02"))

        // Node1 issues an event
        val timestamp1 = hlc1.issueLocalEvent()
        val packedTimestamp = timestamp1.encode()

        // Node2 receives the packed timestamp
        val receivedTimestamp = hlc2.receivePacked(packedTimestamp)

        // Verify the received timestamp is processed correctly
        assertThat(
            receivedTimestamp.distributedNode.clientNodeId,
        ).isEqualTo("node02")
        assertThat(receivedTimestamp.logicalTime.instant).isEqualTo(fixedTime)
        // Counter should be incremented due to receive operation
        assertThat(receivedTimestamp.counter.value).isEqualTo(1)
    }

    @Test
    fun `receivePacked maintains round trip correctness with sender`() {
        val hlc1 = HLC(DistributedNode("node01"))
        val hlc2 = HLC(DistributedNode("node02"))

        // Node1 sends
        val sentPacked = hlc1.sendPacked()

        // Node2 receives
        val receivedTimestamp = hlc2.receivePacked(sentPacked)

        // Verify the logical time propagated correctly
        val sentTimestamp = Timestamp.fromEncoded(sentPacked)
        assertThat(
            receivedTimestamp.logicalTime,
        ).isEqualTo(sentTimestamp.logicalTime)

        // Verify node ID updated to receiver
        assertThat(
            receivedTimestamp.distributedNode.clientNodeId,
        ).isEqualTo("node02")

        // Counter should be incremented due to same logical time
        assertThat(
            receivedTimestamp.counter.value,
        ).isEqualTo(sentTimestamp.counter.value + 1)
    }

    // === RECEIVE PACKED AND REPACK TESTS ===
    @Test
    fun `receivePackedAndRepack maintains round trip correctness`() {
        val hlc1 = HLC(DistributedNode("node01"))
        val hlc2 = HLC(DistributedNode("node02"))

        // Node1 issues an event
        val originalPacked = hlc1.issueLocalEventPacked()

        // Node2 receives and repacks
        val repackedTimestamp = hlc2.receivePackedAndRepack(originalPacked)

        // Decode the repacked timestamp to verify correctness
        val decodedRepacked = Timestamp.fromEncoded(repackedTimestamp)

        assertThat(
            decodedRepacked.distributedNode.clientNodeId,
        ).isEqualTo("node02")
        assertThat(decodedRepacked.logicalTime.instant).isEqualTo(fixedTime)
        assertThat(decodedRepacked.counter.value).isEqualTo(1)

        // Verify the repacked timestamp can be decoded successfully
        val finalDecoded = Timestamp.fromEncoded(repackedTimestamp)
        assertThat(finalDecoded).isEqualTo(decodedRepacked)
    }

    @Test
    fun `receivePackedAndRepack is equivalent to receivePacked encode`() {
        val hlc1 = HLC(DistributedNode("node01"))
        val hlc2 = HLC(DistributedNode("node02"))
        val hlc3 = HLC(DistributedNode("node02"))

        val originalPacked = hlc1.sendPacked()

        // Method 1: receivePackedAndRepack
        val repackedDirect = hlc2.receivePackedAndRepack(originalPacked)

        // Method 2: receivePacked then encode
        val receivedThenEncoded = hlc3.receivePacked(originalPacked).encode()

        // Both should be identical
        assertThat(repackedDirect).isEqualTo(receivedThenEncoded)
    }

    @Test
    fun `receivePackedAndRepack handles logical time advancement correctly`() {
        val hlc1 = HLC(DistributedNode("node01"))
        val hlc2 = HLC(DistributedNode("node02"))

        // Node1 generates an event
        val originalPacked = hlc1.sendPacked()

        // Node2 receives and repacks
        val repackedTimestamp = hlc2.receivePackedAndRepack(originalPacked)

        // Decode both to compare
        val originalDecoded = Timestamp.fromEncoded(originalPacked)
        val repackedDecoded = Timestamp.fromEncoded(repackedTimestamp)

        // Logical time should be preserved
        assertThat(
            repackedDecoded.logicalTime,
        ).isEqualTo(originalDecoded.logicalTime)

        // Node should be updated
        assertThat(
            repackedDecoded.distributedNode.clientNodeId,
        ).isEqualTo("node02")

        // Counter should be incremented due to same logical time
        assertThat(
            repackedDecoded.counter.value,
        ).isEqualTo(originalDecoded.counter.value + 1)
    }

    @Test
    fun `receivePackedAndRepack handles multiple hops correctly`() {
        val hlc1 = HLC(DistributedNode("node01"))
        val hlc2 = HLC(DistributedNode("node02"))
        val hlc3 = HLC(DistributedNode("node03"))

        // Node1 -> Node2 -> Node3
        val originalPacked = hlc1.sendPacked()
        val hop1Packed = hlc2.receivePackedAndRepack(originalPacked)
        val hop2Packed = hlc3.receivePackedAndRepack(hop1Packed)

        val originalDecoded = Timestamp.fromEncoded(originalPacked)
        val hop1Decoded = Timestamp.fromEncoded(hop1Packed)
        val hop2Decoded = Timestamp.fromEncoded(hop2Packed)

        // All should have same logical time
        assertThat(
            hop1Decoded.logicalTime,
        ).isEqualTo(originalDecoded.logicalTime)
        assertThat(
            hop2Decoded.logicalTime,
        ).isEqualTo(originalDecoded.logicalTime)

        // Nodes should update appropriately
        assertThat(hop1Decoded.distributedNode.clientNodeId).isEqualTo("node02")
        assertThat(hop2Decoded.distributedNode.clientNodeId).isEqualTo("node03")

        // Counters should increment at each hop
        assertThat(
            hop1Decoded.counter.value,
        ).isEqualTo(originalDecoded.counter.value + 1)
        assertThat(
            hop2Decoded.counter.value,
        ).isEqualTo(originalDecoded.counter.value + 2)
    }

    @Test
    fun `receivePackedAndRepack double encode decode correctness`() {
        val hlc1 = HLC(DistributedNode("node01"))
        val hlc2 = HLC(DistributedNode("node02"))

        val originalPacked = hlc1.sendPacked()
        val repackedTimestamp = hlc2.receivePackedAndRepack(originalPacked)

        // Decode and re-encode to verify stability
        val decodedRepacked = Timestamp.fromEncoded(repackedTimestamp)
        val reReencoded = decodedRepacked.encode()

        assertThat(reReencoded).isEqualTo(repackedTimestamp)
    }

    @Test
    fun `complex distributed scenario with all packing methods`() {
        val hlc1 = HLC(DistributedNode("node01"))
        val hlc2 = HLC(DistributedNode("node02"))
        val hlc3 = HLC(DistributedNode("node03"))

        // Node1 generates local event
        val event1 = hlc1.issueLocalEventPacked()

        // Node1 sends to Node2
        val sent12 = hlc1.sendPacked()
        val received12 = hlc2.receivePackedAndRepack(sent12)

        // Node2 sends to Node3
        val sent23 = hlc2.sendPacked()
        val received23 = hlc3.receivePacked(sent23)

        // Node3 sends back to Node1
        val sent31 = hlc3.sendPacked()
        val received31 = hlc1.receivePackedAndRepack(sent31)

        // Verify all are decodable
        val event1Decoded = Timestamp.fromEncoded(event1)
        val received12Decoded = Timestamp.fromEncoded(received12)
        val received31Decoded = Timestamp.fromEncoded(received31)

        // Verify logical ordering
        assertThat(event1Decoded).isLessThan(received12Decoded)
        assertThat(received12Decoded).isLessThan(received23)
        assertThat(received23).isLessThan(received31Decoded)
    }

    @Test
    fun `packing methods handle counter overflow protection`() {
        // Create HLC with small counter to test near-overflow
        HLCEnvironment.uninitialize()
        HLCEnvironment.initialize(
            HLCConfig(
                getPhysicalTime = { fixedTime },
                counterLength = 1, // Max value is 15 (0xF)
            ),
        )

        val hlc = HLC(DistributedNode("node01"))

        // Generate events up to near max counter
        repeat(14) {
            hlc.sendPacked()
        }

        // This should work (counter = 14)
        val nearMaxPacked = hlc.sendPacked()
        val nearMaxDecoded = Timestamp.fromEncoded(nearMaxPacked)
        assertThat(nearMaxDecoded.counter.value).isEqualTo(14)

        // This should also work (counter = 15, which is max for 1 hex digit)
        val maxPacked = hlc.sendPacked()
        val maxDecoded = Timestamp.fromEncoded(maxPacked)
        assertThat(maxDecoded.counter.value).isEqualTo(15)
    }

    @Test
    fun `all packing methods produce valid encoded timestamps`() {
        val hlc = HLC(DistributedNode("node01"))

        val methods =
            listOf(
                { hlc.issueLocalEventPacked() },
                { hlc.sendPacked() },
                { hlc.getZeroTimestampPacked() },
                { hlc.receivePackedAndRepack(hlc.sendPacked()) },
            )

        methods.forEach { method ->
            val packed = method()
            // Should not throw exception
            val decoded = Timestamp.fromEncoded(packed)
            val reencoded = decoded.encode()
            assertThat(reencoded).isEqualTo(packed)
        }
    }
}
