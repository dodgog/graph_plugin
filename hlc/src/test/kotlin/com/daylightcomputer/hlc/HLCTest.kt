package com.daylightcomputer.hlc

import com.daylightcomputer.hlc.exceptions.ClockDriftException
import com.daylightcomputer.hlc.exceptions.CounterOverflowException
import com.daylightcomputer.hlc.model.ClientNode
import com.daylightcomputer.hlc.model.Counter
import com.daylightcomputer.hlc.model.LogicalTimestamp
import com.daylightcomputer.hlc.model.Timestamp
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import assertk.assertFailure
import assertk.assertions.hasClass

import java.time.Instant

// AIUSE: tests handwritten in dart, converted to kotlin with AI

class HLCTest {
    private val fixedTime = LogicalTimestamp.fromMillis(Instant.now().toEpochMilli())

    @BeforeEach
    fun setup() {
        HLCEnvironment.initialize(
            HLCConfig(
                getPhysicalTime = { fixedTime }
            )
        )
    }

    @AfterEach
    fun tearDown() {
        HLCEnvironment.resetForTests()
    }

    @Test
    fun `ClientNode comparison works correctly`() {
        val node1 = ClientNode("node01")
        val node2 = ClientNode("node02")
        val node1Duplicate = ClientNode("node01")

        assertThat(node1).isLessThan(node2)
        assertThat(node1).isEqualTo(node1Duplicate)
    }

    @Test
    fun `ClientNode packing and unpacking works`() {
        val original = ClientNode("node12")
        val packed = original.pack()
        val unpacked = ClientNode.fromPacked(packed)

        assertThat(original.clientNodeId).isEqualTo(unpacked.clientNodeId)
    }

    @Test
    fun `Timestamp comparison works correctly`() {
        val now = fixedTime
        val later = LogicalTimestamp.fromMillis(now.millis + 1000)

        val timestamp1 = Timestamp(now, ClientNode("node01"), Counter(0))
        val timestamp2 = Timestamp(later, ClientNode("node01"), Counter(0))
        val timestamp3 = Timestamp(now, ClientNode("node01"), Counter(1))
        val timestamp4 = Timestamp(now, ClientNode("node02"), Counter(0))

        assertThat(timestamp1).isLessThan(timestamp2)
        assertThat(timestamp1).isLessThan(timestamp3)
        assertThat(timestamp1).isLessThan(timestamp4)
    }


    @Test
    fun `Local event generation works`() {
        val hlc = HLC(ClientNode("node01"))
        val timestamp = hlc.issueLocalEvent()

        assertThat(timestamp.logicalTime).isEqualTo(fixedTime)
        assertThat(timestamp.counter.value).isEqualTo(0)
        assertThat(timestamp.clientNode.clientNodeId).isEqualTo("node01")
    }

    @Test
    fun `Send operation works`() {
        val hlc = HLC(ClientNode("node01"))
        val timestamp = hlc.send()

        assertThat(timestamp.logicalTime).isEqualTo(fixedTime)
        assertThat(timestamp.counter.value).isEqualTo(0)
    }

    @Test
    fun `Local event with physical time - logical time sets counter to 0`() {
        val pastTime = LogicalTimestamp.fromMillis(fixedTime.millis - 1000)
        val hlc = HLC(
            ClientNode("node01"),
            Timestamp(pastTime, ClientNode("node01"), Counter(5))
        )

        val result = hlc.send()
        assertThat(result.logicalTime).isEqualTo(fixedTime)
        assertThat(result.counter.value).isEqualTo(0)
    }

    @Test
    fun `Local event with logical time - physical time increments counter`() {
        val futureTime = LogicalTimestamp.fromMillis(fixedTime.millis + 1000)
        val hlc = HLC(
            ClientNode("node01"),
            Timestamp(futureTime, ClientNode("node01"), Counter(5))
        )

        val result = hlc.send()
        assertThat(result.logicalTime).isEqualTo(futureTime)
        assertThat(result.counter.value).isEqualTo(6)
    }

    @Test
    fun `Receive with same logical times local counter greater sets to local counter + 1`() {
        val futureTime = LogicalTimestamp.fromMillis(fixedTime.millis + 1000)
        val hlc = HLC(
            ClientNode("node01"),
            Timestamp(futureTime, ClientNode("node01"), Counter(5))
        )

        val incoming = Timestamp(futureTime, ClientNode("node02"), Counter(3))
        val result = hlc.receive(incoming)
        assertThat(result.logicalTime).isEqualTo(futureTime)
        assertThat(result.counter.value).isEqualTo(6)
    }

    @Test
    fun `Receive with same logical times incoming counter greater sets to incoming counter + 1`() {
        val futureTime = LogicalTimestamp.fromMillis(fixedTime.millis + 1000)
        val hlc = HLC(
            ClientNode("node01"),
            Timestamp(futureTime, ClientNode("node01"), Counter(3))
        )

        val incoming = Timestamp(futureTime, ClientNode("node02"), Counter(5))
        val result = hlc.receive(incoming)
        assertThat(result.logicalTime).isEqualTo(futureTime)
        assertThat(result.counter.value).isEqualTo(6)
    }

    @Test
    fun `Receive with local logical time greater than physical and incoming increments local`() {
        val futureTime = LogicalTimestamp.fromMillis(fixedTime.millis + 2000)
        val incomingTime = LogicalTimestamp.fromMillis(fixedTime.millis + 1000)
        val hlc = HLC(
            ClientNode("node01"),
            Timestamp(futureTime, ClientNode("node01"), Counter(5))
        )

        val incoming = Timestamp(incomingTime, ClientNode("node02"), Counter(3))
        val result = hlc.receive(incoming)
        assertThat(result.logicalTime).isEqualTo(futureTime)
        assertThat(result.counter.value).isEqualTo(6)
    }

    @Test
    fun `Receive with incoming logical time greater than physical and local increments incoming`() {
        val incomingTime = LogicalTimestamp.fromMillis(fixedTime.millis + 2000)
        val localTime = LogicalTimestamp.fromMillis(fixedTime.millis + 1000)
        val hlc = HLC(
            ClientNode("node01"),
            Timestamp(localTime, ClientNode("node01"), Counter(5))
        )

        val incoming = Timestamp(incomingTime, ClientNode("node02"), Counter(3))
        val result = hlc.receive(incoming)
        assertThat(result.logicalTime).isEqualTo(incomingTime)
        assertThat(result.counter.value).isEqualTo(4)
    }

    @Test
    fun `Receive with physical time greater than both logical times resets counter`() {
        val pastLocalTime = LogicalTimestamp.fromMillis(fixedTime.millis - 2000)
        val pastIncomingTime = LogicalTimestamp.fromMillis(fixedTime.millis - 1000)
        val hlc = HLC(
            ClientNode("node01"),
            Timestamp(pastLocalTime, ClientNode("node01"), Counter(5))
        )

        val incoming = Timestamp(pastIncomingTime, ClientNode("node02"), Counter(3))
        val result = hlc.receive(incoming)
        assertThat(result.logicalTime).isEqualTo(fixedTime)
        assertThat(result.counter.value).isEqualTo(0)
    }

    @Test
    fun `Counter overflow throws exception`() {
        HLCEnvironment.resetForTests()
        HLCEnvironment.initialize(
            HLCConfig(
                getPhysicalTime = { fixedTime },
                numberOfCharactersInCounterHexRepresentation = 1
            )
        )

        val hlc = HLC(ClientNode("node01"), previousTimestamp = Timestamp(fixedTime, ClientNode("node01"), Counter(15)))

        val overflowCounter = Timestamp(fixedTime, ClientNode("node01"), Counter(15))
        assertFailure {
            hlc.receive(overflowCounter)
        }.hasClass(CounterOverflowException::class.java)
    }

    @Test
    fun `Clock drift detection works`() {
        val farFuture = LogicalTimestamp.fromMillis(fixedTime.millis + 7200000) // 2 hours ahead
        val driftedTimestamp = Timestamp(farFuture, ClientNode("node01"), Counter(0))

        assertFailure {
            HLC(ClientNode("node01")).receive(driftedTimestamp)
        }.hasClass(ClockDriftException::class.java)
    }

    @Test
    fun `Simulated distributed event ordering`() {
        val hlc = HLC(ClientNode("node01"))
        val event1 = hlc.issueLocalEventPacked()

        val receivedEvent = hlc.receivePackedAndRepack(
            "${LogicalTimestamp.now().pack()}-0001-node02"
        )

        val event2 = hlc.issueLocalEventPacked()

        assertThat(event1).isLessThan(receivedEvent)
        assertThat(receivedEvent).isLessThan(event2)
    }
} 