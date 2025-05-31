package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.Entity

class DocumentNode(
    entity: Entity,
) : INode by Node(entity) {
    val author: String? by entity.optional(
        "author",
        defaultValue = null,
        transform = { it },
    )
}
