package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.HLCEnvironment
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlin.math.absoluteValue

data class LogicalTimestamp(
    val instant: Instant,
) : Comparable<LogicalTimestamp>,
    Packable<LogicalTimestamp> {
    override fun encode(): String = instant.format(FIXED_MICROSECOND_FORMAT)

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
            LogicalTimestamp(Instant.parse(data))

        fun now(): LogicalTimestamp =
            LogicalTimestamp(HLCEnvironment.config.getPhysicalTime())

        private val FIXED_MICROSECOND_FORMAT = DateTimeComponents.Format {
            date(LocalDate.Formats.ISO)
            char('T')
            hour()
            char(':')
            minute()
            char(':')
            second()
            char('.')
            secondFraction(fixedLength = 6) // Always 6 digits for microseconds
            char('Z')
        }

        internal fun fromMillisForTests(millis: Long): LogicalTimestamp =
            LogicalTimestamp(Instant.fromEpochMilliseconds(millis))
    }
}
