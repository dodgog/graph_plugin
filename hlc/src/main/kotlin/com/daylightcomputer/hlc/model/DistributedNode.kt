package com.daylightcomputer.hlc.model

import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.exceptions.TimestampFormatException

data class DistributedNode(
    val clientNodeId: String,
) : Comparable<DistributedNode>,
    Packable<DistributedNode> {
    init {
        if (clientNodeId.length != encodedLength) {
            throw TimestampFormatException(
                "Invalid clientNodeId Length, " +
                    "got ${clientNodeId.length} expecting $encodedLength",
            )
        }
    }

    override fun encode(): String = clientNodeId

    override fun compareTo(other: DistributedNode): Int = clientNodeId.compareTo(other.clientNodeId)

    companion object : Packable.HelpHelp<DistributedNode> {
        override val encodedLength: Int
            get() = HLCEnvironment.config.distributedNodeLength

        override fun fromEncodedImpl(data: String): DistributedNode = DistributedNode(data)
    }
}
