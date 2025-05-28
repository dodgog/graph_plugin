package com.daylightcomputer.coreplugin.entity.node

import com.daylightcomputer.coreplugin.entity.Entity

enum class NodeTypes(
    val stringValue: String,
    val factory: (Entity) -> Node?,
) {
    DOCUMENT("document", DocumentNode::fromEntity),
    PICTURE("picture", Node::fromEntity),
    ;

    companion object {
        fun fromString(value: String): NodeTypes? =
            values().find {
                it.stringValue.equals(value, ignoreCase = true)
            }

        /**
         * Create appropriate Node subclass from Entity
         * using the type's factory method
         */
        fun createNodeFromEntity(entity: Entity): Node? {
            val typeString = entity.attributes["type"]?.value ?: return null
            val nodeType = fromString(typeString) ?: return null
            return nodeType.factory(entity)
        }
    }
}
