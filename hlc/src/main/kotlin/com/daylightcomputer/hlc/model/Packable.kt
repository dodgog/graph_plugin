package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.exceptions.TimestampFormatException

interface Packable<T> {
    fun pack(): String

    interface HelpHelp<T> {
        val packedLength: Int

        fun fromPacked(data: String): T {
            validatePackedLength(data)
            return fromPackedImpl(data)
        }

        fun fromPackedImpl(data: String): T

        fun validatePackedLength(data: String) {
            if (data.length != packedLength) {
                throw TimestampFormatException(
                    "Invalid packed data length: expected $packedLength but got ${data.length}"
                )
            }
        }
    }
}

