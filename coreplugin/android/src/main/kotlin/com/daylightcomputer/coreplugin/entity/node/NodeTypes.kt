package com.daylightcomputer.coreplugin.entity.node

import com.daylightcomputer.coreplugin.entity.Entity

enum class NodeTypes(
    val stringValue: String,
    val factory: (Entity) -> Node?,
) {
    DOCUMENT("DOCUMENT", ::DocumentNode),
    FOUNDATION("FOUNDATION", ::Node),
    ;

    companion object {
        fun fromString(value: String): NodeTypes =
            NodeTypes.entries.find {
                it.stringValue.equals(value, ignoreCase = true)
            }
                ?: throw IllegalStateException(
                    "Node type can not be constructed from string",
                )

        /**
         * Create appropriate Node subclass from Entity
         * using the type's factory method
         */
        fun createNodeFromEntity(entity: Entity): Node? {
            val typeString = entity.attributes["type"]?.value ?: return null
            val nodeType = fromString(typeString)
            return nodeType.factory(entity)
        }
    }
}
