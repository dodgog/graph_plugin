package com.daylightcomputer.coreplugin.entity.thing

import com.daylightcomputer.coreplugin.entity.Entity

open class Thing(
    val entity: Entity,
) : IThing {
    override val id: String = entity.id

    // TODO: Tanuj suggests using property delegates
    // for setting and retreiving fields

//    /**
//     * Override should concatenate with super to validate inherited fields
//     */
//    override fun getKnownFields(): Set<String> = setOf("type", "isDeleted")
//
//    override fun getUnknownAttributes(): Set<String> {
//        val knownFields = getKnownFields()
//        return entity.attributes.keys - knownFields
//    }
//
//    protected fun validateNoExtraAttributes() {
//        val unknown = getUnknownAttributes()
//        if (unknown.isNotEmpty()) {
//            throw IllegalStateException(
//                "Node has unknown attributes: ${
//                    unknown.joinToString(
//                        ", ",
//                    ) { "`$it`" }
//                }",
//            )
//        }
//    }

//    override fun <T : Thing> mutateSingleAttribute(
//        name: String,
//        value: String,
//        issueTimestamp: () -> String,
//    ): Pair<Pair<String, AttributeValueRecord>, T> {
//        val timestamp = issueTimestamp()
//        val attributeValueRecord = (name to AttributeValueRecord(value, timestamp))
//        val newEntity =
//            entity.copy(
//                _attributes = (entity.attributes + attributeValueRecord).toMutableMap(),
//            )
//
//        val newNode =
//            ThingTypes.createNodeFromEntity<T>(newEntity) ?: throw IllegalStateException(
//                "Failed to create node from entity",
//            )
//
//        return Pair(attributeValueRecord, newNode)
//    }
//
//    // TODO: Tanuj:also need to drop / delete the other attribute values
//    //  here (except isDeleted and maybe updatedAt?)
//    fun <T : Thing> delete(): Pair<Pair<String, AttributeValueRecord>, T> =
//        mutateSingleAttribute("isDeleted", "true") { "TODO TIMESTAMP" }

    //    override val type: ThingTypes =
//        ThingTypes.fromString(
//            entity.getRequiredAttribute("type"),
//        )
    override val type: ThingTypes by entity.required(
        "type",
        transform = { ThingTypes.fromString(it) },
    )

    // TODO: this will throw if error
    override val lastModifiedAtTimestamp: String by entity.derived {
        it.attributes.values.maxOf { valuePair ->
            valuePair.timestamp
        }
    }

    override val isDeleted: Boolean by entity.optional(
        "isDeleted",
        defaultValue = false,
        transform = { it.toBoolean() },
    )

//    init {
//        validateNoExtraAttributes()
//    }
}
