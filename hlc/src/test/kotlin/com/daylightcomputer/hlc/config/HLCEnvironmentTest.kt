package com.daylightcomputer.hlc.config

import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import com.daylightcomputer.hlc.HLCConfig
import com.daylightcomputer.hlc.HLCEnvironment
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

// AIUSE: AI generated tests

class HLCEnvironmentTest {
    @AfterEach
    fun tearDown() {
        HLCEnvironment.resetForTests()
    }

    @Test
    fun `HLCEnvironment initialization works`() {
        val config = HLCConfig()
        HLCEnvironment.initialize(config)
        assertThat(HLCEnvironment.config).isEqualTo(config)
    }

    @Test
    fun `HLCEnvironment double initialization throws exception`() {
        val config = HLCConfig()
        HLCEnvironment.initialize(config)
        assertk.assertFailure {
            HLCEnvironment.initialize(config)
        }.hasClass(IllegalStateException::class.java)
    }

    @Test
    fun `HLCEnvironment access before initialization throws exception`() {
        assertk.assertFailure {
            HLCEnvironment.config
        }.hasClass(IllegalStateException::class.java)
    }

    @Test
    fun `HLCEnvironment reset works`() {
        val config = HLCConfig()
        HLCEnvironment.initialize(config)
        HLCEnvironment.resetForTests()
        assertk.assertFailure {
            HLCEnvironment.config
        }.hasClass(IllegalStateException::class.java)
    }
} 