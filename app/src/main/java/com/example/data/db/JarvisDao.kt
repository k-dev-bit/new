package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ChatMessage
import com.example.data.model.JarvisMemory
import com.example.data.model.ScreenedCallRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
  // Chat History
  @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
  fun getAllMessages(): Flow<List<ChatMessage>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessage): Long

  @Query("DELETE FROM chat_messages")
  suspend fun clearChatHistory()

  @Query("DELETE FROM chat_messages WHERE id = :id")
  suspend fun deleteMessage(id: Long)

  // Long-term Memories
  @Query("SELECT * FROM jarvis_memories ORDER BY timestamp DESC")
  fun getAllMemories(): Flow<List<JarvisMemory>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMemory(memory: JarvisMemory): Long

  @Query("DELETE FROM jarvis_memories WHERE id = :id")
  suspend fun deleteMemory(id: Long)

  @Query("DELETE FROM jarvis_memories")
  suspend fun clearAllMemories()

  // Screened Calls
  @Query("SELECT * FROM screened_calls ORDER BY timestamp DESC")
  fun getAllScreenedCalls(): Flow<List<ScreenedCallRecord>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertScreenedCall(call: ScreenedCallRecord): Long

  @Query("DELETE FROM screened_calls WHERE id = :id")
  suspend fun deleteScreenedCall(id: Long)

  @Query("DELETE FROM screened_calls")
  suspend fun clearScreenedCalls()
}
