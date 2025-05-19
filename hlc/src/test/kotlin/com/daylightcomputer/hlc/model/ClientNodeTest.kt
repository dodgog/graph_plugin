package com.daylightcomputer.hlc.model

import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.HLCConfig

// AIUSE: AI generated tests

class ClientNodeTest {
    @BeforeEach
    fun setup() {
        HLCEnvironment.resetForTests()
        HLCEnvironment.initialize(
            HLCConfig(
                numberOfCharactersInClientNodeRepresentation = 6
            )
        )
    }

    @Test
    fun `ClientNode initialization with valid ID works`() {
        val node = ClientNode("node01")
        assertThat(node.clientNodeId).isEqualTo("node01")
    }

    @Test
    fun `ClientNode initialization with invalid length throws exception`() {
        assertk.assertFailure {
            ClientNode("node1")
        }.hasClass(IllegalArgumentException::class.java)
    }

    @Test
    fun `ClientNode comparison works correctly`() {
        val node1 = ClientNode("node01")
        val node2 = ClientNode("node02")
        val node1Duplicate = ClientNode("node01")

        assertThat(node1).isLessThan(node2)
        assertThat(node1).isEqualTo(node1Duplicate)
    }

    @Test
    fun `ClientNode packing and unpacking works`() {
        val original = ClientNode("node12")
        val packed = original.pack()
        val unpacked = ClientNode.fromPacked(packed)

        assertThat(original.clientNodeId).isEqualTo(unpacked.clientNodeId)
    }

    @Test
    fun `ClientNode packing maintains correct length`() {
        val node = ClientNode("node12")
        assertThat(node.pack().length).isEqualTo(6)
    }
} 