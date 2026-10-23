package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jarvis_memories")
data class JarvisMemory(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val keyTitle: String,
  val content: String,
  val category: String = "General", // "Personal", "Fact", "Contact", "Custom Command", "Preference"
  val timestamp: Long = System.currentTimeMillis()
)
