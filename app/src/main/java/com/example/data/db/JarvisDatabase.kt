package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.JarvisMemory
import com.example.data.model.ScreenedCallRecord

@Database(
  entities = [ChatMessage::class, JarvisMemory::class, ScreenedCallRecord::class],
  version = 1,
  exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
  abstract fun jarvisDao(): JarvisDao

  companion object {
    @Volatile
    private var INSTANCE: JarvisDatabase? = null

    fun getDatabase(context: Context): JarvisDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          JarvisDatabase::class.java,
          "jarvis_core.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
