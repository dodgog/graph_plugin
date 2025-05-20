package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.exceptions.CounterOverflowException

data class Counter(val value: Int) : Comparable<Counter>, Packable<Counter> {
    init {
        if (value > maxValue){
            throw CounterOverflowException("Counter exceeded the limit of $maxValue")
        }
    }

    override fun pack(): String {
        return value.toString(16).padStart(packedLength, '0')
    }

    override fun compareTo(other: Counter): Int {
        return value.compareTo(other.value)
    }

    fun increment(): Counter {
        return Counter(value + 1)
    }

    companion object : Packable.HelpHelp<Counter> {
        /** Maximum value expressible with the encoding */
        val maxValue: Int get() = HLCEnvironment.config.maxCount

        /** How to compute the max value available in with encoding */
        fun computeMaxValue(numberOfHexDigits: Int): Int {
            return (1 shl (4 * numberOfHexDigits)) - 1
        }

        override val packedLength: Int
            get() = HLCEnvironment.config.hexCounterLength

        override fun fromPackedImpl(data: String): Counter {
            val value = data.toIntOrNull(16) ?: 0
            return Counter(value)
        }

    }
}