package com.daylightcomputer.hlc

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import com.daylightcomputer.hlc.events.issueLocalEvent
import com.daylightcomputer.hlc.events.issueLocalEventPacked
import com.daylightcomputer.hlc.events.receivePackedAndRepack
import com.daylightcomputer.hlc.events.send
import com.daylightcomputer.hlc.exceptions.ClockDriftException
import com.daylightcomputer.hlc.exceptions.CounterOverflowException
import com.daylightcomputer.hlc.model.Counter
import com.daylightcomputer.hlc.model.DistributedNode
import com.daylightcomputer.hlc.model.LogicalTimestamp
import com.daylightcomputer.hlc.model.Timestamp
import kotlinx.datetime.Clock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

// AIUSE: tests handwritten in dart, converted to kotlin with AI

class HLCTest {
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
    fun `ClientNode comparison works correctly`() {
        val node1 = DistributedNode("node01")
        val node2 = DistributedNode("node02")
        val node1Duplicate = DistributedNode("node01")

        assertThat(node1).isLessThan(node2)
        assertThat(node1).isEqualTo(node1Duplicate)
    }

    @Test
    fun `ClientNode packing and unpacking works`() {
        val original = DistributedNode("node12")
        val packed = original.encode()
        val unpacked = DistributedNode.fromEncoded(packed)

        assertThat(original.clientNodeId)
            .isEqualTo(unpacked.clientNodeId)
    }

    @Test
    fun `Timestamp comparison works correctly`() {
        val now = LogicalTimestamp(fixedTime)
        val later =
            LogicalTimestamp
                .fromMillisForTests(now.millisForTests + 1000)

        val timestamp1 =
            Timestamp(
                now,
                DistributedNode("node01"),
                Counter(0),
            )
        val timestamp2 =
            Timestamp(
                later,
                DistributedNode("node01"),
                Counter(0),
            )
        val timestamp3 =
            Timestamp(
                now,
                DistributedNode("node01"),
                Counter(1),
            )
        val timestamp4 =
            Timestamp(
                now,
                DistributedNode("node02"),
                Counter(0),
            )

        assertThat(timestamp1).isLessThan(timestamp2)
        assertThat(timestamp1).isLessThan(timestamp3)
        assertThat(timestamp1).isLessThan(timestamp4)
    }

    @Test
    fun `Local event generation works`() {
        val hlc = HLC(DistributedNode("node01"))
        val timestamp = hlc.issueLocalEvent()

        assertThat(timestamp.logicalTime.instant).isEqualTo(fixedTime)
        assertThat(timestamp.counter.value).isEqualTo(0)
        assertThat(timestamp.distributedNode.clientNodeId)
            .isEqualTo("node01")
    }

    @Test
    fun `Send operation works`() {
        val hlc = HLC(DistributedNode("node01"))
        val timestamp = hlc.send()

        assertThat(timestamp.logicalTime.instant).isEqualTo(fixedTime)
        assertThat(timestamp.counter.value).isEqualTo(0)
    }

    @Test
    fun `Local event with physical time - logical time sets counter to 0`() {
        val pastTime =
            LogicalTimestamp
                .fromMillisForTests(fixedTime.toEpochMilliseconds() - 1000)
        val hlc =
            HLC(
                DistributedNode("node01"),
                Timestamp(
                    pastTime,
                    DistributedNode("node01"),
                    Counter(5),
                ),
            )

        val result = hlc.send()
        assertThat(result.logicalTime.instant).isEqualTo(fixedTime)
        assertThat(result.counter.value).isEqualTo(0)
    }

    @Test
    fun `Local event with logical time - physical time increments counter`() {
        val futureTime =
            LogicalTimestamp
                .fromMillisForTests(fixedTime.toEpochMilliseconds() + 1000)
        val hlc =
            HLC(
                DistributedNode("node01"),
                Timestamp(
                    futureTime,
                    DistributedNode("node01"),
                    Counter(5),
                ),
            )

        val result = hlc.send()
        assertThat(result.logicalTime).isEqualTo(futureTime)
        assertThat(result.counter.value).isEqualTo(6)
    }

    @Test
    fun `Receive local counter greater sets to local counter + 1`() {
        val futureTime =
            LogicalTimestamp
                .fromMillisForTests(fixedTime.toEpochMilliseconds() + 1000)
        val hlc =
            HLC(
                DistributedNode("node01"),
                Timestamp(
                    futureTime,
                    DistributedNode("node01"),
                    Counter(5),
                ),
            )

        val incoming =
            Timestamp(
                futureTime,
                DistributedNode("node02"),
                Counter(3),
            )
        val result = hlc.receive(incoming)
        assertThat(result.logicalTime).isEqualTo(futureTime)
        assertThat(result.counter.value).isEqualTo(6)
    }

    @Test
    fun `Receive incoming counter greater sets to incoming counter + 1`() {
        val futureTime =
            LogicalTimestamp
                .fromMillisForTests(fixedTime.toEpochMilliseconds() + 1000)
        val hlc =
            HLC(
                DistributedNode("node01"),
                Timestamp(
                    futureTime,
                    DistributedNode("node01"),
                    Counter(3),
                ),
            )

        val incoming =
            Timestamp(
                futureTime,
                DistributedNode("node02"),
                Counter(5),
            )
        val result = hlc.receive(incoming)
        assertThat(result.logicalTime).isEqualTo(futureTime)
        assertThat(result.counter.value).isEqualTo(6)
    }

    @Test
    fun `Receive with local logical gr than phys and incoming incr local`() {
        val futureTime =
            LogicalTimestamp
                .fromMillisForTests(fixedTime.toEpochMilliseconds() + 2000)
        val incomingTime =
            LogicalTimestamp
                .fromMillisForTests(fixedTime.toEpochMilliseconds() + 1000)
        val hlc =
            HLC(
                DistributedNode("node01"),
                Timestamp(
                    futureTime,
                    DistributedNode("node01"),
                    Counter(5),
                ),
            )

        val incoming =
            Timestamp(
                incomingTime,
                DistributedNode("node02"),
                Counter(3),
            )
        val result = hlc.receive(incoming)
        assertThat(result.logicalTime).isEqualTo(futureTime)
        assertThat(result.counter.value).isEqualTo(6)
    }

    @Test
    fun `Receive with incoming logical gr than phys and local incr incoming`() {
        val incomingTime =
            LogicalTimestamp
                .fromMillisForTests(fixedTime.toEpochMilliseconds() + 2000)
        val localTime =
            LogicalTimestamp
                .fromMillisForTests(fixedTime.toEpochMilliseconds() + 1000)
        val hlc =
            HLC(
                DistributedNode("node01"),
                Timestamp(
                    localTime,
                    DistributedNode("node01"),
                    Counter(5),
                ),
            )

        val incoming =
            Timestamp(
                incomingTime,
                DistributedNode("node02"),
                Counter(3),
            )
        val result = hlc.receive(incoming)
        assertThat(result.logicalTime).isEqualTo(incomingTime)
        assertThat(result.counter.value).isEqualTo(4)
    }

    @Test
    fun `Receive with phys gr than both logical times resets counter`() {
        val pastLocalTime =
            LogicalTimestamp
                .fromMillisForTests(fixedTime.toEpochMilliseconds() - 2000)
        val pastIncomingTime =
            LogicalTimestamp
                .fromMillisForTests(fixedTime.toEpochMilliseconds() - 1000)
        val hlc =
            HLC(
                DistributedNode("node01"),
                Timestamp(
                    pastLocalTime,
                    DistributedNode("node01"),
                    Counter(5),
                ),
            )

        val incoming =
            Timestamp(
                pastIncomingTime,
                DistributedNode("node02"),
                Counter(3),
            )
        val result = hlc.receive(incoming)
        assertThat(result.logicalTime.instant).isEqualTo(fixedTime)
        assertThat(result.counter.value).isEqualTo(0)
    }

    @Test
    fun `Counter overflow throws exception`() {
        HLCEnvironment.resetForTests()
        HLCEnvironment.initialize(
            HLCConfig(
                getPhysicalTime = { fixedTime },
                counterLength = 1,
            ),
        )

        val hlc =
            HLC(
                DistributedNode("node01"),
                previousTimestamp =
                    Timestamp(
                        LogicalTimestamp(fixedTime),
                        DistributedNode("node01"),
                        Counter(15),
                    ),
            )

        val overflowCounter =
            Timestamp(
                LogicalTimestamp(fixedTime),
                DistributedNode("node01"),
                Counter(15),
            )
        assertFailure {
            hlc.receive(overflowCounter)
        }.hasClass(CounterOverflowException::class.java)
    }

    @Test
    fun `Clock drift detection works`() {
        val farFuture =
            LogicalTimestamp.fromMillisForTests(
                fixedTime.toEpochMilliseconds() + 7200000,
            ) // 2 hours ahead
        val driftedTimestamp =
            Timestamp(
                farFuture,
                DistributedNode("node01"),
                Counter(0),
            )

        assertFailure {
            HLC(DistributedNode("node01")).receive(driftedTimestamp)
        }.hasClass(ClockDriftException::class.java)
    }

    @Test
    fun `Simulated distributed event ordering`() {
        val hlc = HLC(DistributedNode("node01"))
        val event1 = hlc.issueLocalEventPacked()

        val receivedEvent =
            hlc.receivePackedAndRepack(
                "${LogicalTimestamp.now().encode()}-0001-node02",
            )

        val event2 = hlc.issueLocalEventPacked()

        assertThat(event1).isLessThan(receivedEvent)
        assertThat(receivedEvent).isLessThan(event2)
    }
}
