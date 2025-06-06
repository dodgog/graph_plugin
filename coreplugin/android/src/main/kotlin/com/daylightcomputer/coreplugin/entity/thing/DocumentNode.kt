package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.Entity

/**
 * A node in the graph carrying a viewable document
 */
class DocumentNode(
    private val entity: Entity,
) : IDocumentNode,
    INode by Node(entity) {
    override val author: String? by entity.optional(
        "author",
        defaultValue = null,
        decode = { it },
    )

    override var thought: String? by entity.requiredMutable(
        "thought",
        { it },
        { it },
    )

    override fun validatePropertiesOnInit() {
        // TODO: add thoguht and other required fields
    }

    init {
        validatePropertiesOnInit()
    }
}
