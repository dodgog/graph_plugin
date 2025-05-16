package com.daylightcomputer.hlc

interface Packable<T> {
    interface HelpHelp<T>{
        val numberOfCharactersInRepresentation: Int

        fun fromPacked(data: String): T {
            validatePackedLength(data)
            return fromPackedImpl(data)
        }
        
        fun fromPackedImpl(data: String): T
        
        fun validatePackedLength(data: String) {
            require(data.length == numberOfCharactersInRepresentation) { 
                "Invalid packed data length: expected $numberOfCharactersInRepresentation but got ${data.length}" 
            }
        }
    }

    fun pack(): String
}

