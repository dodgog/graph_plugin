package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.HLCEnvironment
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

data class LogicalTimestamp(val instant: Instant) : Comparable<LogicalTimestamp>, Packable<LogicalTimestamp> {

    companion object : Packable.HelpHelp<LogicalTimestamp> {
        override val numberOfCharactersInRepresentation: Int
            get() =
                HLCEnvironment.config.numberOfCharactersInLogicalTimestampRepresentation
        val formatter: DateTimeFormatter = DateTimeFormatter
            .ISO_INSTANT
            .withZone(ZoneOffset.UTC)

        override fun fromPackedImpl(data: String): LogicalTimestamp {
            return LogicalTimestamp(Instant.from(formatter.parse(data)))
        }

        fun now(): LogicalTimestamp {
            return LogicalTimestamp(Instant.now())
        }

        fun fromMillis(millis: Long): LogicalTimestamp {
            return LogicalTimestamp(Instant.ofEpochMilli(millis))
        }
    }

    fun absDifferenceInMillis(other: LogicalTimestamp): Long {
        return (instant.toEpochMilli() - other.instant.toEpochMilli()).absoluteValue
    }

    override fun pack(): String {
        return formatter.format(instant)
    }

    override fun compareTo(other: LogicalTimestamp): Int {
        return instant.compareTo(other.instant)
    }

    val millis: Long get() = instant.toEpochMilli()
}