package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.exceptions.TimestampFormatException

interface Packable<T> {
    fun encode(): String

    interface HelpHelp<T> {
        val encodedLength: Int

        fun fromEncoded(data: String): T {
            validateLength(data)
            return fromEncodedImpl(data)
        }

        fun fromEncodedImpl(data: String): T

        fun validateLength(data: String) {
            if (data.length != encodedLength) {
                throw TimestampFormatException(
                    "Invalid packed data length: expected $encodedLength but got ${data.length}",
                )
            }
        }
    }
}
