package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.Entity

/**
 * Represents any and all pieces of data used to build the knowledge graph: nodes, links, tags
 */
open class Thing(
    val entity: Entity,
) : IThing {
    override val id: String = entity.id

    override val type: ThingTypes by entity.required(
        "type",
        decode = {
            ThingTypes.fromString(
                it ?: throw IllegalArgumentException("Type encoding cannot be null"),
            )
        },
    )

    // TODO: this will throw if error
    override val lastModifiedAtTimestamp: String by entity.derived {
        it.attributes.values.maxOf { valuePair ->
            valuePair.timestamp
        }
    }

    override fun validateRequiredProperties() {
        type
    }

    override val isDeleted: Boolean by entity.optional(
        "isDeleted",
        defaultValue = false,
        decode = { it.toBoolean() },
    )

    init {
        validateRequiredProperties()
        // TODO: check extra attributes perhaps
    }
}
