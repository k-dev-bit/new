package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screened_calls")
data class ScreenedCallRecord(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val callerName: String,
  val callerPhone: String,
  val timestamp: Long = System.currentTimeMillis(),
  val unansweredDurationSec: Int = 15,
  val transcript: String,
  val summary: String,
  val callerMessage: String,
  val isUrgent: Boolean = false
)
