package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.Entity

open class Thing(
    val entity: Entity,
) : IThing {
    override val id: String = entity.id

    override val type: ThingTypes by entity.required(
        "type",
        // TODO add nullability field
        decode = { ThingTypes.fromString(it!!) },
    )

    // TODO: this will throw if error
    override val lastModifiedAtTimestamp: String by entity.derived {
        it.attributes.values.maxOf { valuePair ->
            valuePair.timestamp
        }
    }

    override val isDeleted: Boolean by entity.optional(
        "isDeleted",
        defaultValue = false,
        decode = { it.toBoolean() },
    )
}
