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
class Entity(
    val id: String,
    private var _attributes: MutableMap<String, AttributeValueRecord>,
    private val timestampProvider: TimestampProvider,
) {
    val attributes get(): Map<String, AttributeValueRecord> = _attributes.toMap()

    /**
     * Emit changes for events to be constructed and reduced
     *
     * TODO: the replay was added because the first value would get lost due to couroutine setup i would guess
     */
    private val _attributeChanges = MutableSharedFlow<Pair<String, AttributeValueRecord>>(1)

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
            setAttribute(attributeName, encode(value))
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
            setAttribute(attributeName, encode(value))
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
    ) {
        val record = AttributeValueRecord(value, timestampProvider.issueTimestamp())
        _attributes[attributeName] = record
        // TODO: what if this fails?
        _attributeChanges.tryEmit(attributeName to record) // Non-suspend emit!
    }

    // NOTE: Not using a data class, because then the timestamp provider becomes a value
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Entity

        if (id != other.id) return false
        if (_attributes != other._attributes) return false

        return true
    }

    override fun hashCode(): Int = 31 * id.hashCode() + _attributes.hashCode()

    override fun toString(): String = "Entity(id='$id', attributes=$_attributes)"

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
        ): Entity = Entity(id, attributes.toAttributeMap().toMutableMap(), timestampProvider)

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
                Entity(entityId, attrs.toAttributeMap().toMutableMap(), timestampProvider)
            }
    }
}
