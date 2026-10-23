package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val sender: String, // "user", "jarvis", "system"
  val message: String,
  val timestamp: Long = System.currentTimeMillis(),
  val actionType: String? = null, // "YOUTUBE", "CALL", "FLASHLIGHT", "SHEET", "ALARM", "CALENDAR", "FILE_READ", "MEMORY", "IMAGE", "CALL_SCREEN"
  val actionDetails: String? = null,
  val actionStatus: String? = null // "EXECUTED", "PENDING", "FAILED"
)
