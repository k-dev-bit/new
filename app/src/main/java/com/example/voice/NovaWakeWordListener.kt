package com.example.voice

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.data.settings.NovaPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class NovaWakeWordListener(
  private val context: Context,
  private val preferences: NovaPreferences,
  private val onWakeWordDetected: (commandAfterWake: String?) -> Unit
) {

  private var speechRecognizer: SpeechRecognizer? = null
  private val mainHandler = Handler(Looper.getMainLooper())

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  private val _lastDetectedPhrase = MutableStateFlow("")
  val lastDetectedPhrase: StateFlow<String> = _lastDetectedPhrase.asStateFlow()

  private var isContinuousRunning = false
  private var isProcessingWake = false

  companion object {
    private const val TAG = "NovaWakeWord"
    val WAKE_PHRASES = listOf(
      "utho nova",
      "uth nova",
      "wake up nova",
      "hey nova",
      "hello nova",
      "nova suno",
      "nova",
      "उठो नोवा",
      "सुनो नोवा"
    )
  }

  fun startWakeWordListening() {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      Log.e(TAG, "SpeechRecognizer is unavailable on this device.")
      return
    }

    isContinuousRunning = true
    mainHandler.post {
      initAndListen()
    }
  }

  fun stopWakeWordListening() {
    isContinuousRunning = false
    mainHandler.post {
      try {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
      } catch (_: Exception) {}
      speechRecognizer = null
      _isListening.value = false
    }
  }

  private fun initAndListen() {
    if (!isContinuousRunning) return

    try {
      if (speechRecognizer == null) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
      }

      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
        putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-US", "en-IN"))
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
      }

      speechRecognizer?.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
          _isListening.value = true
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
          _isListening.value = false
          // Auto-restart loop for background continuous listening unless stopped
          if (isContinuousRunning && !isProcessingWake) {
            mainHandler.postDelayed({
              if (isContinuousRunning) {
                initAndListen()
              }
            }, 600)
          }
        }

        override fun onResults(results: Bundle?) {
          _isListening.value = false
          val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          matches?.forEach { phrase ->
            if (checkAndHandlePhrase(phrase)) {
              return
            }
          }

          // Restart if no wake word matched
          if (isContinuousRunning && !isProcessingWake) {
            mainHandler.postDelayed({
              if (isContinuousRunning) initAndListen()
            }, 400)
          }
        }

        override fun onPartialResults(partialResults: Bundle?) {
          val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          matches?.forEach { phrase ->
            if (checkAndHandlePhrase(phrase)) {
              try {
                speechRecognizer?.stopListening()
              } catch (_: Exception) {}
              return
            }
          }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
      })

      speechRecognizer?.startListening(intent)
    } catch (e: Exception) {
      Log.e(TAG, "Error starting speech recognizer: ${e.message}")
      if (isContinuousRunning) {
        mainHandler.postDelayed({ if (isContinuousRunning) initAndListen() }, 1500)
      }
    }
  }

  /**
   * Checks if phrase contains 'utho nova' or related wake words
   */
  private fun checkAndHandlePhrase(phrase: String): Boolean {
    val clean = phrase.lowercase().trim()
    _lastDetectedPhrase.value = clean

    for (wake in WAKE_PHRASES) {
      if (clean.contains(wake)) {
        isProcessingWake = true
        playWakeChime()

        // Extract remaining command if user spoke command together
        val command = clean.substringAfter(wake).trim()
        val finalCommand = if (command.isBlank()) null else command

        onWakeWordDetected(finalCommand)

        // Resume background listener after brief delay
        mainHandler.postDelayed({
          isProcessingWake = false
          if (isContinuousRunning) {
            initAndListen()
          }
        }, 3000)
        return true
      }
    }
    return false
  }

  private fun playWakeChime() {
    try {
      val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
      tone.startTone(ToneGenerator.TONE_PROP_ACK, 250)
    } catch (_: Exception) {}
  }
}
