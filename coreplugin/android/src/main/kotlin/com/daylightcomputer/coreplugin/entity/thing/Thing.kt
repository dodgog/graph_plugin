package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.AttributeValueRecord
import com.daylightcomputer.coreplugin.entity.Entity

open class Thing(
    protected val entity: Entity,
) {
    val id: String = entity.id

    // TODO: Tanuj suggests using property delegates
    // for setting and retreiving fields

    protected fun getRequiredAttribute(attributeName: String): String =
        entity.attributes[attributeName]?.value ?: throw IllegalStateException(
            "Node lacks a required attribute `$attributeName`",
        )

    protected fun getAttribute(attributeName: String): String? =
        entity.attributes[attributeName]?.value

    /**
     * Override should concatenate with super to validate inherited fields
     */
    protected open fun getKnownFields(): Set<String> = setOf("type", "isDeleted")

    protected fun getUnknownAttributes(): Set<String> {
        val knownFields = getKnownFields()
        return entity.attributes.keys - knownFields
    }

    protected fun validateNoExtraAttributes() {
        val unknown = getUnknownAttributes()
        if (unknown.isNotEmpty()) {
            throw IllegalStateException(
                "Node has unknown attributes: ${
                    unknown.joinToString(
                        ", ",
                    ) { "`$it`" }
                }",
            )
        }
    }

    fun <T : Thing> mutateSingleAttribute(
        name: String,
        value: String,
        issueTimestamp: () -> String,
    ): Pair<Pair<String, AttributeValueRecord>, T> {
        val timestamp = issueTimestamp()
        val attributeValueRecord = (name to AttributeValueRecord(value, timestamp))
        val newEntity =
            entity.copy(
                attributes = entity.attributes + attributeValueRecord,
            )

        val newNode =
            ThingTypes.createNodeFromEntity<T>(newEntity) ?: throw IllegalStateException(
                "Failed to create node from entity",
            )

        return Pair(attributeValueRecord, newNode)
    }

    // TODO: Tanuj:also need to drop / delete the other attribute values
    //  here (except isDeleted and maybe updatedAt?)
    fun <T : Thing> delete(): Pair<Pair<String, AttributeValueRecord>, T> =
        mutateSingleAttribute("isDeleted", "true") { "TODO TIMESTAMP" }

    val type: ThingTypes =
        ThingTypes.fromString(
            getRequiredAttribute("type"),
        )

    // TODO: this could also be a getter or a lazy eval for complex properties
    val lastModifiedAtTimestamp: String =
        entity.attributes.values.maxOfOrNull { it.timestamp }
            ?: throw IllegalStateException("Node does not have attributes")

    val isDeleted: Boolean = getAttribute("isDeleted").toBoolean()

    init {
        validateNoExtraAttributes()
    }
}
