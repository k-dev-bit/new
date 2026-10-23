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
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.settings.NovaPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class NovaVoiceManager(
  private val context: Context,
  private val preferences: NovaPreferences
) : TextToSpeech.OnInitListener {

  companion object {
    private const val TAG = "NovaVoiceManager"
  }

  private val mainHandler = Handler(Looper.getMainLooper())
  private var tts: TextToSpeech? = null
  private var speechRecognizer: SpeechRecognizer? = null
  private var isTtsInitialized = false

  // Indicates whether user desires active listening (will not self-terminate)
  private var isListeningDesired = false

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _speechAmplitude = MutableStateFlow(0.1f)
  val speechAmplitude: StateFlow<Float> = _speechAmplitude.asStateFlow()

  private val _recognizedText = MutableStateFlow("")
  val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

  private val _isWakeWordActive = MutableStateFlow(false)
  val isWakeWordActive: StateFlow<Boolean> = _isWakeWordActive.asStateFlow()

  private val _isContinuousChatActive = MutableStateFlow(false)
  val isContinuousChatActive: StateFlow<Boolean> = _isContinuousChatActive.asStateFlow()

  var onVoiceInputResult: ((String) -> Unit)? = null
  var onWakeWordActivated: ((commandAfterWake: String?) -> Unit)? = null

  // Lightweight on-device wake-word listener
  private var wakeWordListener: NovaWakeWordListener? = null

  init {
    initTts()
    initWakeWordListener()
  }

  private fun initTts() {
    try {
      tts = TextToSpeech(context.applicationContext, this)
    } catch (e: Exception) {
      Log.e(TAG, "TTS Init exception: ${e.message}")
    }
  }

  private fun initWakeWordListener() {
    wakeWordListener = NovaWakeWordListener(context, preferences) { commandAfterWake ->
      onWakeWordActivated?.invoke(commandAfterWake)
    }
  }

  fun toggleContinuousWakeWord(enable: Boolean) {
    _isWakeWordActive.value = enable
    preferences.continuousMicEnabled = enable
    preferences.wakeWordEnabled = enable
    if (enable) {
      wakeWordListener?.startWakeWordListening()
    } else {
      wakeWordListener?.stopWakeWordListening()
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      isTtsInitialized = true
      val ukLocale = Locale.UK
      val result = tts?.setLanguage(ukLocale)
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        tts?.language = Locale.getDefault()
      }
      applyVoiceSettings()

      tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
          _isSpeaking.value = true
          _speechAmplitude.value = 0.85f
          // Temporarily cancel active speech recognition so Nova does not transcribe its own voice
          mainHandler.post {
            try {
              speechRecognizer?.cancel()
            } catch (_: Exception) {}
          }
        }

        override fun onDone(utteranceId: String?) {
          _isSpeaking.value = false
          _speechAmplitude.value = 0.1f
          // Automatically resume listening if continuous mic / hands-free mode is on!
          mainHandler.postDelayed({
            if (isListeningDesired && !_isSpeaking.value) {
              startListeningInternal()
            }
          }, 350)
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
          _isSpeaking.value = false
          _speechAmplitude.value = 0.1f
          mainHandler.postDelayed({
            if (isListeningDesired && !_isSpeaking.value) {
              startListeningInternal()
            }
          }, 350)
        }
      })
    }
  }

  fun applyVoiceSettings() {
    tts?.setPitch(preferences.voicePitch)
    tts?.setSpeechRate(preferences.voiceSpeed)
  }

  fun speak(text: String, utteranceId: String = "nova_speech_${System.currentTimeMillis()}") {
    if (!preferences.voiceTtsEnabled) return
    if (text.isBlank()) return

    if (tts == null || !isTtsInitialized) {
      initTts()
    }
    applyVoiceSettings()
    tts?.stop()
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
  }

  fun stopSpeaking() {
    tts?.stop()
    _isSpeaking.value = false
    _speechAmplitude.value = 0.1f
    // If user explicitly stops speaking, resume listening if desired
    if (isListeningDesired) {
      mainHandler.postDelayed({
        if (isListeningDesired && !_isSpeaking.value) {
          startListeningInternal()
        }
      }, 300)
    }
  }

  /**
   * Starts persistent listening. When [continuous] is true, the microphone NEVER turns off
   * on its own due to timeouts or pauses in speech. It seamlessly auto-restarts until stopListening() is called.
   */
  fun startListening(continuous: Boolean = true) {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      _recognizedText.value = "Speech recognition unavailable on this device."
      return
    }

    isListeningDesired = true
    _isContinuousChatActive.value = continuous
    playBeepTone(ToneGenerator.TONE_PROP_BEEP)

    mainHandler.post {
      startListeningInternal()
    }
  }

  private fun startListeningInternal() {
    if (!isListeningDesired) return
    if (_isSpeaking.value) return // Wait for TTS to complete

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
          _speechAmplitude.value = 0.35f
        }

        override fun onBeginningOfSpeech() {
          _speechAmplitude.value = 0.75f
        }

        override fun onRmsChanged(rmsdB: Float) {
          val norm = ((rmsdB + 2f) / 12f).coerceIn(0.18f, 1.0f)
          _speechAmplitude.value = norm
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
          // Do NOT shut off the mic! Keep state active while processing
          _speechAmplitude.value = 0.2f
        }

        override fun onError(error: Int) {
          Log.d(TAG, "SpeechRecognizer error: $error (isListeningDesired=$isListeningDesired)")

          if (isListeningDesired) {
            // Keep the user-visible mic state active while gracefully auto-recovering
            _speechAmplitude.value = 0.15f

            if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY ||
                error == SpeechRecognizer.ERROR_CLIENT
            ) {
              try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
              } catch (_: Exception) {}
              speechRecognizer = null
            }

            // Immediately schedule restart so mic never shuts off on pauses / no-match
            mainHandler.postDelayed({
              if (isListeningDesired && !_isSpeaking.value) {
                startListeningInternal()
              }
            }, 300)
          } else {
            _isListening.value = false
            _speechAmplitude.value = 0.1f
          }
        }

        override fun onResults(results: Bundle?) {
          _speechAmplitude.value = 0.15f
          val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          val text = matches?.firstOrNull() ?: ""

          if (text.isNotBlank()) {
            _recognizedText.value = text
            playBeepTone(ToneGenerator.TONE_PROP_ACK)
            onVoiceInputResult?.invoke(text)
          }

          if (isListeningDesired) {
            // If text was recognized, Nova will typically trigger TTS.
            // If no TTS triggers within 600ms, restart listening to stay active.
            mainHandler.postDelayed({
              if (isListeningDesired && !_isSpeaking.value) {
                startListeningInternal()
              }
            }, 600)
          } else {
            _isListening.value = false
            _speechAmplitude.value = 0.1f
          }
        }

        override fun onPartialResults(partialResults: Bundle?) {
          val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          matches?.firstOrNull()?.let {
            _recognizedText.value = it
          }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
      })

      speechRecognizer?.startListening(intent)
      _isListening.value = true
    } catch (e: Exception) {
      Log.e(TAG, "Exception starting speechRecognizer: ${e.message}")
      if (isListeningDesired) {
        mainHandler.postDelayed({
          if (isListeningDesired && !_isSpeaking.value) {
            startListeningInternal()
          }
        }, 800)
      } else {
        _isListening.value = false
      }
    }
  }

  /**
   * Stops listening completely. Only explicitly called when user taps the mic to turn it off.
   */
  fun stopListening() {
    isListeningDesired = false
    _isContinuousChatActive.value = false
    _isListening.value = false
    _speechAmplitude.value = 0.1f

    mainHandler.post {
      try {
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
      } catch (_: Exception) {}
      speechRecognizer = null
    }
  }

  private fun playBeepTone(toneType: Int) {
    try {
      val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)
      tone.startTone(toneType, 150)
    } catch (_: Exception) {}
  }

  fun destroy() {
    isListeningDesired = false
    _isContinuousChatActive.value = false
    _isListening.value = false
    wakeWordListener?.stopWakeWordListening()
    wakeWordListener = null
    tts?.stop()
    tts?.shutdown()
    tts = null
    try {
      speechRecognizer?.destroy()
    } catch (_: Exception) {}
    speechRecognizer = null
  }
}

typealias JarvisVoiceManager = NovaVoiceManager
