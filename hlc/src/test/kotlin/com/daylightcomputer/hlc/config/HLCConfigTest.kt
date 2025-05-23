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
        assertThat(config.counterLength).isEqualTo(4)
        assertThat(config.distributedNodeLength).isEqualTo(6)
        assertThat(config.logicalTimestampLength).isEqualTo(27)
    }

    @Test
    fun `HLCConfig custom values are set correctly`() {
        val config =
            HLCConfig(
                maxClockDriftMilliseconds = 1_800_000,
                counterLength = 2,
                distributedNodeLength = 4,
                logicalTimestampLength = 20,
            )
        assertThat(config.maxClockDriftMilliseconds).isEqualTo(1_800_000)
        assertThat(config.counterLength).isEqualTo(2)
        assertThat(config.distributedNodeLength).isEqualTo(4)
        assertThat(config.logicalTimestampLength).isEqualTo(20)
    }

    @Test
    fun `HLCConfig maxCount is calculated correctly`() {
        val config = HLCConfig(counterLength = 2)
        assertThat(config.maxCount).isEqualTo(0xFF)
    }

    @Test
    fun `HLCConfig packing and unpacking works`() {
        val original =
            HLCConfig(
                maxClockDriftMilliseconds = 1_800_000,
                counterLength = 2,
                distributedNodeLength = 4,
                logicalTimestampLength = 20,
            )
        val packed = original.encode()
        val unpacked = HLCConfig.fromEncoded(packed)

        assertThat(
            unpacked.maxClockDriftMilliseconds,
        ).isEqualTo(original.maxClockDriftMilliseconds)
        assertThat(
            unpacked.counterLength,
        ).isEqualTo(original.counterLength)
        assertThat(
            unpacked.distributedNodeLength,
        ).isEqualTo(original.distributedNodeLength)
        assertThat(
            unpacked.logicalTimestampLength,
        ).isEqualTo(original.logicalTimestampLength)
    }

    @Test
    fun `HLCConfig packing maintains correct length`() {
        val config = HLCConfig()
        assertThat(config.encode().length).isEqualTo(50)
    }
}
