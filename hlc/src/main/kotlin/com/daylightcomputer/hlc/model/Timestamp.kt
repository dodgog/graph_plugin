package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.exceptions.TimestampFormatException

data class Timestamp(
    val logicalTime: LogicalTimestamp,
    val clientNode: ClientNode,
    val counter: Counter,
) : Comparable<Timestamp>,
    Packable<Timestamp> {
    override fun pack(): String =
        "${logicalTime.pack()}-${counter.pack()}-${clientNode.pack()}"

    override fun compareTo(other: Timestamp): Int {
        val timeCompare = logicalTime.compareTo(other.logicalTime)
        if (timeCompare != 0) return timeCompare

        val counterCompare = counter.compareTo(other.counter)
        if (counterCompare != 0) return counterCompare

        return clientNode.compareTo(other.clientNode)
    }

    companion object : Packable.HelpHelp<Timestamp> {
        override val packedLength: Int
            get() =
                Counter.packedLength +
                    LogicalTimestamp.packedLength +
                    ClientNode.packedLength + 2

        override fun fromPackedImpl(data: String): Timestamp {
            try {
                val timeLength = LogicalTimestamp.packedLength
                val counterLength = Counter.packedLength

                val timeString = data.substring(0, timeLength)
                val counterString =
                    data.substring(
                        timeLength + 1,
                        timeLength + 1 + counterLength,
                    )
                val nodeString =
                    data.substring(timeLength + 1 + counterLength + 1)

                val logicalTime = LogicalTimestamp.fromPacked(timeString)
                val counter = Counter.fromPacked(counterString)
                val clientNode = ClientNode.fromPacked(nodeString)

                return Timestamp(logicalTime, clientNode, counter)
            } catch (e: Exception) {
                when (e) {
                    is TimestampFormatException -> throw e
                    is StringIndexOutOfBoundsException ->
                        throw TimestampFormatException(
                            "Invalid timestamp format: $data",
                        )

                    else -> throw TimestampFormatException(
                        "Failed to parse timestamp: " +
                            "$data. ${e.message}",
                    )
                }
            }
        }
    }
}
