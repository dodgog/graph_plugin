package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.Entity

/**
 * A node in the graph carrying a viewable document
 */
class DocumentNode(
    private val entity: Entity,
) : INode by Node(entity) {
    val author: String? by entity.optional(
        "author",
        defaultValue = null,
        decode = { it },
    )

    override fun validateRequiredProperties() {
    }

    init {
        validateRequiredProperties()
    }
}
