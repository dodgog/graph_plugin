package com.daylightcomputer.hlc

import com.daylightcomputer.hlc.model.Counter
import com.daylightcomputer.hlc.model.LogicalTimestamp
import com.daylightcomputer.hlc.model.Packable

data class HLCConfig(
    val maxClockDriftMilliseconds: Int = 3_600_000,
    val counterLength: Int = 4,
    val distributedNodeLength: Int = 6,
    val logicalTimestampLength: Int = 27,
    val getPhysicalTime: () -> LogicalTimestamp = { LogicalTimestamp.now() },
) : Packable<HLCConfig> {
    override fun encode(): String {
        val serialized =
            "$maxClockDriftMilliseconds|$counterLength|$distributedNodeLength|$logicalTimestampLength"
        return serialized.padEnd(encodedLength, '#')
    }

    val maxCount: Int =
        Counter.Companion
            .computeMaxValue(counterLength)

    companion object : Packable.HelpHelp<HLCConfig> {
        override val encodedLength: Int = 50

        override fun fromEncodedImpl(data: String): HLCConfig {
            val trimmed = data.trimEnd('#')
            val parts = trimmed.split("|")
            return HLCConfig(
                maxClockDriftMilliseconds = parts[0].toInt(),
                counterLength = parts[1].toInt(),
                distributedNodeLength = parts[2].toInt(),
                logicalTimestampLength = parts[3].toInt(),
            )
        }
    }
}
