package com.daylightcomputer.hlc.events

import com.daylightcomputer.hlc.HLC
import com.daylightcomputer.hlc.model.Timestamp

fun HLC.issueLocalEvent(): Timestamp = tick()

fun HLC.issueLocalEventPacked(): String = tick().encode()

fun HLC.send(): Timestamp = tick()

fun HLC.sendPacked(): String = tick().encode()

fun HLC.receivePacked(packedTimestamp: String): Timestamp =
    receive(Timestamp.fromEncoded(packedTimestamp))

fun HLC.receivePackedAndRepack(packedTimestamp: String): String =
    receive(Timestamp.fromEncoded(packedTimestamp)).encode()
