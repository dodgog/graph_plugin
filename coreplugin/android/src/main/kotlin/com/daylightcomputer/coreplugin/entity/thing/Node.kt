package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.Entity

// TODO: is this a combination of inheritance and compopsition still technically?
// Or since Node actually only brings contract im fine
class Node(
    entity: Entity,
) : INode by Node(entity) {
    override val title: String by entity.required(
        "title",
        transform = { it },
    )
}
