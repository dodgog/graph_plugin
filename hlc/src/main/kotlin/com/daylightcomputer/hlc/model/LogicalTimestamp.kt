package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.HLCEnvironment
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlin.math.absoluteValue

data class LogicalTimestamp(
    val instant: Instant,
) : Comparable<LogicalTimestamp>,
    Packable<LogicalTimestamp> {
    override fun encode(): String = instant.toString()

    override fun compareTo(other: LogicalTimestamp): Int =
        instant.compareTo(other.instant)

    fun absDifferenceInMillis(other: LogicalTimestamp): Long =
        (
            instant.toEpochMilliseconds() -
                other.instant.toEpochMilliseconds()
        ).absoluteValue

    internal val millisForTests: Long get() = instant.toEpochMilliseconds()

    companion object : Packable.HelpHelp<LogicalTimestamp> {
        override val encodedLength: Int
            get() = HLCEnvironment.config.logicalTimestampLength

        override fun fromEncodedImpl(data: String): LogicalTimestamp =
            LogicalTimestamp(data.toInstant())

        fun now(): LogicalTimestamp =
            LogicalTimestamp(HLCEnvironment.config.getPhysicalTime())

        internal fun fromMillisForTests(millis: Long): LogicalTimestamp =
            LogicalTimestamp(Instant.fromEpochMilliseconds(millis))
    }
}
