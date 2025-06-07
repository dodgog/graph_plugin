package com.daylightcomputer.coreplugin.cli

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.daylightcomputer.coreplugin.database.EventsAttributesDatabase
import com.daylightcomputer.coreplugin.database.sqldefinitions.Database
import com.daylightcomputer.coreplugin.entity.AttributeValueRecord
import com.daylightcomputer.coreplugin.entity.Entity
import com.daylightcomputer.coreplugin.entity.TimestampProvider
import com.daylightcomputer.coreplugin.entity.insertAllAttributes
import com.daylightcomputer.coreplugin.entity.thing.DocumentNode
import com.daylightcomputer.coreplugin.entity.thing.ThingTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import java.util.UUID
import com.daylightcomputer.hlc.events.issueLocalEventPacked

// AIGEN vibecoded

/**
 *
 * Commands:
 *   n  – create a new document
 *   e  – edit (mutate) an existing document (thought/comment)
 *   l  – list all documents
 *   q  – quit the application
 *
 * This utility intentionally keeps dependencies light and runs fully in-memory using
 * SQLDelight's `JdbcSqliteDriver` (no external DB setup required).
 */
object DocumentCLI {
    // In-memory database & coroutine scope shared for the whole session
    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    private val database: Database
    private val scope: CoroutineScope

    init {
        Database.Schema.create(driver)
        database = Database(driver)
        scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        // Ensure a clean singleton and initialise it for use from the CLI
        EventsAttributesDatabase.resetForTesting()
        EventsAttributesDatabase.initialize(database, clientId = "cli001", scope = scope)
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        println("Welcome to DocumentCLI — minimal knowledge-graph playground\n")
        while (true) {
            println("Commands:  n = new   e = edit   l = list   q = quit")
            print(": ")
            val command = readLine() ?: break        // EOF → leave loop

            when (command.trim().lowercase()) {
                "n" -> createDocument()
                "e" -> mutateDocument()
                "l" -> listDocuments()
                "q" -> break
                else -> println("Unknown command.")
            }
            println()
        }
        println("Bye!")
    }

    private fun createDocument() {
        println("\n— Create new document —")
        print("Title: ")
        val title = readLine()?.ifBlank { null }
        if (title == null) {
            println("❌ Title is required.")
            return
        }

        print("Author (optional): ")
        val author = readLine()?.ifBlank { null }
        print("Initial thought: ")
        val thought = readLine()?.ifBlank { null }

        val id = UUID.randomUUID().toString()
        val timestampProvider = TimestampProvider { EventsAttributesDatabase.hlc.issueLocalEventPacked() }

        val attrs = mutableMapOf(
            "type" to AttributeValueRecord(ThingTypes.DOCUMENT_NODE.stringValue, timestampProvider.issueTimestamp()),
            "title" to AttributeValueRecord(title, timestampProvider.issueTimestamp()),
            // `thought` is requiredMutable on DocumentNode, so provide even if null (for safety use empty string)
            "thought" to AttributeValueRecord(thought, timestampProvider.issueTimestamp()),
        )
        if (author != null) {
            attrs["author"] = AttributeValueRecord(author, timestampProvider.issueTimestamp())
        }

        val entity = Entity(id, attrs, timestampProvider)
        database.transaction { entity.insertAllAttributes() }

        println("✅ Created document $id")
    }

    private fun mutateDocument() {
        println("\n— Edit document —")
        print("Document id: ")
        val id = readLine()?.trim().orEmpty()
        if (id.isBlank()) {
            println("❌ Invalid id.")
            return
        }

        val entity = try {
            EventsAttributesDatabase.getEntity(id)
        } catch (e: Exception) {
            println("❌ Could not find entity with id '$id'.")
            return
        }

        val doc = toDocumentNode(entity)
        if (doc == null) {
            println("❌ Entity '$id' is not a DocumentNode.")
            return
        }

        println("Editing: ${doc.title} (id=$id)")
        println("Current thought: ${doc.thought}")
        println("Current comment: ${doc.comment}")
        print("Set new thought (blank to keep): ")
        val newThought = readLine()
        if (!newThought.isNullOrBlank()) doc.thought = newThought
        print("Set new comment (blank to keep): ")
        val newComment = readLine()
        if (newComment != null) {
            // Allow explicit null by entering "null"
            doc.comment = if (newComment.lowercase() == "null") null else newComment.ifBlank { doc.comment }
        }
        println("✅ Updated document $id")
    }

    private fun listDocuments() {
        val docs = EventsAttributesDatabase
            .getAllEntities()
            .mapNotNull { toDocumentNode(it) }
            .toList()

        if (docs.isEmpty()) {
            println("(no documents)")
            return
        }

        println("\n— Documents —")
        docs.forEachIndexed { index, doc ->
            println("")
            println("${index + 1}. id=${doc.id}")
            println("   title:   ${doc.title}")
            println("   author:  ${doc.author ?: "-"}")
            println("   thought: ${doc.thought}")
            println("   comment: ${doc.comment ?: "-"}")
            println("   isDel:   ${doc.isDeleted}")
            println("   modified:${doc.lastModifiedAtTimestamp}")
        }
    }

    // Helper – safe conversion (DocumentNode is NOT a subtype of Thing)
    private fun toDocumentNode(entity: Entity): DocumentNode? =
        if (entity.attributes["type"]?.value
                ?.equals(ThingTypes.DOCUMENT_NODE.stringValue, ignoreCase = true) == true
        ) DocumentNode(entity) else null
}
