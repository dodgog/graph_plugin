package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.HLCEnvironment
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

data class LogicalTimestamp(val instant: Instant) : Comparable<LogicalTimestamp>, Packable<LogicalTimestamp> {
    override fun pack(): String {
        return FORMATTER.format(instant)
    }

    override fun compareTo(other: LogicalTimestamp): Int {
        return instant.compareTo(other.instant)
    }

    fun absDifferenceInMillis(other: LogicalTimestamp): Long {
        return (instant.toEpochMilli() - other.instant.toEpochMilli()).absoluteValue
    }

    internal val millisForTests: Long get() = instant.toEpochMilli()

    companion object : Packable.HelpHelp<LogicalTimestamp> {
        override val packedLength: Int
            get() = HLCEnvironment.config.logicalTimestampLength

        override fun fromPackedImpl(data: String): LogicalTimestamp {
            return LogicalTimestamp(Instant.from(FORMATTER.parse(data)))
        }

        val FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ISO_INSTANT
            .withZone(ZoneOffset.UTC)

        fun now(): LogicalTimestamp {
            return LogicalTimestamp(Instant.now())
        }

        internal fun fromMillisForTests(millis: Long): LogicalTimestamp {
            return LogicalTimestamp(Instant.ofEpochMilli(millis))
        }
    }
}