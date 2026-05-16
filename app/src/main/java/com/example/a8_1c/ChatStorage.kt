package com.example.a8_1c

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

enum class SenderType {
    USER,
    BOT
}

data class ChatMessage(
    val id: Long,
    val username: String,
    val messageText: String,
    val senderType: SenderType,
    val timestampMillis: Long
)

class ChatStorage(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_MESSAGES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT NOT NULL,
                $COL_MESSAGE_TEXT TEXT NOT NULL,
                $COL_SENDER_TYPE TEXT NOT NULL,
                $COL_TIMESTAMP_MILLIS INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        onCreate(db)
    }

    fun insertMessage(username: String, messageText: String, senderType: SenderType, timestampMillis: Long): Long {
        val values = ContentValues().apply {
            put(COL_USERNAME, username)
            put(COL_MESSAGE_TEXT, messageText)
            put(COL_SENDER_TYPE, senderType.name)
            put(COL_TIMESTAMP_MILLIS, timestampMillis)
        }
        return writableDatabase.insert(TABLE_MESSAGES, null, values)
    }

    fun readAllMessagesSorted(): List<ChatMessage> {
        val query = """
            SELECT $COL_ID, $COL_USERNAME, $COL_MESSAGE_TEXT, $COL_SENDER_TYPE, $COL_TIMESTAMP_MILLIS
            FROM $TABLE_MESSAGES
            ORDER BY $COL_TIMESTAMP_MILLIS ASC, $COL_ID ASC
        """.trimIndent()

        val messages = mutableListOf<ChatMessage>()
        readableDatabase.rawQuery(query, null).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COL_ID)
            val usernameIndex = cursor.getColumnIndexOrThrow(COL_USERNAME)
            val messageIndex = cursor.getColumnIndexOrThrow(COL_MESSAGE_TEXT)
            val senderIndex = cursor.getColumnIndexOrThrow(COL_SENDER_TYPE)
            val timestampIndex = cursor.getColumnIndexOrThrow(COL_TIMESTAMP_MILLIS)

            while (cursor.moveToNext()) {
                messages.add(
                    ChatMessage(
                        id = cursor.getLong(idIndex),
                        username = cursor.getString(usernameIndex),
                        messageText = cursor.getString(messageIndex),
                        senderType = SenderType.valueOf(cursor.getString(senderIndex)),
                        timestampMillis = cursor.getLong(timestampIndex)
                    )
                )
            }
        }
        return messages
    }

    fun readMessagesByUsernameSorted(username: String): List<ChatMessage> {
        val query = """
            SELECT $COL_ID, $COL_USERNAME, $COL_MESSAGE_TEXT, $COL_SENDER_TYPE, $COL_TIMESTAMP_MILLIS
            FROM $TABLE_MESSAGES
            WHERE $COL_USERNAME = ?
            ORDER BY $COL_TIMESTAMP_MILLIS ASC, $COL_ID ASC
        """.trimIndent()

        val messages = mutableListOf<ChatMessage>()
        readableDatabase.rawQuery(query, arrayOf(username)).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COL_ID)
            val usernameIndex = cursor.getColumnIndexOrThrow(COL_USERNAME)
            val messageIndex = cursor.getColumnIndexOrThrow(COL_MESSAGE_TEXT)
            val senderIndex = cursor.getColumnIndexOrThrow(COL_SENDER_TYPE)
            val timestampIndex = cursor.getColumnIndexOrThrow(COL_TIMESTAMP_MILLIS)

            while (cursor.moveToNext()) {
                messages.add(
                    ChatMessage(
                        id = cursor.getLong(idIndex),
                        username = cursor.getString(usernameIndex),
                        messageText = cursor.getString(messageIndex),
                        senderType = SenderType.valueOf(cursor.getString(senderIndex)),
                        timestampMillis = cursor.getLong(timestampIndex)
                    )
                )
            }
        }
        return messages
    }

    fun clearMessagesByUsername(username: String) {
        writableDatabase.delete(
            TABLE_MESSAGES,
            "$COL_USERNAME = ?",
            arrayOf(username)
        )
    }

    companion object {
        private const val DATABASE_NAME = "chat_storage.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_MESSAGES = "messages"
        private const val COL_ID = "id"
        private const val COL_USERNAME = "username"
        private const val COL_MESSAGE_TEXT = "message_text"
        private const val COL_SENDER_TYPE = "sender_type"
        private const val COL_TIMESTAMP_MILLIS = "timestamp_millis"
    }
}
