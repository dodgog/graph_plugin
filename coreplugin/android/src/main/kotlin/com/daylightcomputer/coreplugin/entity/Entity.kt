package com.daylightcomputer.coreplugin.entity

import com.daylightcomputer.coreplugin.database.sqldefinitions.Attributes
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// Entity is a mutable collection of
// attribute to (value, timestamp maps)
// which has an id
data class Entity(
    val id: String,
    private var _attributes: MutableMap<String, AttributeValueRecord>,
) {
    val attributes get(): Map<String, AttributeValueRecord> = _attributes.toMap()

    // Read-only delegates
    fun <T> required(
        attributeName: String,
        transform: (String) -> T,
    ) = object : ReadOnlyProperty<Any?, T> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): T = transform(getRequiredAttribute(attributeName))
    }

    fun <T> optional(
        attributeName: String,
        defaultValue: T,
        transform: (String?) -> T,
    ) = object : ReadOnlyProperty<Any?, T> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): T = transform(getAttribute(attributeName)) ?: defaultValue
    }

    fun <T> derived(derivation: (Entity) -> T) =
        object : ReadOnlyProperty<Any?, T> {
            override fun getValue(
                thisRef: Any?,
                property: KProperty<*>,
            ): T = derivation(this@Entity)
        }

//    // Mutable delegates
//    fun requiredMutable(attributeName: String, timestampProvider: () -> String) =
//        object : ReadWriteProperty<Any?, String> {
//            override fun getValue(thisRef: Any?, property: KProperty<*>): String {
//                return getRequiredAttribute(attributeName)
//            }
//
//            override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
//                setRequiredAttribute(attributeName, value, timestampProvider())
//            }
//        }
//
//    fun optionalMutable(attributeName: String, timestampProvider: () -> String) =
//        object : ReadWriteProperty<Any?, String?> {
//            override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
//                return getAttribute(attributeName)
//            }
//
//            override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
//                setAttribute(attributeName, value, timestampProvider())
//            }
//        }
//
//    // Helper methods
//    fun setRequiredAttribute(attributeName: String, value: String, timestamp: String) {
//        _attributes[attributeName] = AttributeValueRecord(value, timestamp)
//    }
//
//    fun setAttribute(attributeName: String, value: String?, timestamp: String) {
//        _attributes[attributeName] = AttributeValueRecord(value, timestamp)
//    }

    fun getRequiredAttribute(attributeName: String): String =
        _attributes[attributeName]?.value ?: throw IllegalStateException(
            "Entity lacks a required attribute `$attributeName`",
        )

    fun getAttribute(attributeName: String): String? = _attributes[attributeName]?.value

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
                    it.attr_name to AttributeValueRecord(it.attr_val, it.timestamp)
                }
            return Entity(id, map.toMutableMap())
        }

        /**
         * From an arbitrary collection of attributes
         * get all possible entities (by distinct id's).
         * CAUTION: lazy evaluation with sequences.
         * */
        fun fromAttributePool(attributes: Sequence<Attributes>): Sequence<Entity> =
            attributes.groupBy { it.entity_id }.asSequence().map { (entityId, attrs) ->
                val attributeMap =
                    attrs.associate {
                        it.attr_name to AttributeValueRecord(it.attr_val, it.timestamp)
                    }
                Entity(entityId, attributeMap.toMutableMap())
            }
    }
}
