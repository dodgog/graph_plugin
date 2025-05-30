package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.Entity

class DocumentNode(
    entity: Entity,
) : Thing(entity) {
    override fun getKnownFields(): Set<String> = super.getKnownFields() + setOf("title", "author")

    val title: String = getRequiredAttribute("title")

    val author: String? = getAttribute("author")
}
