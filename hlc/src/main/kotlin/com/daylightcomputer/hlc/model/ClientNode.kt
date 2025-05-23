package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.exceptions.ClientFormatException

data class ClientNode(
    val clientNodeId: String,
) : Comparable<ClientNode>,
    Packable<ClientNode> {
    init {
        if (clientNodeId.length != encodedLength) {
            throw ClientFormatException(
                "Invalid clientNodeId Length, " +
                    "got ${clientNodeId.length} expecting $encodedLength",
            )
        }
    }

    override fun encode(): String = clientNodeId

    override fun compareTo(other: ClientNode): Int =
        clientNodeId.compareTo(other.clientNodeId)

    companion object : Packable.HelpHelp<ClientNode> {
        override val encodedLength: Int
            get() = HLCEnvironment.config.clientNodeLength

        override fun fromEncodedImpl(data: String): ClientNode =
            ClientNode(data)
    }
}
