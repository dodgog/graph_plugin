package com.daylightcomputer.coreplugin.entity.thing

interface IThing {
    val id: String
    val type: ThingTypes
    val isDeleted: Boolean
    val lastModifiedAtTimestamp: String

//    fun <T : Thing> mutateSingleAttribute(
//        // TODO: signature
//        name: String,
//        value: String,
//        issueTimestamp: () -> String,
//    ): Pair<Pair<String, AttributeValueRecord>, T>
//
//    // TODO different return types
//    fun getKnownFields(): Set<String>
//
//    // TODO different types
//    fun getUnknownAttributes(): Set<String>
}
