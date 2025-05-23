package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.HLCEnvironment

data class ClientNode(val clientNodeId: String) : Comparable<ClientNode>, Packable<ClientNode> {
    init {
        require(clientNodeId.length == packedLength)
    }

    companion object : Packable.HelpHelp<ClientNode> {
        override val packedLength: Int
            get() = HLCEnvironment.config.clientNodeLength
        override fun fromPackedImpl(data: String): ClientNode {
            return ClientNode(data)
        }
    }
    
    override fun pack(): String {
        return clientNodeId
    }
    
    override fun compareTo(other: ClientNode): Int {
        return clientNodeId.compareTo(other.clientNodeId)
    }
}