package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.exceptions.CounterOverflowException

/**
 * Represents a logical counter in a Hybrid Logical Clock
 */
data class Counter(val value: Int) : Comparable<Counter>, Packable<Counter> {
    init {
        if (value > maxValue){
            throw CounterOverflowException("Counter exceeded the limit of $maxValue")
        }
    }

    companion object : Packable.HelpHelp<Counter> {
        override val packedLength: Int
            get() = HLCEnvironment.config.hexCounterLength
        val maxValue: Int get() = HLCEnvironment.config.maxCount

        override fun fromPackedImpl(data: String): Counter {
            val value = data.toIntOrNull(16) ?: 0
            return Counter(value)
        }

        fun computeMaxValue(numberOfHexDigits: Int): Int {
            return (1 shl (4 * numberOfHexDigits)) - 1
        }
    }

    init {
        require(value in 0..maxValue) {
            "Counter value must be between 0 and $maxValue but was $value"
        }
    }

    override fun pack(): String {
        return value.toString(16).padStart(packedLength, '0')
    }

    fun increment(): Counter {
        if (value > maxValue) {
            throw CounterOverflowException("Counter exceeded the limit of $maxValue")
        }
        return Counter(value + 1)
    }

    override fun compareTo(other: Counter): Int {
        return value.compareTo(other.value)
    }
}