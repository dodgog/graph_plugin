package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.Entity

/**
 * Represents a node in a graph, a piece which can connect to others
 */
class Node(
    entity: Entity,
) : INode,
    IThing by Thing(entity) {
    override val title: String by entity.required(
        "title",
        decode = { it ?: throw IllegalArgumentException("Title cannot be null for Node entity") },
    )

    override fun validateRequiredProperties() {
        title
    }

    init {
        validateRequiredProperties()
    }
}
