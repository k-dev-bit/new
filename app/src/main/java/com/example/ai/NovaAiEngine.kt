package com.example.ai

import android.content.Context
import com.example.controller.ActionResult
import com.example.controller.SystemActionController
import com.example.data.db.JarvisDao
import com.example.data.model.JarvisMemory
import com.example.data.settings.NovaPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class NovaAiResponse(
  val replyText: String,
  val actionType: String? = null,
  val actionDetails: String? = null,
  val executionResult: ActionResult? = null
)

typealias AiResponse = NovaAiResponse

class NovaAiEngine(
  private val context: Context,
  private val preferences: NovaPreferences,
  private val jarvisDao: JarvisDao
) {

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  /**
   * Process user input (from voice or text) and execute actions or return AI answer
   */
  suspend fun processCommand(input: String): NovaAiResponse = withContext(Dispatchers.IO) {
    var trimmed = input.trim()

    // 0. Strip Wake Word if included at the start: "utho nova", "wake up nova", "hey nova", "nova"
    trimmed = trimmed.replace("(?i)^(utho nova|uth nova|wake up nova|hey nova|hello nova|nova suno|sun nova|nova)\\s*[,:;\\-]?\\s*".toRegex(), "").trim()
    if (trimmed.isBlank()) {
      return@withContext NovaAiResponse(
        replyText = "Haan ${preferences.userName}, Nova hazir hai! Boliye kya hukum hai?",
        actionType = "WAKE_WORD_ACK",
        actionDetails = "Nova is listening"
      )
    }

    val lower = trimmed.lowercase()

    // 1. Check for Direct Phone System Actions (Fast, Offline & Guaranteed)
    val actionCheck = detectAndExecuteAction(trimmed, lower)
    if (actionCheck != null) {
      return@withContext actionCheck
    }

    // 2. Check for Memory Commands: "yaad rakho ...", "remember that ..."
    if (lower.startsWith("yaad rakho") || lower.startsWith("remember that") || lower.startsWith("remember")) {
      val memoryContent = trimmed
        .replace("(?i)^(yaad rakho ki|yaad rakho|remember that|remember)".toRegex(), "")
        .trim()
      if (memoryContent.isNotBlank()) {
        val memory = JarvisMemory(
          keyTitle = memoryContent.take(25),
          content = memoryContent,
          category = "User Saved Fact"
        )
        jarvisDao.insertMemory(memory)
        return@withContext NovaAiResponse(
          replyText = "Nova has safely stored this in memory, Sir: \"$memoryContent\".",
          actionType = "MEMORY",
          actionDetails = memoryContent
        )
      }
    }

    // 3. Process with External AI Provider if configured, else use Local Brain
    when (preferences.aiProvider) {
      "GEMINI" -> {
        val key = preferences.geminiApiKey.ifBlank { "" }
        if (key.isNotBlank()) {
          val onlineReply = callGeminiApi(key, preferences.selectedModel, trimmed)
          if (onlineReply != null) return@withContext NovaAiResponse(replyText = onlineReply)
        }
      }
      "OPENAI" -> {
        val key = preferences.openAiApiKey.ifBlank { "" }
        if (key.isNotBlank()) {
          val onlineReply = callOpenAiCompatible(
            url = "https://api.openai.com/v1/chat/completions",
            apiKey = key,
            model = preferences.selectedModel.ifBlank { "gpt-4o" },
            prompt = trimmed
          )
          if (onlineReply != null) return@withContext NovaAiResponse(replyText = onlineReply)
        }
      }
      "GROQ" -> {
        val key = preferences.groqApiKey.ifBlank { "" }
        if (key.isNotBlank()) {
          val onlineReply = callOpenAiCompatible(
            url = "https://api.groq.com/openai/v1/chat/completions",
            apiKey = key,
            model = preferences.selectedModel.ifBlank { "openai/gpt-oss-120b" },
            prompt = trimmed
          )
          if (onlineReply != null) return@withContext NovaAiResponse(replyText = onlineReply)
        }
      }
      "OLLAMA_LOCAL" -> {
        val localReply = callOllamaLocal(preferences.customApiUrl, preferences.selectedModel, trimmed)
        if (localReply != null) return@withContext NovaAiResponse(replyText = localReply)
      }
      "CUSTOM_ENDPOINT" -> {
        val customReply = callOpenAiCompatible(
          url = preferences.customApiUrl,
          apiKey = preferences.customApiKey,
          model = preferences.selectedModel,
          prompt = trimmed
        )
        if (customReply != null) return@withContext NovaAiResponse(replyText = customReply)
      }
    }

    // Default Fallback: Nova Core Local Intelligence (Offline Brain)
    val localResponse = generateLocalNovaReply(trimmed, lower)
    NovaAiResponse(replyText = localResponse)
  }

  /**
   * System Command Action Matcher
   */
  private fun detectAndExecuteAction(raw: String, lower: String): NovaAiResponse? {
    // A. YouTube Control
    if (lower.contains("youtube") && (lower.contains("video") || lower.contains("chalao") || lower.contains("play") || lower.contains("search") || lower.contains("kholo") || lower.contains("on kar") || lower.contains("lagao"))) {
      var query = raw
        .replace("(?i)(youtube per|youtube par|youtube pe|on youtube|youtube|video|chalao|play|search karo|kholo|on kar de|kare|mere liye|lagao)".toRegex(), "")
        .trim()
      if (query.isBlank()) query = "Trending Music"
      val result = SystemActionController.openYouTube(context, query)
      return NovaAiResponse(
        replyText = result.speechResponse.replace("JARVIS", "NOVA"),
        actionType = "YOUTUBE",
        actionDetails = query,
        executionResult = result
      )
    }

    // B. Phone Call
    if (lower.startsWith("call") || lower.contains("phone lagao") || lower.contains("call karo") || lower.contains("dial karo") || lower.contains("ko call") || lower.contains("kisi ko phone")) {
      val target = raw
        .replace("(?i)(call karo|phone lagao|dial karo|call|phone|kisi ko|lagao|ko call lagao|ko|kare|please)".toRegex(), "")
        .trim()
      if (target.isNotBlank()) {
        val result = SystemActionController.makeCall(context, target)
        return NovaAiResponse(
          replyText = result.speechResponse,
          actionType = "CALL",
          actionDetails = target,
          executionResult = result
        )
      }
    }

    // C. Flashlight
    if (lower.contains("flashlight") || lower.contains("torch") || lower.contains("batti") || lower.contains("light on") || lower.contains("light off")) {
      val enable = when {
        lower.contains("on") || lower.contains("jalao") || lower.contains("chalu") -> true
        lower.contains("off") || lower.contains("band") || lower.contains("bujhao") -> false
        else -> null
      }
      val result = SystemActionController.setFlashlight(context, enable)
      return NovaAiResponse(
        replyText = result.speechResponse,
        actionType = "FLASHLIGHT",
        actionDetails = if (SystemActionController.isFlashlightOn()) "ON" else "OFF",
        executionResult = result
      )
    }

    // D. Camera
    if (lower.contains("camera kholo") || lower.contains("open camera") || lower.contains("photo khinchna") || lower.contains("camera chalu")) {
      val result = SystemActionController.openCamera(context)
      return NovaAiResponse(
        replyText = result.speechResponse,
        actionType = "CAMERA",
        actionDetails = "Camera Launched",
        executionResult = result
      )
    }

    // E. Google Sheet
    if (lower.contains("google sheet") || lower.contains("google seat") || lower.contains("spreadsheet") || lower.contains("sheet bana")) {
      val title = raw.replace("(?i)(google sheet banaa de|google sheet banao|google seat|sheet banao|spreadsheet|create)".toRegex(), "").trim()
      val result = SystemActionController.createGoogleSheet(context, title.ifBlank { "Nova Sheet" })
      return NovaAiResponse(
        replyText = result.speechResponse,
        actionType = "SHEET",
        actionDetails = "Google Sheet Created",
        executionResult = result
      )
    }

    // F. Calendar Event
    if (lower.contains("calender") || lower.contains("calendar") || lower.contains("meeting schedule")) {
      val eventName = raw.replace("(?i)(calender manage karen|calendar me add karo|calendar event|calendar|meeting schedule)".toRegex(), "").trim()
      val result = SystemActionController.addCalendarEvent(
        context,
        title = if (eventName.isBlank()) "Nova Strategic Meeting" else eventName
      )
      return NovaAiResponse(
        replyText = result.speechResponse,
        actionType = "CALENDAR",
        actionDetails = eventName,
        executionResult = result
      )
    }

    // G. Alarm
    if (lower.contains("alarm")) {
      val matcher = Pattern.compile("(\\d{1,2})(:|\\s*)?(\\d{2})?").matcher(lower)
      var hour = 7
      var min = 0
      if (matcher.find()) {
        hour = matcher.group(1)?.toIntOrNull() ?: 7
        min = matcher.group(3)?.toIntOrNull() ?: 0
        if (lower.contains("pm") || lower.contains("sham") || lower.contains("raat")) {
          if (hour < 12) hour += 12
        }
      }
      val result = SystemActionController.setAlarm(context, hour, min, "Nova Alarm")
      return NovaAiResponse(
        replyText = result.speechResponse,
        actionType = "ALARM",
        actionDetails = "$hour:$min",
        executionResult = result
      )
    }

    // H. Phone Lock
    if (lower.contains("phone lock") || lower.contains("lock phone") || lower.contains("screen lock") || lower.contains("device lock")) {
      val result = SystemActionController.triggerDeviceLock(context)
      return NovaAiResponse(
        replyText = result.speechResponse,
        actionType = "LOCK",
        actionDetails = "Device Security Lock",
        executionResult = result
      )
    }

    // I. System Settings
    if (lower.contains("setting kholo") || lower.contains("open settings") || lower.contains("settings")) {
      val result = SystemActionController.openSettings(context)
      return NovaAiResponse(
        replyText = result.speechResponse,
        actionType = "SETTINGS",
        actionDetails = "Settings Opened",
        executionResult = result
      )
    }

    // J. Web Search
    if (lower.startsWith("search karo") || lower.startsWith("google search") || lower.startsWith("search")) {
      val query = raw.replace("(?i)(search karo|google search|search)".toRegex(), "").trim()
      if (query.isNotBlank()) {
        val result = SystemActionController.openWebSearch(context, query)
        return NovaAiResponse(
          replyText = result.speechResponse,
          actionType = "WEB_SEARCH",
          actionDetails = query,
          executionResult = result
        )
      }
    }

    return null
  }

  /**
   * Local Nova Brain responses (Contextual, Intelligent & Fast)
   */
  private fun generateLocalNovaReply(prompt: String, lower: String): String {
    val user = preferences.userName
    return when {
      lower.contains("hello") || lower.contains("namaste") || lower.contains("kaise ho") || lower.contains("nova") -> {
        "Namaste $user! Main NOVA hoon, aapki AI personal assistant. Sabhi phone controls, YouTube, calling, flashlight, aur wake-word detection online hain. Boliye, main aapki kya madad karoon?"
      }
      lower.contains("who are you") || lower.contains("tum kaun ho") || lower.contains("kya kar sakte ho") -> {
        "Main N.O.V.A. hoon—aapka autonomous personal AI assistant. Main aapka phone poori tarah control kar sakti hoon: background me 'Utho Nova' sun kar trigger hona, YouTube par video chalana, contacts ko call lagana, flashlight on/off karna, Google Sheets banana, alarm lagana, documents padh kar sunana, aur calls screen karna."
      }
      lower.contains("status") || lower.contains("diagnostics") || lower.contains("battery") -> {
        "NOVA System Status: Core engine optimal. Background 'Utho Nova' listener active. Voice synthesizer online. All protocols green, Sir."
      }
      lower.contains("code") || lower.contains("coding") || lower.contains("python") || lower.contains("kotlin") -> {
        "Main aapke liye software architecture, Kotlin, Python, aur algorithms solve kar sakti hoon, Sir. Boliye kaunsa code likhna hai?"
      }
      else -> {
        "Aapka aadesh note kar liya gaya hai, $user: \"$prompt\". Mera local neural processor active hai. Kya ise ek system task ke roop me chalana hai ya online LLM ko bhejna hai?"
      }
    }
  }

  /**
   * Gemini API client
   */
  private fun callGeminiApi(apiKey: String, model: String, prompt: String): String? {
    return try {
      val actualModel = if (model.contains("gemini")) model else "gemini-2.5-flash"
      val url = "https://generativelanguage.googleapis.com/v1beta/models/$actualModel:generateContent?key=$apiKey"

      val bodyJson = JSONObject().apply {
        put("contents", JSONArray().apply {
          put(JSONObject().apply {
            put("parts", JSONArray().apply {
              put(JSONObject().apply {
                put("text", "You are N.O.V.A., a state-of-the-art AI personal assistant to ${preferences.userName}. Instructions: ${preferences.customInstructions}. Reply concisely and intelligently in Hindi/English as requested.\n\nUser: $prompt")
              })
            })
          })
        })
      }

      val request = Request.Builder()
        .url(url)
        .post(bodyJson.toString().toRequestBody(jsonMediaType))
        .build()

      val response = httpClient.newCall(request).execute()
      val resString = response.body?.string() ?: return null
      val json = JSONObject(resString)
      val candidates = json.optJSONArray("candidates")
      val content = candidates?.optJSONObject(0)?.optJSONObject("content")
      val parts = content?.optJSONArray("parts")
      parts?.optJSONObject(0)?.optString("text")
    } catch (_: Exception) {
      null
    }
  }

  /**
   * OpenAI / Groq Compatible Client
   */
  private fun callOpenAiCompatible(url: String, apiKey: String, model: String, prompt: String): String? {
    return try {
      val bodyJson = JSONObject().apply {
        put("model", model)
        put("messages", JSONArray().apply {
          put(JSONObject().apply {
            put("role", "system")
            put("content", "You are NOVA, personal AI assistant to ${preferences.userName}. ${preferences.customInstructions}")
          })
          put(JSONObject().apply {
            put("role", "user")
            put("content", prompt)
          })
        })
      }

      val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $apiKey")
        .post(bodyJson.toString().toRequestBody(jsonMediaType))
        .build()

      val response = httpClient.newCall(request).execute()
      val resString = response.body?.string() ?: return null
      val json = JSONObject(resString)
      val choices = json.optJSONArray("choices")
      val msg = choices?.optJSONObject(0)?.optJSONObject("message")
      msg?.optString("content")
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Local Ollama Client
   */
  private fun callOllamaLocal(endpointUrl: String, model: String, prompt: String): String? {
    return try {
      val bodyJson = JSONObject().apply {
        put("model", if (model.isNotBlank()) model else "llama3")
        put("prompt", "User: $prompt\nNova:")
        put("stream", false)
      }

      val request = Request.Builder()
        .url(endpointUrl)
        .post(bodyJson.toString().toRequestBody(jsonMediaType))
        .build()

      val response = httpClient.newCall(request).execute()
      val resString = response.body?.string() ?: return null
      val json = JSONObject(resString)
      json.optString("response").ifBlank { null }
    } catch (_: Exception) {
      null
    }
  }
}

typealias JarvisAiEngine = NovaAiEngine
