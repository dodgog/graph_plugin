package com.daylightcomputer.coreplugin.entity.thing

interface IThing {
    val id: String
    val type: ThingTypes
    val isDeleted: Boolean
    val lastModifiedAtTimestamp: String

    /**
     * Invocation of all the required properties because if they cannot be obtained they will throw
     */
    fun validatePropertiesOnInit()

//
//    // TODO different types
//    fun getUnknownAttributes(): Set<String>
}
