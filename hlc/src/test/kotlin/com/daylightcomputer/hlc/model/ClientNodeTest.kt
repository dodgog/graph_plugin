package com.daylightcomputer.hlc.model

import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import com.daylightcomputer.hlc.HLCConfig
import com.daylightcomputer.hlc.HLCEnvironment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

// AIUSE: AI generated tests

class ClientNodeTest {
    @BeforeEach
    fun setup() {
        HLCEnvironment.resetForTests()
        HLCEnvironment.initialize(
            HLCConfig(
                clientNodeLength = 6,
            ),
        )
    }

    @Test
    fun `ClientNode initialization with valid ID works`() {
        val node = DistributedNode("node01")
        assertThat(node.clientNodeId).isEqualTo("node01")
    }

    @Test
    fun `ClientNode initialization with invalid length throws exception`() {
        assertk
            .assertFailure {
                DistributedNode("node1")
            }.hasClass(IllegalArgumentException::class.java)
    }

    @Test
    fun `ClientNode comparison works correctly`() {
        val node1 = DistributedNode("node01")
        val node2 = DistributedNode("node02")
        val node1Duplicate = DistributedNode("node01")

        assertThat(node1).isLessThan(node2)
        assertThat(node1).isEqualTo(node1Duplicate)
    }

    @Test
    fun `ClientNode packing and unpacking works`() {
        val original = DistributedNode("node12")
        val packed = original.encode()
        val unpacked = DistributedNode.fromEncoded(packed)

        assertThat(original.clientNodeId).isEqualTo(unpacked.clientNodeId)
    }

    @Test
    fun `ClientNode packing maintains correct length`() {
        val node = DistributedNode("node12")
        assertThat(node.encode().length).isEqualTo(6)
    }
}
