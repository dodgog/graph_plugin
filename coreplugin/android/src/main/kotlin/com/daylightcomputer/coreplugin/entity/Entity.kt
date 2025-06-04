package com.daylightcomputer.coreplugin.entity

import com.daylightcomputer.coreplugin.database.sqldefinitions.Attributes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Collection of named timestamped values or "attributes" with assigned ID
 */
data class Entity(
    val id: String,
    private var _attributes: MutableMap<String, AttributeValueRecord>,
    private val timestampProvider: TimestampProvider,
) {
    val attributes get(): Map<String, AttributeValueRecord> = _attributes.toMap()

    /**
     * Emit changes for events to be constructed and reduced
     */
    private val _attributeChanges = MutableSharedFlow<Pair<String, AttributeValueRecord>>()

    /**
     * Public consumable event flow without emission rights
     */
    val attributeChanges = _attributeChanges.asSharedFlow()

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

    /**
     * The property has to be specified as an attribute
     * Setting triggers the event emission mechanism
     */
    fun <T> requiredMutable(
        attributeName: String,
        decode: (String?) -> T,
        encode: (T) -> String?,
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

    /**
     * The attribute does not have to be specified,
     * if it isn't the default value is returned.
     * Setting triggers the event emission mechanism
     */
    fun <T> optionalMutable(
        attributeName: String,
        defaultValue: T,
        decode: (String?) -> T,
        encode: (T) -> String?,
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
        timestampProvider: TimestampProvider,
    ) {
        val record = AttributeValueRecord(value, timestampProvider.issueTimestamp())
        _attributes[attributeName] = record
        // TODO: what if this fails?
        _attributeChanges.tryEmit(attributeName to record) // Non-suspend emit!
    }

    companion object {
        // TODO: move these methods up to where the timestamp provider is created (closer to the db)

        /**
         * From an arbitrary list of attributes find entity with id
         * TODO: is sequence appropriate here?
         * */
        fun fromAttributePool(
            id: String,
            attributes: List<Attributes>,
            timestampProvider: TimestampProvider,
        ): Entity {
            val map =
                attributes.filter { it.entity_id == id }.associate {
                    it.attr_name to AttributeValueRecord(it.attr_val, it.timestamp)
                }
            return Entity(id, map.toMutableMap(), timestampProvider)
        }

        /**
         * From an arbitrary collection of attributes
         * get all possible entities (by distinct id's).
         * CAUTION: lazy evaluation with sequences.
         * */
        fun allFromAttributePool(
            attributes: List<Attributes>,
            timestampProvider: TimestampProvider,
        ): Sequence<Entity> =
            attributes.groupBy { it.entity_id }.asSequence().map { (entityId, attrs) ->
                val attributeMap =
                    attrs.associate {
                        it.attr_name to AttributeValueRecord(it.attr_val, it.timestamp)
                    }
                Entity(entityId, attributeMap.toMutableMap(), timestampProvider)
            }
    }
}
