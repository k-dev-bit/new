package com.example.call

import android.content.Context
import com.example.data.db.JarvisDao
import com.example.data.model.ScreenedCallRecord
import com.example.data.settings.NovaPreferences
import com.example.voice.NovaVoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class CallScreenState {
  object Idle : CallScreenState()
  data class Ringing(
    val callerName: String,
    val callerPhone: String,
    val secondsLeft: Int
  ) : CallScreenState()
  data class JarvisScreening(
    val callerName: String,
    val callerPhone: String,
    val jarvisSpeech: String,
    val callerSpeech: String = "",
    val isAnalyzing: Boolean = false
  ) : CallScreenState()
  data class Finished(
    val record: ScreenedCallRecord
  ) : CallScreenState()
}

class NovaCallManager(
  private val context: Context,
  private val preferences: NovaPreferences,
  private val voiceManager: NovaVoiceManager,
  private val jarvisDao: JarvisDao,
  private val scope: CoroutineScope
) {

  private val _screenState = MutableStateFlow<CallScreenState>(CallScreenState.Idle)
  val screenState: StateFlow<CallScreenState> = _screenState.asStateFlow()

  private var countdownJob: kotlinx.coroutines.Job? = null

  /**
   * Start an incoming call simulation or real call screening process
   */
  fun triggerIncomingCall(callerName: String = "Dr. Rajesh Sharma", callerPhone: String = "+91 98765 43210") {
    countdownJob?.cancel()
    val timeoutSec = preferences.callScreeningTimeoutSec.coerceIn(5, 60)

    countdownJob = scope.launch(Dispatchers.Main) {
      for (remaining in timeoutSec downTo 1) {
        _screenState.value = CallScreenState.Ringing(callerName, callerPhone, remaining)
        delay(1000)
      }
      // If still ringing after 15s timeout, Nova takes over automatically!
      if (_screenState.value is CallScreenState.Ringing) {
        startNovaScreening(callerName, callerPhone)
      }
    }
  }

  /**
   * User manually delegates to Nova immediately
   */
  fun delegateToNova() {
    val current = _screenState.value
    if (current is CallScreenState.Ringing) {
      countdownJob?.cancel()
      startNovaScreening(current.callerName, current.callerPhone)
    }
  }

  fun dismissCall() {
    countdownJob?.cancel()
    voiceManager.stopSpeaking()
    _screenState.value = CallScreenState.Idle
  }

  private fun startNovaScreening(callerName: String, callerPhone: String) {
    val openingScript = preferences.callScreeningScript
    _screenState.value = CallScreenState.JarvisScreening(
      callerName = callerName,
      callerPhone = callerPhone,
      jarvisSpeech = openingScript
    )

    // Nova speaks the script aloud
    voiceManager.speak(openingScript)
  }

  /**
   * Process caller response during screening (voice input or test message)
   */
  fun submitCallerResponse(callerUtterance: String) {
    val current = _screenState.value as? CallScreenState.JarvisScreening ?: return

    scope.launch(Dispatchers.IO) {
      _screenState.value = current.copy(
        callerSpeech = callerUtterance,
        isAnalyzing = true
      )

      val lower = callerUtterance.lowercase()
      val keywords = preferences.urgentKeywords.split(",").map { it.trim().lowercase() }
      val isUrgent = keywords.any { it.isNotBlank() && lower.contains(it) }

      delay(1500)

      val finalNovaReply = if (isUrgent) {
        "URGENT PRIORITY: Emergency flagged. Connecting Mr. ${preferences.userName} immediately!"
      } else {
        preferences.callScreeningRejectionMsg
      }

      val summary = if (isUrgent) {
        "Urgent call regarding: $callerUtterance"
      } else {
        "Routine inquiry: $callerUtterance. Handled by Nova."
      }

      val record = ScreenedCallRecord(
        callerName = current.callerName,
        callerPhone = current.callerPhone,
        unansweredDurationSec = preferences.callScreeningTimeoutSec,
        transcript = "Nova: ${current.jarvisSpeech}\nCaller: $callerUtterance\nNova: $finalNovaReply",
        summary = summary,
        callerMessage = callerUtterance,
        isUrgent = isUrgent
      )

      jarvisDao.insertScreenedCall(record)

      withContext(Dispatchers.Main) {
        voiceManager.speak(finalNovaReply)
        _screenState.value = CallScreenState.Finished(record)
      }
    }
  }
}

typealias JarvisCallManager = NovaCallManager
