package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.ai.NovaAiEngine
import com.example.controller.SystemActionController
import com.example.data.db.NovaDatabase
import com.example.data.settings.NovaPreferences
import com.example.voice.NovaVoiceManager
import com.example.voice.NovaWakeWordListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NovaBackgroundService : Service() {

  companion object {
    const val CHANNEL_ID = "nova_assistant_channel"
    const val NOTIFICATION_ID = 1001

    const val ACTION_START = "com.example.service.START_NOVA"
    const val ACTION_STOP = "com.example.service.STOP_NOVA"
    const val ACTION_TRIGGER_VOICE = "com.example.service.TRIGGER_VOICE"

    var isServiceRunning = false
      private set

    fun startService(context: Context) {
      val intent = Intent(context, NovaBackgroundService::class.java).apply {
        action = ACTION_START
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }

    fun stopService(context: Context) {
      val intent = Intent(context, NovaBackgroundService::class.java).apply {
        action = ACTION_STOP
      }
      context.stopService(intent)
      isServiceRunning = false
    }
  }

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private lateinit var preferences: NovaPreferences
  private lateinit var voiceManager: NovaVoiceManager
  private var wakeWordListener: NovaWakeWordListener? = null
  private lateinit var aiEngine: NovaAiEngine

  override fun onCreate() {
    super.onCreate()
    preferences = NovaPreferences(this)
    voiceManager = NovaVoiceManager(this, preferences)
    val dao = NovaDatabase.getDatabase(this).jarvisDao()
    aiEngine = NovaAiEngine(this, preferences, dao)

    createNotificationChannel()

    // Setup background continuous wake-word listener
    wakeWordListener = NovaWakeWordListener(this, preferences) { commandAfterWake ->
      handleBackgroundWakeWord(commandAfterWake)
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action == ACTION_STOP) {
      isServiceRunning = false
      wakeWordListener?.stopWakeWordListening()
      stopForeground(STOP_FOREGROUND_REMOVE)
      stopSelf()
      return START_NOT_STICKY
    }

    if (intent?.action == ACTION_TRIGGER_VOICE) {
      val openApp = Intent(this, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        putExtra("TRIGGER_VOICE", true)
      }
      startActivity(openApp)
      return START_STICKY
    }

    isServiceRunning = true
    val notification = buildNotification()
    startForeground(NOTIFICATION_ID, notification)

    // Start background wake-word listening loop
    if (preferences.wakeWordEnabled) {
      wakeWordListener?.startWakeWordListening()
    }

    return START_STICKY
  }

  private fun handleBackgroundWakeWord(command: String?) {
    serviceScope.launch {
      if (command.isNullOrBlank()) {
        voiceManager.speak("Haan Bharat, Nova taiyar hai. Boliye!")
      } else {
        // Direct execution of spoken command
        voiceManager.speak("Nova executing: $command")
        aiEngine.processCommand(command)
      }
    }
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "NOVA AI Background Listener",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Background 'Utho Nova' wake-word and system control listener"
      }
      val manager = getSystemService(NotificationManager::class.java)
      manager.createNotificationChannel(channel)
    }
  }

  private fun buildNotification(): Notification {
    val openAppIntent = Intent(this, MainActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val pendingApp = PendingIntent.getActivity(
      this,
      0,
      openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = Intent(this, NovaBackgroundService::class.java).apply {
      action = ACTION_STOP
    }
    val pendingStop = PendingIntent.getService(
      this,
      1,
      stopIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("N.O.V.A. AI Systems Online")
      .setContentText("Listening for 'Utho Nova' • Continuous background mic active")
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentIntent(pendingApp)
      .addAction(R.drawable.ic_launcher_foreground, "Stop", pendingStop)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()
  }

  override fun onDestroy() {
    super.onDestroy()
    isServiceRunning = false
    wakeWordListener?.stopWakeWordListening()
    wakeWordListener = null
    voiceManager.destroy()
    serviceScope.cancel()
  }

  override fun onBind(intent: Intent?): IBinder? = null
}

typealias JarvisBackgroundService = NovaBackgroundService
