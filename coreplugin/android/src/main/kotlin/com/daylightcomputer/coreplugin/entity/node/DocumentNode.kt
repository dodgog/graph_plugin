package com.daylightcomputer.coreplugin.entity.node

import com.daylightcomputer.coreplugin.entity.Entity

data class DocumentNode(
    private val node: Node,
    val title: String,
    val author: String?,
) : Node(
        id = node.id,
        type = node.type,
        lastModifiedAtTimestamp = node.lastModifiedAtTimestamp,
        isDeleted = node.isDeleted,
    ) {
    companion object {
        /**
         * Creates a DocumentNode from an Entity
         * Requires all Node attributes plus title and author
         */
        fun fromEntity(entity: Entity): DocumentNode? {
            val baseNode = Node.fromEntity(entity) ?: return null

            if (baseNode.type != NodeTypes.DOCUMENT) return null

            // Extract document-specific attributes
            // Must be not null
            val title = entity.attributes["title"]?.value ?: return null
            // Can be null
            val author = entity.attributes["author"]?.value

            return DocumentNode(
                node = baseNode,
                title = title,
                author = author,
            )
        }
    }
}
