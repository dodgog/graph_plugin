package com.daylightcomputer.hlc

import com.daylightcomputer.hlc.model.Counter
import com.daylightcomputer.hlc.model.LogicalTimestamp
import com.daylightcomputer.hlc.model.Packable

data class HLCConfig(
    val maxClockDriftMilliseconds: Int = 3_600_000,

    val hexCounterLength: Int = 4,
    val clientNodeLength: Int = 6,
    val logicalTimestampLength: Int = 27,

    val getPhysicalTime: () -> LogicalTimestamp = { LogicalTimestamp.now() }
) : Packable<HLCConfig> {
    val maxCount: Int = Counter.Companion.computeMaxValue(hexCounterLength)


    override fun pack(): String {
        val serialized =
            "$maxClockDriftMilliseconds|$hexCounterLength|$clientNodeLength|$logicalTimestampLength"
        return serialized.padEnd(packedLength, '#')
    }

    companion object : Packable.HelpHelp<HLCConfig> {
        override val packedLength: Int = 50  // A reasonable estimate based on field values

        override fun fromPackedImpl(data: String): HLCConfig {
            val trimmed = data.trimEnd('#')
            val parts = trimmed.split("|")
            return HLCConfig(
                maxClockDriftMilliseconds = parts[0].toInt(),
                hexCounterLength = parts[1].toInt(),
                clientNodeLength = parts[2].toInt(),
                logicalTimestampLength = parts[3].toInt()
            )
        }
    }
}