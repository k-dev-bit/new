package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.NovaAiEngine
import com.example.call.CallScreenState
import com.example.call.NovaCallManager
import com.example.controller.SystemActionController
import com.example.data.db.NovaDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.JarvisMemory
import com.example.data.model.ScreenedCallRecord
import com.example.data.settings.NovaPreferences
import com.example.service.NovaBackgroundService
import com.example.voice.NovaVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NovaTab(val title: String) {
  HUD("NOVA Core"),
  MEMORY("Memory Bank"),
  CALL_SCREEN("Call Screener"),
  AI_MODELS("AI Providers"),
  FILE_READER("Doc Reader")
}

typealias JarvisTab = NovaTab

class NovaViewModel(application: Application) : AndroidViewModel(application) {

  private val context = application.applicationContext
  val preferences = NovaPreferences(context)
  private val database = NovaDatabase.getDatabase(context)
  val jarvisDao = database.jarvisDao()

  val voiceManager = NovaVoiceManager(context, preferences)
  val aiEngine = NovaAiEngine(context, preferences, jarvisDao)
  val callManager = NovaCallManager(context, preferences, voiceManager, jarvisDao, viewModelScope)

  // Navigation & UI States
  private val _currentTab = MutableStateFlow(NovaTab.HUD)
  val currentTab: StateFlow<NovaTab> = _currentTab.asStateFlow()

  private val _inputText = MutableStateFlow("")
  val inputText: StateFlow<String> = _inputText.asStateFlow()

  private val _isFlashlightOn = MutableStateFlow(SystemActionController.isFlashlightOn())
  val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

  private val _isServiceActive = MutableStateFlow(NovaBackgroundService.isServiceRunning)
  val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

  private val _isWakeWordListening = MutableStateFlow(preferences.continuousMicEnabled)
  val isWakeWordListening: StateFlow<Boolean> = _isWakeWordListening.asStateFlow()

  private val _activeFileContent = MutableStateFlow<String?>(null)
  val activeFileContent: StateFlow<String?> = _activeFileContent.asStateFlow()

  private val _activeFileName = MutableStateFlow<String?>(null)
  val activeFileName: StateFlow<String?> = _activeFileName.asStateFlow()

  // Room DB Flows
  val chatHistory: StateFlow<List<ChatMessage>> = jarvisDao.getAllMessages()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val memoryList: StateFlow<List<JarvisMemory>> = jarvisDao.getAllMemories()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val screenedCalls: StateFlow<List<ScreenedCallRecord>> = jarvisDao.getAllScreenedCalls()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    // 1. Connect Voice Recognition callback to Command Pipeline
    voiceManager.onVoiceInputResult = { recognizedSpeech ->
      handleUserCommand(recognizedSpeech)
    }

    // 2. Connect Wake Word Trigger ('Utho Nova')
    voiceManager.onWakeWordActivated = { commandAfterWake ->
      if (!commandAfterWake.isNullOrBlank()) {
        handleUserCommand(commandAfterWake)
      } else {
        voiceManager.speak("Haan ${preferences.userName}, Nova hazir hai. Boliye!")
        // Start speech recognizer for the follow-up command
        voiceManager.startListening()
      }
    }

    // Start background wake word listener if enabled
    if (preferences.continuousMicEnabled) {
      voiceManager.toggleContinuousWakeWord(true)
    }

    // Insert initial Welcome message if chat history is empty
    viewModelScope.launch {
      jarvisDao.insertMessage(
        ChatMessage(
          sender = "nova",
          message = "Namaste Sir! N.O.V.A. systems online. Aap kabhi bhi 'Utho Nova' bol kar mujhe activate kar sakte hain, ya microphone tap karein.",
          actionType = "WAKE_WORD_READY",
          actionDetails = "Wake-word: 'Utho Nova' Active"
        )
      )
    }
  }

  fun setTab(tab: NovaTab) {
    _currentTab.value = tab
  }

  fun setInputText(text: String) {
    _inputText.value = text
  }

  fun toggleContinuousWakeWord() {
    val newState = !_isWakeWordListening.value
    _isWakeWordListening.value = newState
    preferences.continuousMicEnabled = newState
    voiceManager.toggleContinuousWakeWord(newState)

    if (newState) {
      // Ensure background service is also running for continuous background mic
      if (!_isServiceActive.value) {
        toggleBackgroundService()
      }
      voiceManager.speak("'Utho Nova' wake-word listener activated. Main background me sun rahi hoon.")
    } else {
      voiceManager.speak("Background listener deactivated.")
    }
  }

  fun handleUserCommand(command: String) {
    if (command.isBlank()) return
    _inputText.value = ""

    viewModelScope.launch {
      // 1. Add user message to DB
      jarvisDao.insertMessage(ChatMessage(sender = "user", message = command))

      // 2. Process via AI Engine
      val response = aiEngine.processCommand(command)

      // 3. Save Nova response to DB
      jarvisDao.insertMessage(
        ChatMessage(
          sender = "nova",
          message = response.replyText,
          actionType = response.actionType,
          actionDetails = response.actionDetails,
          actionStatus = response.executionResult?.let { if (it.success) "EXECUTED" else "FAILED" }
        )
      )

      // 4. Update flashlight state if changed
      _isFlashlightOn.value = SystemActionController.isFlashlightOn()

      // 5. Voice speak Nova response
      voiceManager.speak(response.replyText)
    }
  }

  fun startVoiceListening() {
    voiceManager.startListening()
  }

  fun stopVoiceListening() {
    voiceManager.stopListening()
  }

  fun stopSpeaking() {
    voiceManager.stopSpeaking()
  }

  fun toggleFlashlight() {
    val res = SystemActionController.setFlashlight(context)
    _isFlashlightOn.value = SystemActionController.isFlashlightOn()
    voiceManager.speak(res.speechResponse)
  }

  fun toggleBackgroundService() {
    if (_isServiceActive.value) {
      NovaBackgroundService.stopService(context)
      _isServiceActive.value = false
      voiceManager.speak("Nova background service suspended, Sir.")
    } else {
      NovaBackgroundService.startService(context)
      _isServiceActive.value = true
      voiceManager.speak("Nova background guardian online, Sir. Main background me har waqt active hoon.")
    }
  }

  // Memory operations
  fun addCustomMemory(key: String, content: String, category: String) {
    viewModelScope.launch {
      jarvisDao.insertMemory(
        JarvisMemory(
          keyTitle = key.ifBlank { "Custom Record" },
          content = content,
          category = category
        )
      )
      voiceManager.speak("Saved to Nova memory bank, Sir.")
    }
  }

  fun deleteMemory(id: Long) {
    viewModelScope.launch {
      jarvisDao.deleteMemory(id)
      voiceManager.speak("Record expunged from memory, Sir.")
    }
  }

  fun clearAllMemories() {
    viewModelScope.launch {
      jarvisDao.clearAllMemories()
      voiceManager.speak("Memory bank completely cleared, Sir.")
    }
  }

  fun clearChatHistory() {
    viewModelScope.launch {
      jarvisDao.clearChatHistory()
      voiceManager.speak("Terminal logs cleared, Sir.")
    }
  }

  fun deleteScreenedCall(id: Long) {
    viewModelScope.launch {
      jarvisDao.deleteScreenedCall(id)
    }
  }

  fun clearScreenedCalls() {
    viewModelScope.launch {
      jarvisDao.clearScreenedCalls()
    }
  }

  // File loading and reading
  fun loadFileFromUri(uri: Uri, displayName: String?) {
    viewModelScope.launch {
      val content = SystemActionController.readTextFromUri(context, uri)
      _activeFileContent.value = content
      _activeFileName.value = displayName ?: "Document"
      voiceManager.speak("Document '$displayName' loaded successfully, Sir. You can read it or have me speak it aloud.")
    }
  }

  fun readLoadedFileAloud() {
    val content = _activeFileContent.value ?: return
    voiceManager.speak("Reading document: $content")
  }

  fun clearActiveFile() {
    _activeFileContent.value = null
    _activeFileName.value = null
    voiceManager.stopSpeaking()
  }

  override fun onCleared() {
    super.onCleared()
    voiceManager.destroy()
  }
}

typealias JarvisViewModel = NovaViewModel
