package com.daylightcomputer.hlc

import com.daylightcomputer.hlc.model.Counter
import com.daylightcomputer.hlc.model.LogicalTimestamp
import com.daylightcomputer.hlc.model.Packable

data class HLCConfig(
    val maxClockDriftMilliseconds: Int = 3_600_000,

    val numberOfCharactersInCounterHexRepresentation: Int = 4,
    val numberOfCharactersInClientNodeRepresentation: Int = 6,
    val numberOfCharactersInLogicalTimestampRepresentation: Int = 27,

    val getPhysicalTime: () -> LogicalTimestamp = { LogicalTimestamp.now() }
) : Packable<HLCConfig> {
    val maxCount: Int = Counter.Companion.computeMaxValue(numberOfCharactersInCounterHexRepresentation)


    override fun pack(): String {
        val serialized =
            "$maxClockDriftMilliseconds|$numberOfCharactersInCounterHexRepresentation|$numberOfCharactersInClientNodeRepresentation|$numberOfCharactersInLogicalTimestampRepresentation"
        return serialized.padEnd(numberOfCharactersInRepresentation, '#')
    }

    companion object : Packable.HelpHelp<HLCConfig> {
        override val numberOfCharactersInRepresentation: Int = 50  // A reasonable estimate based on field values

        override fun fromPackedImpl(data: String): HLCConfig {
            val trimmed = data.trimEnd('#')
            val parts = trimmed.split("|")
            return HLCConfig(
                maxClockDriftMilliseconds = parts[0].toInt(),
                numberOfCharactersInCounterHexRepresentation = parts[1].toInt(),
                numberOfCharactersInClientNodeRepresentation = parts[2].toInt(),
                numberOfCharactersInLogicalTimestampRepresentation = parts[3].toInt()
            )
        }
    }
}