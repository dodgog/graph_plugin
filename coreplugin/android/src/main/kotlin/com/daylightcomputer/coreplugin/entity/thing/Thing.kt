package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.Entity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

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

    override fun validatePropertiesOnInit() {
        type
    }

    override val isDeleted: Boolean by entity.optional(
        "isDeleted",
        defaultValue = false,
        decode = { it.toBoolean() },
    )

    override val changeNotifications: Flow<Unit> = entity.attributeChanges.transform { }

    init {
        validatePropertiesOnInit()
        // TODO: check extra attributes perhaps
    }
}
