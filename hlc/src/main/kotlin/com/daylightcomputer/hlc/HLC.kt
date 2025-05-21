package com.daylightcomputer.hlc

import com.daylightcomputer.hlc.exceptions.DistributedNodeException
import com.daylightcomputer.hlc.exceptions.ClockDriftException
import com.daylightcomputer.hlc.exceptions.CounterOverflowException
import com.daylightcomputer.hlc.model.DistributedNode
import com.daylightcomputer.hlc.model.Counter
import com.daylightcomputer.hlc.model.LogicalTimestamp
import com.daylightcomputer.hlc.model.Timestamp
import java.time.Instant
import kotlin.math.max

class HLC(
    val distributedNode: DistributedNode,
    previousTimestamp: Timestamp? = null,
) {
    private val config: HLCConfig get() = HLCEnvironment.config

    private var _timestamp: Timestamp

    val timestamp: Timestamp get() = _timestamp

    init {
        if (previousTimestamp != null &&
            previousTimestamp.distributedNode != distributedNode
        ) {
            throw DistributedNodeException(
                "Previous issuing client differs from current",
            )
        }

        _timestamp = previousTimestamp ?: Timestamp(
            LogicalTimestamp(Instant.ofEpochMilli(0)),
            distributedNode,
            Counter(0),
        )
    }

    fun receive(incoming: Timestamp): Timestamp {
        val now = config.getPhysicalTime()
        val newLogicalTime =
            maxOf(now, incoming.logicalTime, _timestamp.logicalTime)

        val newCounter =
            when {
                newLogicalTime == _timestamp.logicalTime &&
                    newLogicalTime == incoming.logicalTime ->
                    Counter(
                        max(
                            _timestamp.counter.value,
                            incoming.counter.value,
                        ) + 1,
                    )

                newLogicalTime == _timestamp.logicalTime ->
                    _timestamp.counter.increment()

                newLogicalTime == incoming.logicalTime ->
                    incoming.counter.increment()

                else ->
                    Counter(0)
            }

        val newTimestamp =
            _timestamp.copy(
                logicalTime = newLogicalTime,
                counter = newCounter,
            )

        return setTimestamp(newTimestamp, physicalDriftReferenceTime = now)
    }

    fun tick(): Timestamp {
        val now = config.getPhysicalTime()

        val newTimestamp =
            if (_timestamp.logicalTime > now) {
                _timestamp.copy(
                    counter = _timestamp.counter.increment(),
                )
            } else {
                _timestamp.copy(
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

        _timestamp = newTimestamp
        return _timestamp
    }

    companion object {
        private fun maxOf(
            a: LogicalTimestamp,
            b: LogicalTimestamp,
            c: LogicalTimestamp,
        ): LogicalTimestamp = maxOf(maxOf(a, b), c)
    }
}
