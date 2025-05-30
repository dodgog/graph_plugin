package com.daylightcomputer.hlc.events

import com.daylightcomputer.hlc.HLC
import com.daylightcomputer.hlc.model.Counter
import com.daylightcomputer.hlc.model.LogicalTimestamp
import com.daylightcomputer.hlc.model.Timestamp
import kotlinx.datetime.Instant

fun HLC.issueLocalEvent(): Timestamp = tick()

fun HLC.issueLocalEventPacked(): String = tick().encode()

fun HLC.send(): Timestamp = tick()

fun HLC.sendPacked(): String = tick().encode()

fun HLC.receivePacked(packedTimestamp: String): Timestamp =
    receive(Timestamp.fromEncoded(packedTimestamp))

fun HLC.receivePackedAndRepack(packedTimestamp: String): String =
    receive(Timestamp.fromEncoded(packedTimestamp)).encode()

fun HLC.getZeroTimestamp(): Timestamp =
    Timestamp(
        LogicalTimestamp(Instant.fromEpochMilliseconds(0)),
        this.distributedNode,
        Counter(0),
    )

fun HLC.getZeroTimestampPacked(): String =
    Timestamp(
        LogicalTimestamp(Instant.fromEpochMilliseconds(0)),
        this.distributedNode,
        Counter(0),
    ).encode()
