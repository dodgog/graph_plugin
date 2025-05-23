package com.daylightcomputer.hlc.model

import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import com.daylightcomputer.hlc.HLCConfig
import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.exceptions.CounterOverflowException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

// AIUSE: AI generated tests

class CounterTest {
    @BeforeEach
    fun setup() {
        HLCEnvironment.resetForTests()
        HLCEnvironment.initialize(
            HLCConfig(
                counterLength = 4,
            ),
        )
    }

    @Test
    fun `Counter initialization with valid value works`() {
        val counter = Counter(0)
        assertThat(counter.value).isEqualTo(0)
    }

    @Test
    fun `Counter initialization with negative value throws exception`() {
        assertk
            .assertFailure {
                Counter(-1)
            }.hasClass(CounterOverflowException::class.java)
    }

    @Test
    fun `Counter initialization with overflow value throws exception`() {
        assertk
            .assertFailure {
                Counter(0x10000)
            }.hasClass(CounterOverflowException::class.java)
    }

    @Test
    fun `Counter increment works correctly`() {
        val counter = Counter(0)
        val incremented = counter.increment()
        assertThat(incremented.value).isEqualTo(1)
    }

    @Test
    fun `Counter increment at max value throws exception`() {
        val counter = Counter(0xFFFF)
        assertk
            .assertFailure {
                counter.increment()
            }.hasClass(CounterOverflowException::class.java)
    }

    @Test
    fun `Counter comparison works correctly`() {
        val counter1 = Counter(1)
        val counter2 = Counter(2)
        val counter1Duplicate = Counter(1)

        assertThat(counter1).isLessThan(counter2)
        assertThat(counter1).isEqualTo(counter1Duplicate)
    }

    @Test
    fun `Counter packing and unpacking works`() {
        val original = Counter(0x1234)
        val packed = original.encode()
        val unpacked = Counter.fromEncoded(packed)

        assertThat(original.value).isEqualTo(unpacked.value)
    }

    @Test
    fun `Counter packing maintains correct length`() {
        val counter = Counter(0x1234)
        assertThat(counter.encode().length).isEqualTo(4)
    }

    @Test
    fun `Counter packing produces correct hex format`() {
        val counter = Counter(0x1234)
        assertThat(counter.encode()).isEqualTo("1234")
    }
}
