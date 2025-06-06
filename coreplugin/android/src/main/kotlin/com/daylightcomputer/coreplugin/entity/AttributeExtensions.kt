package com.daylightcomputer.coreplugin.entity

import com.daylightcomputer.coreplugin.database.EventsAttributesDatabase
import com.daylightcomputer.coreplugin.database.sqldefinitions.Attributes

fun List<Attributes>.toAttributeMap(): Map<String, AttributeValueRecord> =
    associate { it.attr_name to AttributeValueRecord(it.attr_val, it.timestamp) }

fun Map<String, AttributeValueRecord>.toAttributesList(entityId: String): List<Attributes> =
    map { (name, record) ->
        Attributes(entityId, name, record.value, record.timestamp)
    }

fun Attributes.toAttributePair(): Pair<String, AttributeValueRecord> =
    Pair(this.attr_name, AttributeValueRecord(this.attr_val, this.timestamp))

fun Entity.insertAllAttributes() {
    attributes.toAttributesList(id).onEach { attribute ->
        EventsAttributesDatabase.insertAttributeRecord(
            id,
            attribute.toAttributePair(),
        )
    }
}
