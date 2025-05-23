package com.daylightcomputer.hlc

import com.daylightcomputer.hlc.exceptions.ClientFormatException
import com.daylightcomputer.hlc.exceptions.ClockDriftException
import com.daylightcomputer.hlc.exceptions.CounterOverflowException
import com.daylightcomputer.hlc.model.ClientNode
import com.daylightcomputer.hlc.model.Counter
import com.daylightcomputer.hlc.model.LogicalTimestamp
import com.daylightcomputer.hlc.model.Timestamp
import java.time.Instant
import kotlin.math.max

class HLC(
    val clientNode: ClientNode,
    previousTimestamp: Timestamp? = null,
) {
    private val config: HLCConfig get() = HLCEnvironment.config

    private var timestamp: Timestamp

    val currentTimestamp: Timestamp get() = timestamp

    init {
        if (previousTimestamp != null &&
            previousTimestamp.clientNode != clientNode
        ) {
            throw ClientFormatException(
                "Previous issuing client differs from current",
            )
        }

        timestamp = previousTimestamp ?: Timestamp(
            LogicalTimestamp(Instant.ofEpochMilli(0)),
            clientNode,
            Counter(0),
        )
    }

    fun issueLocalEvent(): Timestamp = localEventOrSend()

    fun issueLocalEventPacked(): String = localEventOrSend().encode()

    fun send(): Timestamp = localEventOrSend()

    fun sendPacked(): String = localEventOrSend().encode()

    fun receivePacked(packedTimestamp: String): Timestamp =
        receive(Timestamp.fromEncoded(packedTimestamp))

    fun receivePackedAndRepack(packedTimestamp: String): String =
        receive(Timestamp.fromEncoded(packedTimestamp)).encode()

    fun receive(incoming: Timestamp): Timestamp {
        val now = config.getPhysicalTime()
        val newLogicalTime =
            maxOf(now, incoming.logicalTime, timestamp.logicalTime)

        val newCounter =
            when {
                newLogicalTime == timestamp.logicalTime &&
                    newLogicalTime == incoming.logicalTime ->
                    Counter(
                        max(
                            timestamp.counter.value,
                            incoming.counter.value,
                        ) + 1,
                    )

                newLogicalTime == timestamp.logicalTime ->
                    timestamp.counter.increment()

                newLogicalTime == incoming.logicalTime ->
                    incoming.counter.increment()

                else ->
                    Counter(0)
            }

        val newTimestamp =
            timestamp.copy(
                logicalTime = newLogicalTime,
                counter = newCounter,
            )

        return setTimestamp(newTimestamp, physicalDriftReferenceTime = now)
    }

    private fun localEventOrSend(): Timestamp {
        val now = config.getPhysicalTime()

        val newTimestamp =
            if (timestamp.logicalTime > now) {
                timestamp.copy(
                    counter = timestamp.counter.increment(),
                )
            } else {
                timestamp.copy(
                    logicalTime = now,
                    counter = Counter(0),
                )
            }

        return setTimestamp(newTimestamp, physicalDriftReferenceTime = now)
    }

    private fun setTimestamp(
        newTimestamp: Timestamp,
        physicalDriftReferenceTime: LogicalTimestamp? = null,
    ): Timestamp {
        if (newTimestamp.counter.value > config.maxCount) {
            throw CounterOverflowException(
                "Counter exceeded the limit of ${config.maxCount}",
            )
        }

        if (physicalDriftReferenceTime != null) {
            val drift =
                newTimestamp.logicalTime.absDifferenceInMillis(
                    physicalDriftReferenceTime,
                )

            if (drift > config.maxClockDriftMilliseconds) {
                throw ClockDriftException(
                    "Logical time drifted from physical time by more than ${config.maxClockDriftMilliseconds} ms",
                )
            }
        }

        timestamp = newTimestamp
        return timestamp
    }

    companion object {
        private fun maxOf(
            a: LogicalTimestamp,
            b: LogicalTimestamp,
            c: LogicalTimestamp,
        ): LogicalTimestamp = maxOf(maxOf(a, b), c)
    }
}
