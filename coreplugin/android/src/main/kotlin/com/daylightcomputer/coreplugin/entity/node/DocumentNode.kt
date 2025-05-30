package com.daylightcomputer.coreplugin.entity.node

import com.daylightcomputer.coreplugin.entity.Entity

class DocumentNode(
    entity: Entity,
) : Node(entity) {
    override fun getKnownFields(): Set<String> = super.getKnownFields() + setOf("title", "author")

    val title: String = getRequiredAttribute("title")

    val author: String? = getAttribute("author")
}
