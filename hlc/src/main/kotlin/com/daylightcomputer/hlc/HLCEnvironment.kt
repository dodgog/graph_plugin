package com.daylightcomputer.hlc

import java.util.concurrent.atomic.AtomicReference

object HLCEnvironment {
    private val ref: AtomicReference<HLCConfig> = AtomicReference()

    val config: HLCConfig
        get() = ref.get()
            ?: throw IllegalStateException(
            "HLCEnvironment has not been initialised. Call initialize() once at application start-up."
        )

    fun initialize(config: HLCConfig) {
        if (!ref.compareAndSet(null, config)) {
            throw IllegalStateException("HLCEnvironment has already been initialised")
        }
    }

    internal fun resetForTests() {
        ref.set(null)
    }
} 