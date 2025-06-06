package com.daylightcomputer.coreplugin.entity

import com.daylightcomputer.coreplugin.database.sqldefinitions.Attributes
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Collection of named timestamped values or "attributes" with assigned ID
 */
data class Entity(
    val id: String,
    private var _attributes: MutableMap<String, AttributeValueRecord>,
) {
    val attributes get(): Map<String, AttributeValueRecord> = _attributes.toMap()

    /**
     * The property has to be specified as an attribute
     */
    fun <T> required(
        attributeName: String,
        decode: (String?) -> T,
    ) = object : ReadOnlyProperty<Any?, T> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): T = decode(getRequiredAttribute(attributeName))
    }

    fun <T> requiredMutable(
        attributeName: String,
        decode: (String?) -> T,
        encode: (T) -> String?,
        timestampProvider: () -> String = issueTimestamp(),
    ) = object : ReadWriteProperty<Any?, T> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): T = decode(getRequiredAttribute(attributeName))

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: T,
        ) {
            setAttribute(attributeName, encode(value), timestampProvider)
        }
    }

    // TODO: Can also add observable which will propagate mutation-affected fields in a flow

    /**
     * The attribute does not have to be specified,
     * if it isn't the default value is returned.
     */
    fun <T> optional(
        attributeName: String,
        defaultValue: T,
        decode: (String?) -> T,
    ) = object : ReadOnlyProperty<Any?, T> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): T = decode(getAttribute(attributeName)) ?: defaultValue
    }

    fun <T> optionalMutable(
        attributeName: String,
        defaultValue: T,
        decode: (String?) -> T,
        encode: (T) -> String?,
        timestampProvider: () -> String = issueTimestamp(),
    ) = object : ReadWriteProperty<Any?, T> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): T = decode(getAttribute(attributeName)) ?: defaultValue

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: T,
        ) {
            setAttribute(attributeName, encode(value), timestampProvider)
        }
    }

    /**
     * A value which is not directly specified, but rather derived from the entity (other attributes)
     */
    fun <T> derived(derivation: (Entity) -> T) =
        object : ReadOnlyProperty<Any?, T> {
            override fun getValue(
                thisRef: Any?,
                property: KProperty<*>,
            ): T = derivation(this@Entity)
        }

    /**
     * An attribute with a given name must be explicitly listed (even if null)
     */
    fun getRequiredAttribute(attributeName: String): String? {
        if (!_attributes.containsKey(attributeName)) {
            throw IllegalStateException("Entity lacks a required attribute `$attributeName`")
        }
        return _attributes[attributeName]?.value
    }

    /**
     * DEV: Should be the only way to read an attribute
     */
    fun getAttribute(attributeName: String): String? = _attributes[attributeName]?.value

    /**
     * DEV: Should be the only way to set an attribute
     */
    fun setAttribute(
        attributeName: String,
        value: String?,
        timestampProvider: () -> String,
    ) {
        _attributes[attributeName] = AttributeValueRecord(value, timestampProvider())
    }

    companion object {
        fun issueTimestamp() =
            {
                kotlinx.datetime.Clock.System
                    .now()
                    .toString()
            }

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
