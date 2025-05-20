package com.daylightcomputer.hlc.config

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.daylightcomputer.hlc.HLCConfig
import org.junit.jupiter.api.Test

// AIUSE: AI generated tests

class HLCConfigTest {
    @Test
    fun `HLCConfig default values are correct`() {
        val config = HLCConfig()
        assertThat(config.maxClockDriftMilliseconds).isEqualTo(3_600_000)
        assertThat(config.hexCounterLength).isEqualTo(4)
        assertThat(config.clientNodeLength).isEqualTo(6)
        assertThat(config.logicalTimestampLength).isEqualTo(27)
    }

    @Test
    fun `HLCConfig custom values are set correctly`() {
        val config = HLCConfig(
            maxClockDriftMilliseconds = 1_800_000,
            hexCounterLength = 2,
            clientNodeLength = 4,
            logicalTimestampLength = 20
        )
        assertThat(config.maxClockDriftMilliseconds).isEqualTo(1_800_000)
        assertThat(config.hexCounterLength).isEqualTo(2)
        assertThat(config.clientNodeLength).isEqualTo(4)
        assertThat(config.logicalTimestampLength).isEqualTo(20)
    }

    @Test
    fun `HLCConfig maxCount is calculated correctly`() {
        val config = HLCConfig(hexCounterLength = 2)
        assertThat(config.maxCount).isEqualTo(0xFF)
    }

    @Test
    fun `HLCConfig packing and unpacking works`() {
        val original = HLCConfig(
            maxClockDriftMilliseconds = 1_800_000,
            hexCounterLength = 2,
            clientNodeLength = 4,
            logicalTimestampLength = 20
        )
        val packed = original.pack()
        val unpacked = HLCConfig.fromPacked(packed)

        assertThat(unpacked.maxClockDriftMilliseconds).isEqualTo(original.maxClockDriftMilliseconds)
        assertThat(unpacked.hexCounterLength).isEqualTo(original.hexCounterLength)
        assertThat(unpacked.clientNodeLength).isEqualTo(original.clientNodeLength)
        assertThat(unpacked.logicalTimestampLength).isEqualTo(original.logicalTimestampLength)
    }

    @Test
    fun `HLCConfig packing maintains correct length`() {
        val config = HLCConfig()
        assertThat(config.pack().length).isEqualTo(50)
    }
} 