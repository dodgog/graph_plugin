package com.daylightcomputer.hlc

import kotlin.math.max

class HLC(
    val clientNode: ClientNode,
    previousTimestamp: Timestamp? = null
) {
    private val config: HLCConfig = HLCEnvironment.config
    private var _timestamp: Timestamp

    init {
        if (previousTimestamp != null && previousTimestamp.clientNode != clientNode) {
            throw ClientException("Previous issuing client differs from current")
        }

        _timestamp = previousTimestamp ?: Timestamp(
            LogicalTimestamp.Companion.fromMillis(0),
            clientNode,
            Counter(0)
        )
    }

    fun issueLocalEvent(): Timestamp {
        return localEventOrSend()
    }

    fun issueLocalEventPacked(): String {
        return localEventOrSend().pack()
    }

    fun send(): Timestamp {
        return localEventOrSend()
    }

    fun sendPacked(): String {
        return localEventOrSend().pack()
    }

    fun receivePacked(packedTimestamp: String): Timestamp {
        return receive(Timestamp.fromPacked(packedTimestamp))
    }

    fun receivePackedAndRepack(packedTimestamp: String): String {
        return receive(Timestamp.fromPacked(packedTimestamp)).pack()
    }

    fun receive(incoming: Timestamp): Timestamp {
        val now = config.getPhysicalTime()
        val newLogicalTime = maxOf(now, incoming.logicalTime, _timestamp.logicalTime)

        val newCounter = when {
            newLogicalTime == _timestamp.logicalTime && newLogicalTime == incoming.logicalTime ->
                Counter(max(_timestamp.counter.value, incoming.counter.value) + 1)

            newLogicalTime == _timestamp.logicalTime ->
                _timestamp.counter.increment()

            newLogicalTime == incoming.logicalTime ->
                _timestamp.counter.increment()

            else ->
                Counter(0)
        }

        val newTimestamp = _timestamp.copy(
            logicalTime = newLogicalTime,
            counter = newCounter
        )

        return setTimestamp(newTimestamp, physicalDriftReferenceTime = now)
    }

    private fun localEventOrSend(): Timestamp {
        val now = config.getPhysicalTime()

        val newTimestamp = if (_timestamp.logicalTime > now) {
            _timestamp.copy(
                counter = _timestamp.counter.increment()
            )
        } else {
            _timestamp.copy(
                logicalTime = now,
                counter = Counter(0)
            )
        }

        return setTimestamp(newTimestamp, physicalDriftReferenceTime = now)
    }

    private fun setTimestamp(
        newTimestamp: Timestamp,
        physicalDriftReferenceTime: LogicalTimestamp? = null
    ): Timestamp {
        if (newTimestamp.counter.value > config.maxCount) {
            throw CounterOverflowException("Counter exceeded the limit of ${config.maxCount}")
        }

        if (physicalDriftReferenceTime != null) {
            val drift = newTimestamp.logicalTime.absDifferenceInMillis(physicalDriftReferenceTime)

            if (drift > config.maxClockDriftMilliseconds) {
                throw ClockDriftException(
                    "Logical time drifted from physical time by more than ${config.maxClockDriftMilliseconds} ms"
                )
            }
        }

        _timestamp = newTimestamp
        return _timestamp
    }

    fun getCurrentTimestamp(): Timestamp {
        return _timestamp
    }

    companion object {
        /**
         * Helper method to compare and find the maximum of three timestamps
         */
        private fun maxOf(a: LogicalTimestamp, b: LogicalTimestamp, c: LogicalTimestamp): LogicalTimestamp {
            return maxOf(maxOf(a, b), c)
        }
    }
}

