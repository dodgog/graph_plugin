package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.exceptions.CounterOverflowException

data class Counter(
    val value: Int,
) : Comparable<Counter>,
    Packable<Counter> {
    init {
        if (value > maxValue || value < 0) {
            throw CounterOverflowException(
                "Counter exceeded the limit of $maxValue",
            )
        }
    }

    override fun encode(): String =
        value.toString(16).padStart(encodedLength, '0')

    override fun compareTo(other: Counter): Int = value.compareTo(other.value)

    fun increment(): Counter = Counter(value + 1)

    companion object : Packable.HelpHelp<Counter> {
        /** Maximum value expressible with the encoding */
        val maxValue: Int get() = HLCEnvironment.config.maxCount

        /** How to compute the max value available in with encoding */
        fun computeMaxValue(numberOfHexDigits: Int): Int =
            (1 shl (4 * numberOfHexDigits)) - 1

        override val encodedLength: Int
            get() = HLCEnvironment.config.counterLength

        override fun fromEncodedImpl(data: String): Counter {
            val value = data.toIntOrNull(16) ?: 0
            return Counter(value)
        }
    }
}
