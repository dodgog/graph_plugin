package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.exceptions.TimestampFormatException

interface Packable<T> {
    interface HelpHelp<T> {
        val numberOfCharactersInRepresentation: Int

        fun fromPacked(data: String): T {
            validatePackedLength(data)
            return fromPackedImpl(data)
        }

        fun fromPackedImpl(data: String): T

        fun validatePackedLength(data: String) {
            if (data.length != numberOfCharactersInRepresentation) {
                throw TimestampFormatException(
                    "Invalid packed data length: expected $numberOfCharactersInRepresentation but got ${data.length}"
                )
            }
        }
    }

    fun pack(): String
}

