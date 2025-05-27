package com.daylightcomputer.coreplugin.database.sqldefinitions

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.Test

/**
 * AIUSE: used genai to generate
 *
 * Comprehensive test suite for the config table covering all queries,
 * edge cases, and special behaviors like triggers and INSERT OR REPLACE.
 */
class ConfigTableTests {
    @Test
    fun `initializeConfig should insert default values`() =
        testing { db ->
            // Initialize config
            db.configQueries.initializeConfig()

            // Verify all default values are inserted
            val allConfig =
                db.configQueries
                    .getAllConfigValues()
                    .executeAsList()
            assertThat(allConfig).hasSize(5)

            // Check specific default values
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "client_id",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isNull()

            assertThat(
                db.configQueries
                    .getValueForKey(
                        "last_server_issued_timestamp",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo("1970-01-01T00:00:01.000Z-0000-serverId")

            assertThat(
                db.configQueries
                    .getValueForKey(
                        "user_id",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isNull()

            assertThat(
                db.configQueries
                    .getValueForKey(
                        "user_token",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isNull()

            assertThat(
                db.configQueries
                    .getValueForKey(
                        "hlc_absolute_zero",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo("1970-01-01T00:00:01.000Z-0000")
        }

    @Test
    fun `initializeConfig should not overwrite existing values`() =
        testing { db ->
            // Set a value first
            db.configQueries.setValueForKey("client_id", "existing_client")

            // Initialize config
            db.configQueries.initializeConfig()

            // Existing value should be preserved (INSERT OR IGNORE behavior)
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "client_id",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo("existing_client")
        }

    @Test
    fun `setValueForKey and getValueForKey should work correctly`() =
        testing { db ->
            val key = "test_key"
            val value = "test_value"

            // Initially should return null
            assertThat(
                db.configQueries.getValueForKey(key).executeAsOneOrNull(),
            ).isNull()

            // Set value
            db.configQueries.setValueForKey(key, value)

            // Should return the set value
            assertThat(
                db.configQueries
                    .getValueForKey(
                        key,
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(value)
        }

    @Test
    fun `setValueForKey should use INSERT OR REPLACE behavior`() =
        testing { db ->
            val key = "test_key"
            val firstValue = "first_value"
            val secondValue = "second_value"

            // Insert first value
            db.configQueries.setValueForKey(key, firstValue)
            assertThat(
                db.configQueries
                    .getValueForKey(
                        key,
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(firstValue)

            // Replace with second value
            db.configQueries.setValueForKey(key, secondValue)
            assertThat(
                db.configQueries
                    .getValueForKey(
                        key,
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(secondValue)

            // Should still only have one row for this key
            val allConfig =
                db.configQueries
                    .getAllConfigValues()
                    .executeAsList()
            val matchingRows = allConfig.filter { it.key == key }
            assertThat(matchingRows).hasSize(1)
        }

    @Test
    fun `setValueForKey should handle null values`() =
        testing { db ->
            val key = "test_key"

            // Set to non-null first
            db.configQueries.setValueForKey(key, "some_value")
            assertThat(
                db.configQueries
                    .getValueForKey(
                        key,
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo("some_value")

            // Set to null
            db.configQueries.setValueForKey(key, null)
            assertThat(
                db.configQueries
                    .getValueForKey(
                        key,
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isNull()
        }

    @Test
    fun `getAllConfigValues should return all config entries`() =
        testing { db ->
            // Initially empty
            val initialConfig =
                db.configQueries
                    .getAllConfigValues()
                    .executeAsList()
            assertThat(initialConfig).isEmpty()

            // Add some entries
            db.configQueries.setValueForKey("key1", "value1")
            db.configQueries.setValueForKey("key2", "value2")
            db.configQueries.setValueForKey("key3", null)

            val allConfig =
                db.configQueries
                    .getAllConfigValues()
                    .executeAsList()
            assertThat(allConfig).hasSize(3)

            // Verify all entries are present
            val keys = allConfig.map { it.key }
            assertThat(keys).contains("key1")
            assertThat(keys).contains("key2")
            assertThat(keys).contains("key3")

            // Verify values
            val key1Entry = allConfig.find { it.key == "key1" }
            val key2Entry = allConfig.find { it.key == "key2" }
            val key3Entry = allConfig.find { it.key == "key3" }

            assertThat(key1Entry?.conf_value).isEqualTo("value1")
            assertThat(key2Entry?.conf_value).isEqualTo("value2")
            assertThat(key3Entry?.conf_value).isNull()

            // All should have time_modified set
            assertThat(key1Entry?.time_modified).isNotNull()
            assertThat(key2Entry?.time_modified).isNotNull()
            assertThat(key3Entry?.time_modified).isNotNull()
        }

    @Test
    fun `setLastSyncTime should work correctly`() =
        testing { db ->
            val syncTime = "2024-01-01T12:00:00Z"

            // Set sync time
            db.configQueries.setLastSyncTime(syncTime)

            // Verify it was set
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "last_server_issued_timestamp",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(syncTime)
        }

    @Test
    fun `setClientId should work correctly`() =
        testing { db ->
            val clientId = "test_client_123"

            // Set client ID
            db.configQueries.setClientId(clientId)

            // Verify it was set
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "client_id",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(clientId)
        }

    @Test
    fun `setUserId should work correctly`() =
        testing { db ->
            val userId = "test_user_456"

            // Set user ID
            db.configQueries.setUserId(userId)

            // Verify it was set
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "user_id",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(userId)
        }

    @Test
    fun `setUserToken should work correctly`() =
        testing { db ->
            val userToken = "jwt_token_xyz"

            // Set user token
            db.configQueries.setUserToken(userToken)

            // Verify it was set
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "user_token",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(userToken)
        }

    @Test
    fun `empty string values should be handled correctly`() =
        testing { db ->
            val key = "empty_test"
            val emptyValue = ""

            // Set empty string value
            db.configQueries.setValueForKey(key, emptyValue)

            // Should return empty string, not null
            assertThat(
                db.configQueries
                    .getValueForKey(
                        key,
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(emptyValue)
        }

    @Test
    fun `special characters and unicode should be handled correctly`() =
        testing { db ->
            val specialKey = "special!@#$%^&*()"
            val unicodeValue = "测试 🚀 emojis αβγ"

            db.configQueries.setValueForKey(specialKey, unicodeValue)

            assertThat(
                db.configQueries
                    .getValueForKey(
                        specialKey,
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(unicodeValue)
        }

    @Test
    fun `large values should be handled correctly`() =
        testing { db ->
            val largeKey = "large_key_" + "x".repeat(1000)
            val largeValue = "x".repeat(10000) // 10KB string

            db.configQueries.setValueForKey(largeKey, largeValue)

            assertThat(
                db.configQueries
                    .getValueForKey(
                        largeKey,
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(largeValue)
        }

    @Test
    fun `case sensitivity should be preserved`() =
        testing { db ->
            // Test case sensitivity in keys
            db.configQueries.setValueForKey("testkey", "lowercase")
            db.configQueries.setValueForKey("TestKey", "mixedcase")
            db.configQueries.setValueForKey("TESTKEY", "uppercase")

            // All should be treated as different keys
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "testkey",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo("lowercase")
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "TestKey",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo("mixedcase")
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "TESTKEY",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo("uppercase")

            val allConfig =
                db.configQueries
                    .getAllConfigValues()
                    .executeAsList()
            assertThat(allConfig).hasSize(3)
        }

    @Test
    fun `null-like string values should be handled correctly`() =
        testing { db ->
            val nullLikeValues =
                listOf("null", "NULL", "nil", "NIL", "undefined", "UNDEFINED")

            nullLikeValues.forEachIndexed { index, nullLikeValue ->
                db.configQueries.setValueForKey("key_$index", nullLikeValue)
            }

            // Verify all null-like values are preserved as strings
            nullLikeValues.forEachIndexed { index, expectedValue ->
                assertThat(
                    db.configQueries
                        .getValueForKey(
                            "key_$index",
                        ).executeAsOneOrNull()
                        ?.conf_value,
                ).isEqualTo(expectedValue)
            }
        }

    @Test
    fun `multiple operations should maintain data integrity`() =
        testing { db ->
            val operations =
                listOf(
                    "key1" to "value1",
                    "key2" to null,
                    "key1" to "updated_value1", // Update existing
                    "key3" to "value3",
                    "key2" to "value2", // Update null to non-null
                    "key3" to null, // Update non-null to null
                )

            operations.forEach { (key, value) ->
                db.configQueries.setValueForKey(key, value)
            }

            // Verify final state
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "key1",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo("updated_value1")
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "key2",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo("value2")
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "key3",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isNull()

            val allConfig =
                db.configQueries
                    .getAllConfigValues()
                    .executeAsList()
            assertThat(allConfig).hasSize(3)
        }

    @Test
    fun `time_modified should be set automatically`() =
        testing { db ->
            val key = "time_test"
            val value = "test_value"

            // Set a value
            db.configQueries.setValueForKey(key, value)

            // Get the config entry with time_modified
            val configEntries =
                db.configQueries
                    .getAllConfigValues()
                    .executeAsList()
            val entry = configEntries.find { it.key == key }

            assertThat(entry).isNotNull()
            assertThat(entry!!.time_modified).isNotNull()

            // time_modified should be in ISO format (though we can't predict exact value)
            assertThat(entry.time_modified?.length).isEqualTo(24)
        }

    @Test
    fun `whitespace in keys and values should be preserved`() =
        testing { db ->
            val keyWithSpaces = "  key with spaces  "
            val valueWithSpaces = "  value with spaces  "

            db.configQueries.setValueForKey(keyWithSpaces, valueWithSpaces)

            // Verify spaces are preserved exactly
            assertThat(
                db.configQueries
                    .getValueForKey(
                        keyWithSpaces,
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(valueWithSpaces)

            val allConfig =
                db.configQueries
                    .getAllConfigValues()
                    .executeAsList()
            val entry = allConfig.find { it.key == keyWithSpaces }
            assertThat(entry?.conf_value).isEqualTo(valueWithSpaces)
        }

    @Test
    fun `repeated identical operations should be idempotent`() =
        testing { db ->
            val key = "idempotent_test"
            val value = "test_value"

            // Set same value multiple times
            repeat(5) {
                db.configQueries.setValueForKey(key, value)
            }

            // Should still only have one entry
            val allConfig =
                db.configQueries
                    .getAllConfigValues()
                    .executeAsList()
            val matchingEntries = allConfig.filter { it.key == key }
            assertThat(matchingEntries).hasSize(1)
            assertThat(matchingEntries.first().conf_value).isEqualTo(value)
        }

    @Test
    fun `all specialized setters should work correctly together`() =
        testing { db ->
            val clientId = "client_123"
            val userId = "user_456"
            val userToken = "token_789"
            val syncTime = "2024-01-01T12:00:00Z"

            // Use all specialized setters
            db.configQueries.setClientId(clientId)
            db.configQueries.setUserId(userId)
            db.configQueries.setUserToken(userToken)
            db.configQueries.setLastSyncTime(syncTime)

            // Verify all values are set correctly
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "client_id",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(clientId)
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "user_id",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(userId)
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "user_token",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(userToken)
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "last_server_issued_timestamp",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo(syncTime)

            // Should have exactly 4 entries
            val allConfig =
                db.configQueries
                    .getAllConfigValues()
                    .executeAsList()
            assertThat(allConfig).hasSize(4)
        }

    @Test
    fun `initialize config combined with manual operations`() =
        testing { db ->
            // First set some manual values
            db.configQueries.setValueForKey("custom_key", "custom_value")
            db.configQueries.setClientId("manual_client")

            // Then initialize - should not overwrite existing values
            db.configQueries.initializeConfig()

            val allConfig =
                db.configQueries
                    .getAllConfigValues()
                    .executeAsList()

            // Should have default entries plus custom ones
            assertThat(allConfig.size).isEqualTo(6) // 5 default + 1 custom

            // Custom values should be preserved
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "custom_key",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo("custom_value")
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "client_id",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo("manual_client")

            // Other defaults should be set
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "user_id",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isNull()
            assertThat(
                db.configQueries
                    .getValueForKey(
                        "hlc_absolute_zero",
                    ).executeAsOneOrNull()
                    ?.conf_value,
            ).isEqualTo("1970-01-01T00:00:01.000Z-0000")
        }
}
