package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.Entity

class Node(
    entity: Entity,
) : INode by Node(entity) {
    override val title: String by entity.required(
        "title",
        // TODO add nullable
        decode = { it!! },
    )
}
