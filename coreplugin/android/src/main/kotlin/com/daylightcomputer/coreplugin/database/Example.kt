package com.daylightcomputer.coreplugin.database

import androidx.annotation.Keep
import com.daylightcomputer.hlc.HLC
import com.daylightcomputer.hlc.HLCConfig
import com.daylightcomputer.hlc.HLCEnvironment
import com.daylightcomputer.hlc.events.issueLocalEventPacked
import com.daylightcomputer.hlc.model.DistributedNode
import kotlinx.coroutines.delay

@Keep
class Example {
    suspend fun thinkBeforeAnswering(): String {
        HLCEnvironment.initialize(HLCConfig())
        val a = HLC(DistributedNode("abc123"))
        println(a.issueLocalEventPacked())
        delay(1000L)
        return "42"
    }
}
