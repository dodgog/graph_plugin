package com.daylightcomputer.coreplugin.entity.thing

import kotlinx.coroutines.flow.Flow

interface IThing {
    val id: String
    val type: ThingTypes
    val isDeleted: Boolean
    val lastModifiedAtTimestamp: String

    /**
     * Notify every time the underlying attributes change: e.g. to trigger rebuild
     */
    val changeNotifications: Flow<Unit>

    /**
     * Invocation of all the required properties because if they cannot be obtained they will throw
     */
    fun validatePropertiesOnInit()

//
//    // TODO different types
//    fun getUnknownAttributes(): Set<String>
}
