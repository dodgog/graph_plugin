package com.daylightcomputer.coreplugin.entity.node

import com.daylightcomputer.coreplugin.entity.Entity

open class Node(
    protected val entity: Entity,
) {
    val id: String = entity.id

    protected fun getRequiredAttribute(attributeName: String): String =
        entity.attributes[attributeName]?.value
            ?: throw IllegalStateException(
                "Node lacks a required attribute `$attributeName`",
            )

    protected fun getAttribute(attributeName: String): String? =
        entity.attributes[attributeName]?.value

    /**
     * Override should concatenate with super to validate inherited fields
     */
    protected open fun getKnownFields(): Set<String> =
        setOf("type", "isDeleted")

    protected fun getUnknownAttributes(): Set<String> {
        val knownFields = getKnownFields()
        return entity.attributes.keys - knownFields
    }

    protected fun validateNoExtraAttributes() {
        val unknown = getUnknownAttributes()
        if (unknown.isNotEmpty()) {
            throw IllegalStateException(
                "Node has unknown attributes: ${unknown.joinToString(
                    ", ",
                ) { "`$it`" }}",
            )
        }
    }

    val type: NodeTypes =
        NodeTypes.fromString(
            getRequiredAttribute("type"),
        )

    // TODO: this could also be a getter or a lazy eval for complex properties
    val lastModifiedAtTimestamp: String =
        entity.attributes.values.maxOfOrNull { it.timestamp }
            ?: throw IllegalStateException("Node does not have attributes")

    val isDeleted: Boolean =
        getAttribute("isDeleted").toBoolean()

    init {
        validateNoExtraAttributes()
    }
}
