package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "chat_sessions")
data class SavedChatSession(
    @PrimaryKey val id: String,
    val title: String,
    val agentName: String,
    val agentMode: String, // "text" or "image"
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = SavedChatSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class SavedChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: String, // "user", "assistant", "error"
    val content: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
