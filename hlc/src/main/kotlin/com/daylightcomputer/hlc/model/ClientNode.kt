package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.exceptions.ClientFormatException

data class ClientNode(val clientNodeId: String) : Comparable<ClientNode>, Packable<ClientNode> {
    init {
        if(clientNodeId.length != packedLength) {
            throw ClientFormatException("Invalid clientNodeId Length, got ${clientNodeId.length} expecting ${packedLength}")
        }
    }

    override fun pack(): String {
        return clientNodeId
    }

    override fun compareTo(other: ClientNode): Int {
        return clientNodeId.compareTo(other.clientNodeId)
    }

    companion object : Packable.HelpHelp<ClientNode> {
        override val packedLength: Int
            get() = HLCEnvironment.config.clientNodeLength

        override fun fromPackedImpl(data: String): ClientNode {
            return ClientNode(data)
        }
    }
}