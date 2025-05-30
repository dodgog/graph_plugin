package com.daylightcomputer.coreplugin.entity.node

import com.daylightcomputer.coreplugin.entity.Entity

open class Node(
    val id: String,
    val type: NodeTypes,
    val lastModifiedAtTimestamp: String,
    val isDeleted: Boolean,
) {
    companion object {
        /**
         * Creates a Node from an Entity by extracting required attributes
         */
        fun fromEntity(entity: Entity): Node? {
            val typeStr = entity.attributes["type"]?.value ?: return null
            val type = NodeTypes.fromString(typeStr) ?: return null

            val isDeleted =
                entity.attributes["isDeleted"]?.value?.toBoolean() ?: false

            val lastModified =
                entity.attributes.values.maxOfOrNull { it.timestamp }
                    ?: return null

            return Node(
                id = entity.id,
                type = type,
                lastModifiedAtTimestamp = lastModified,
                isDeleted = isDeleted,
            )
        }
    }
}
