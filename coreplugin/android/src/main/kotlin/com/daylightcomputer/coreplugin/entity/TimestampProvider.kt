package com.daylightcomputer.coreplugin.entity

/**
 * Note: Single abstract method!
 */
fun interface TimestampProvider {
    fun issueTimestamp(): String
}
