package com.daylightcomputer.coreplugin.entity

import com.daylightcomputer.coreplugin.database.sqldefinitions.Attributes

data class Entity(
    val id: String,
    val attributes: Map<String, AttributeValue>,
) {
    companion object {
        /**
         * From an arbitrary list of attributes find entity with id
         * TODO: is sequence appropriate here?
         * */
        fun fromAttributePool(
            id: String,
            attributes: Sequence<Attributes>,
        ): Entity {
            val map =
                attributes.filter { it.entity_id == id }.associate {
                    it.attr_name to
                        AttributeValue(it.attr_val, it.timestamp)
                }
            return Entity(id, map)
        }

        /**
         * From an arbitrary collection of attributes
         * get all possible entities (by distinct id's).
         * CAUTION: lazy evaluation with sequences.
         * */
        fun fromAttributePool(
            attributes: Sequence<Attributes>,
        ): Sequence<Entity> =
            attributes
                .groupBy { it.entity_id }
                .asSequence()
                .map { (entityId, attrs) ->
                    val attributeMap =
                        attrs.associate {
                            it.attr_name to
                                AttributeValue(it.attr_val, it.timestamp)
                        }
                    Entity(entityId, attributeMap)
                }
    }
}
