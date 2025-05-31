package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.Entity

enum class ThingTypes(
    val stringValue: String,
    val factory: (Entity) -> IThing?,
) {
    // TODO: Tanuj: Optimization for later: maybe store enum values in a map
    //  for faster lookups instead of a find here.
    DOCUMENT("DOCUMENT", ::DocumentNode),
    FOUNDATION("FOUNDATION", ::Thing),
    ;

    companion object {
        fun fromString(value: String): ThingTypes =
            ThingTypes.entries.find {
                it.stringValue.equals(value, ignoreCase = true)
            }
                ?: throw IllegalStateException(
                    "Node type can not be constructed from string",
                )

        /**
         * Create appropriate Node subclass from Entity
         * using the type's factory method
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Thing> createNodeFromEntity(entity: Entity): T? {
            val typeString = entity.attributes["type"]?.value ?: return null
            val nodeType = fromString(typeString)
            return nodeType.factory(entity) as? T
        }
    }
}
