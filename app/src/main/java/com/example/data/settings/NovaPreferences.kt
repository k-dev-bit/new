package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences

class NovaPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("nova_system_config", Context.MODE_PRIVATE)

  companion object {
    const val KEY_AI_NAME = "ai_name"
    const val KEY_AI_PROVIDER = "ai_provider"
    const val KEY_SELECTED_MODEL = "selected_model"
    const val KEY_GEMINI_KEY = "gemini_api_key"
    const val KEY_OPENAI_KEY = "openai_api_key"
    const val KEY_GROQ_KEY = "groq_api_key"
    const val KEY_CUSTOM_URL = "custom_api_url"
    const val KEY_CUSTOM_KEY = "custom_api_key"

    const val KEY_WAKE_WORD_ENABLED = "wake_word_enabled"
    const val KEY_CONTINUOUS_MIC_ENABLED = "continuous_mic_enabled"
    const val KEY_BACKGROUND_VOICE_CHAT = "background_voice_chat_enabled"
    const val KEY_PERSISTENT_MIC = "persistent_mic_enabled"
    const val KEY_WAKE_PHRASE = "wake_phrase"

    const val KEY_SCREENING_ENABLED = "call_screening_enabled"
    const val KEY_SCREENING_TIMEOUT = "call_screening_timeout_sec"
    const val KEY_SCREENING_SCRIPT = "call_screening_script"
    const val KEY_SCREENING_REJECTION = "call_screening_rejection"
    const val KEY_URGENT_KEYWORDS = "call_screening_urgent_keywords"

    const val KEY_VOICE_ENABLED = "voice_tts_enabled"
    const val KEY_VOICE_PITCH = "voice_pitch"
    const val KEY_VOICE_SPEED = "voice_speed"
    const val KEY_USER_NAME = "user_name"
    const val KEY_CUSTOM_INSTRUCTIONS = "custom_instructions"
  }

  var aiName: String
    get() = prefs.getString(KEY_AI_NAME, "NOVA") ?: "NOVA"
    set(value) = prefs.edit().putString(KEY_AI_NAME, value).apply()

  var wakeWordEnabled: Boolean
    get() = prefs.getBoolean(KEY_WAKE_WORD_ENABLED, true)
    set(value) = prefs.edit().putBoolean(KEY_WAKE_WORD_ENABLED, value).apply()

  var continuousMicEnabled: Boolean
    get() = prefs.getBoolean(KEY_CONTINUOUS_MIC_ENABLED, true)
    set(value) = prefs.edit().putBoolean(KEY_CONTINUOUS_MIC_ENABLED, value).apply()

  var backgroundVoiceChatEnabled: Boolean
    get() = prefs.getBoolean(KEY_BACKGROUND_VOICE_CHAT, true)
    set(value) = prefs.edit().putBoolean(KEY_BACKGROUND_VOICE_CHAT, value).apply()

  var persistentMicEnabled: Boolean
    get() = prefs.getBoolean(KEY_PERSISTENT_MIC, true)
    set(value) = prefs.edit().putBoolean(KEY_PERSISTENT_MIC, value).apply()

  var wakePhrase: String
    get() = prefs.getString(KEY_WAKE_PHRASE, "utho nova") ?: "utho nova"
    set(value) = prefs.edit().putString(KEY_WAKE_PHRASE, value).apply()

  var aiProvider: String
    get() = prefs.getString(KEY_AI_PROVIDER, "LOCAL_NOVA") ?: "LOCAL_NOVA"
    set(value) = prefs.edit().putString(KEY_AI_PROVIDER, value).apply()

  var selectedModel: String
    get() = prefs.getString(KEY_SELECTED_MODEL, "nova-core-v2") ?: "nova-core-v2"
    set(value) = prefs.edit().putString(KEY_SELECTED_MODEL, value).apply()

  var geminiApiKey: String
    get() = prefs.getString(KEY_GEMINI_KEY, "") ?: ""
    set(value) = prefs.edit().putString(KEY_GEMINI_KEY, value).apply()

  var openAiApiKey: String
    get() = prefs.getString(KEY_OPENAI_KEY, "") ?: ""
    set(value) = prefs.edit().putString(KEY_OPENAI_KEY, value).apply()

  var groqApiKey: String
    get() = prefs.getString(KEY_GROQ_KEY, "") ?: ""
    set(value) = prefs.edit().putString(KEY_GROQ_KEY, value).apply()

  var customApiUrl: String
    get() = prefs.getString(KEY_CUSTOM_URL, "http://10.0.2.2:11434/api/generate") ?: "http://10.0.2.2:11434/api/generate"
    set(value) = prefs.edit().putString(KEY_CUSTOM_URL, value).apply()

  var customApiKey: String
    get() = prefs.getString(KEY_CUSTOM_KEY, "") ?: ""
    set(value) = prefs.edit().putString(KEY_CUSTOM_KEY, value).apply()

  var callScreeningEnabled: Boolean
    get() = prefs.getBoolean(KEY_SCREENING_ENABLED, true)
    set(value) = prefs.edit().putBoolean(KEY_SCREENING_ENABLED, value).apply()

  var callScreeningTimeoutSec: Int
    get() = prefs.getInt(KEY_SCREENING_TIMEOUT, 15)
    set(value) = prefs.edit().putInt(KEY_SCREENING_TIMEOUT, value).apply()

  var callScreeningScript: String
    get() = prefs.getString(
      KEY_SCREENING_SCRIPT,
      "Hello! This is NOVA, personal AI assistant to Mr. Bharat. He is unavailable to take your call at this moment. Please state your name and the purpose of your call."
    ) ?: "Hello! This is NOVA, personal AI assistant to Mr. Bharat. He is unavailable to take your call at this moment. Please state your name and the purpose of your call."
    set(value) = prefs.edit().putString(KEY_SCREENING_SCRIPT, value).apply()

  var callScreeningRejectionMsg: String
    get() = prefs.getString(
      KEY_SCREENING_REJECTION,
      "Thank you. Since this is not an urgent emergency, I have safely recorded your message and notified Bharat. Good day."
    ) ?: "Thank you. Since this is not an urgent emergency, I have safely recorded your message and notified Bharat. Good day."
    set(value) = prefs.edit().putString(KEY_SCREENING_REJECTION, value).apply()

  var urgentKeywords: String
    get() = prefs.getString(
      KEY_URGENT_KEYWORDS,
      "emergency, urgent, hospital, accident, doctor, immediately, zaroori, madad, critical"
    ) ?: "emergency, urgent, hospital, accident, doctor, immediately, zaroori, madad, critical"
    set(value) = prefs.edit().putString(KEY_URGENT_KEYWORDS, value).apply()

  var voiceTtsEnabled: Boolean
    get() = prefs.getBoolean(KEY_VOICE_ENABLED, true)
    set(value) = prefs.edit().putBoolean(KEY_VOICE_ENABLED, value).apply()

  var voicePitch: Float
    get() = prefs.getFloat(KEY_VOICE_PITCH, 1.05f) // Crisp, smart modern tone for Nova
    set(value) = prefs.edit().putFloat(KEY_VOICE_PITCH, value).apply()

  var voiceSpeed: Float
    get() = prefs.getFloat(KEY_VOICE_SPEED, 1.05f)
    set(value) = prefs.edit().putFloat(KEY_VOICE_SPEED, value).apply()

  var userName: String
    get() = prefs.getString(KEY_USER_NAME, "Bharat") ?: "Bharat"
    set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

  var customInstructions: String
    get() = prefs.getString(
      KEY_CUSTOM_INSTRUCTIONS,
      "Always address user respectfully as Sir or Mr. Bharat. Speak concisely, warmly, and with high intelligence, similar to a state-of-the-art AI assistant named NOVA."
    ) ?: "Always address user respectfully as Sir or Mr. Bharat. Speak concisely, warmly, and with high intelligence, similar to a state-of-the-art AI assistant named NOVA."
    set(value) = prefs.edit().putString(KEY_CUSTOM_INSTRUCTIONS, value).apply()
}

// Backward-compatible typealias so any existing reference works seamlessly
typealias JarvisPreferences = NovaPreferences
