package com.daylightcomputer.hlc.config

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.daylightcomputer.hlc.HLCConfig
import com.daylightcomputer.hlc.model.Counter
import org.junit.jupiter.api.Test

// AIUSE: AI generated tests

class HLCConfigTest {
    @Test
    fun `HLCConfig default values are correct`() {
        val config = HLCConfig()
        assertThat(config.maxClockDriftMilliseconds).isEqualTo(3_600_000)
        assertThat(config.numberOfCharactersInCounterHexRepresentation).isEqualTo(4)
        assertThat(config.numberOfCharactersInClientNodeRepresentation).isEqualTo(6)
        assertThat(config.numberOfCharactersInLogicalTimestampRepresentation).isEqualTo(27)
    }

    @Test
    fun `HLCConfig custom values are set correctly`() {
        val config = HLCConfig(
            maxClockDriftMilliseconds = 1_800_000,
            numberOfCharactersInCounterHexRepresentation = 2,
            numberOfCharactersInClientNodeRepresentation = 4,
            numberOfCharactersInLogicalTimestampRepresentation = 20
        )
        assertThat(config.maxClockDriftMilliseconds).isEqualTo(1_800_000)
        assertThat(config.numberOfCharactersInCounterHexRepresentation).isEqualTo(2)
        assertThat(config.numberOfCharactersInClientNodeRepresentation).isEqualTo(4)
        assertThat(config.numberOfCharactersInLogicalTimestampRepresentation).isEqualTo(20)
    }

    @Test
    fun `HLCConfig maxCount is calculated correctly`() {
        val config = HLCConfig(numberOfCharactersInCounterHexRepresentation = 2)
        assertThat(config.maxCount).isEqualTo(0xFF)
    }

    @Test
    fun `HLCConfig packing and unpacking works`() {
        val original = HLCConfig(
            maxClockDriftMilliseconds = 1_800_000,
            numberOfCharactersInCounterHexRepresentation = 2,
            numberOfCharactersInClientNodeRepresentation = 4,
            numberOfCharactersInLogicalTimestampRepresentation = 20
        )
        val packed = original.pack()
        val unpacked = HLCConfig.fromPacked(packed)

        assertThat(unpacked.maxClockDriftMilliseconds).isEqualTo(original.maxClockDriftMilliseconds)
        assertThat(unpacked.numberOfCharactersInCounterHexRepresentation).isEqualTo(original.numberOfCharactersInCounterHexRepresentation)
        assertThat(unpacked.numberOfCharactersInClientNodeRepresentation).isEqualTo(original.numberOfCharactersInClientNodeRepresentation)
        assertThat(unpacked.numberOfCharactersInLogicalTimestampRepresentation).isEqualTo(original.numberOfCharactersInLogicalTimestampRepresentation)
    }

    @Test
    fun `HLCConfig packing maintains correct length`() {
        val config = HLCConfig()
        assertThat(config.pack().length).isEqualTo(50)
    }
} 