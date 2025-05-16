# Hybrid Logical Clocks (HLC) for Kotlin

A Kotlin implementation of Hybrid Logical Clocks, providing a mechanism for generating timestamps that respect both the happens-before relationship and are closely tied to physical time.

## Overview

Hybrid Logical Clocks combine the best aspects of physical time and logical time to create a distributed timestamping mechanism that:

- Maintains the happens-before relationship (causality)
- Stays closely synchronized with physical time
- Detects clock drift between nodes
- Handles network delays gracefully

This implementation is based on the original HLC paper:
"Logical Physical Clocks and Consistent Snapshots in Globally Distributed Databases" by Kulkarni et al.

## Key Components

- **LogicalTimestamp**: Wrapper around Java's Instant for logical time
- **ClientNode**: Represents a node in the distributed system
- **Counter**: Logical counter for events occurring at the same logical time
- **Timestamp**: Combines logical timestamp, client node, and counter
- **HLCConfig**: Configuration for the HLC algorithm
- **HLC**: Main implementation of the Hybrid Logical Clock algorithm

## Usage

```kotlin
// Create nodes
val node1 = ClientNode("node1")
val node2 = ClientNode("node2") 

// Initialize HLC for each node
val hlc1 = HLC(node1)
val hlc2 = HLC(node2)

// Generate a timestamp for a local event on node1
val localEvent = hlc1.issueLocalEvent()

// Generate a timestamp for sending a message from node1 to node2
val sendTimestamp = hlc1.send()

// Process the timestamp on node2 when receiving the message
val receiveTimestamp = hlc2.receive(sendTimestamp)

// Working with packed (string) timestamps
val packedTimestamp = hlc1.sendPacked()
val unpackedTimestamp = hlc2.receivePacked(packedTimestamp)
```

## Configuration

The HLC algorithm can be configured with:

```kotlin
val config = HLCConfig(
    // Maximum allowed clock drift between logical and physical time (default: 1 hour)
    maxClockDriftMilliseconds = 3_600_000,
    
    // Number of characters used in hex counter representation (default: 4 chars, 0000-FFFF)
    numberOfCharactersInCounterHexRepresentation = 4,
    
    // Custom function to get physical time
    getPhysicalTime = { LogicalTimestamp(Instant.now()) }
)

val hlc = HLC(ClientNode("node"), config)
```

## Running the Demo

To run the demo application:

```bash
./gradlew run
```

## Testing

Run the tests with:

```bash
./gradlew test
```

## License

This library is released under the MIT License. 