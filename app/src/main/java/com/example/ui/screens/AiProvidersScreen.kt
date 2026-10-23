package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NovaViewModel
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.StarkAmber
import com.example.ui.theme.StarkGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TitaniumSurface

data class ProviderOption(
  val id: String,
  val name: String,
  val description: String,
  val defaultModels: List<String>
)

@Composable
fun AiProvidersScreen(viewModel: NovaViewModel) {
  val prefs = viewModel.preferences

  val providers = listOf(
    ProviderOption(
      id = "LOCAL_NOVA",
      name = "Built-in Nova Local Brain",
      description = "Works 100% offline & free. Instant phone controls & 'Utho Nova' listener.",
      defaultModels = listOf("nova-core-v2", "rule-engine-v2")
    ),
    ProviderOption(
      id = "GEMINI",
      name = "Google Gemini AI",
      description = "Advanced reasoning, multimodal understanding & lightning fast responses.",
      defaultModels = listOf("gemini-2.5-flash", "gemini-1.5-pro", "gemini-1.5-flash")
    ),
    ProviderOption(
      id = "OPENAI",
      name = "OpenAI",
      description = "GPT-4o & GPT-4o-mini general intelligence.",
      defaultModels = listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo")
    ),
    ProviderOption(
      id = "GROQ",
      name = "Groq High-Speed Llama",
      description = "Extremely fast open-source LPU inference.",
      defaultModels = listOf("openai/gpt-oss-120b", "openai/gpt-oss-20b")
    ),
    ProviderOption(
      id = "OLLAMA_LOCAL",
      name = "Local Ollama (Open Source)",
      description = "Self-hosted local AI models on your PC or home network.",
      defaultModels = listOf("llama3", "mistral", "deepseek-r1:8b", "qwen2.5-coder")
    ),
    ProviderOption(
      id = "CUSTOM_ENDPOINT",
      name = "Custom API Endpoint",
      description = "Any OpenAI-compatible server or proxy.",
      defaultModels = listOf("custom-model-v1")
    )
  )

  var selectedProviderId by remember { mutableStateOf(prefs.aiProvider) }
  var selectedModel by remember { mutableStateOf(prefs.selectedModel) }

  var geminiKey by remember { mutableStateOf(prefs.geminiApiKey) }
  var openAiKey by remember { mutableStateOf(prefs.openAiApiKey) }
  var groqKey by remember { mutableStateOf(prefs.groqApiKey) }
  var customUrl by remember { mutableStateOf(prefs.customApiUrl) }
  var customKey by remember { mutableStateOf(prefs.customApiKey) }

  var hideKeys by remember { mutableStateOf(true) }

  // Voice modulation
  var voicePitch by remember { mutableFloatStateOf(prefs.voicePitch) }
  var voiceSpeed by remember { mutableFloatStateOf(prefs.voiceSpeed) }
  var autoTtsEnabled by remember { mutableStateOf(prefs.voiceTtsEnabled) }
  var userName by remember { mutableStateOf(prefs.userName) }
  var customInstructions by remember { mutableStateOf(prefs.customInstructions) }

  var saveStatus by remember { mutableStateOf<String?>(null) }

  val activeProvider = providers.firstOrNull { it.id == selectedProviderId } ?: providers.first()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Header
    item {
      Column {
        Text(
          text = "AI PROVIDERS & MODEL HUB",
          color = CyanPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.2.sp
        )
        Text(
          text = "Switch intelligence engines, configure custom API keys, and tune voice cadence",
          color = TextSecondary,
          fontSize = 11.sp
        )
      }
    }

    // 2. Provider Selection
    item {
      CardBox {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "SELECT AI PROVIDER",
            color = CyanPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          providers.forEach { provider ->
            val isSelected = selectedProviderId == provider.id
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) Color(0xFF072435) else TitaniumSurface)
                .border(1.dp, if (isSelected) CyanPrimary else CyberBorder, RoundedCornerShape(8.dp))
                .clickable {
                  selectedProviderId = provider.id
                  selectedModel = provider.defaultModels.first()
                }
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = if (isSelected) CyanGlow else TextSecondary,
                modifier = Modifier.size(20.dp)
              )
              Column(
                modifier = Modifier
                  .weight(1f)
                  .padding(start = 10.dp)
              ) {
                Text(
                  text = provider.name,
                  color = if (isSelected) CyanPrimary else TextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = provider.description,
                  color = TextSecondary,
                  fontSize = 10.sp
                )
              }
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = CyanGlow,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }

    // 3. Model Switcher for Chosen Provider
    item {
      CardBox {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "AVAILABLE MODELS FOR ${activeProvider.name.uppercase()}",
            color = StarkGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(activeProvider.defaultModels) { model ->
              val isSelected = selectedModel == model
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) StarkGold else TitaniumSurface)
                  .border(1.dp, if (isSelected) StarkAmber else CyberBorder, RoundedCornerShape(8.dp))
                  .clickable { selectedModel = model }
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  text = model,
                  color = if (isSelected) Color(0xFF291700) else TextPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }

    // 4. API Keys & Credentials
    item {
      CardBox {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Key, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
              Text(
                text = " API KEY VAULT",
                color = CyanPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
            IconButton(onClick = { hideKeys = !hideKeys }, modifier = Modifier.size(28.dp)) {
              Icon(
                imageVector = if (hideKeys) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Toggle Visibility",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          when (selectedProviderId) {
            "GEMINI" -> {
              OutlinedTextField(
                value = geminiKey,
                onValueChange = { geminiKey = it },
                label = { Text("Google Gemini API Key") },
                modifier = Modifier.fillMaxWidth().testTag("gemini_key_input"),
                visualTransformation = if (hideKeys) PasswordVisualTransformation() else VisualTransformation.None,
                singleLine = true
              )
            }
            "OPENAI" -> {
              OutlinedTextField(
                value = openAiKey,
                onValueChange = { openAiKey = it },
                label = { Text("OpenAI API Key (sk-...)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (hideKeys) PasswordVisualTransformation() else VisualTransformation.None,
                singleLine = true
              )
            }
            "GROQ" -> {
              OutlinedTextField(
                value = groqKey,
                onValueChange = { groqKey = it },
                label = { Text("Groq API Key (gsk_...)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (hideKeys) PasswordVisualTransformation() else VisualTransformation.None,
                singleLine = true
              )
            }
            "OLLAMA_LOCAL" -> {
              OutlinedTextField(
                value = customUrl,
                onValueChange = { customUrl = it },
                label = { Text("Ollama Local Endpoint URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
              )
            }
            "CUSTOM_ENDPOINT" -> {
              OutlinedTextField(
                value = customUrl,
                onValueChange = { customUrl = it },
                label = { Text("Custom Server Base URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
              )
              OutlinedTextField(
                value = customKey,
                onValueChange = { customKey = it },
                label = { Text("API Bearer Token (if any)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (hideKeys) PasswordVisualTransformation() else VisualTransformation.None,
                singleLine = true
              )
            }
            else -> {
              Text(
                text = "No API key needed! Built-in Nova local intelligence handles all phone features directly.",
                color = SuccessGreen,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }

    // 5. Voice Cadence & User Persona
    item {
      CardBox {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = SkySecondary, modifier = Modifier.size(18.dp))
            Text(
              text = " VOICE CADENCE & PERSONA",
              color = SkySecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Voice Synthesis Response", color = TextPrimary, fontSize = 12.sp)
            Switch(
              checked = autoTtsEnabled,
              onCheckedChange = { autoTtsEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = CyanGlow, checkedTrackColor = CyanPrimary.copy(alpha = 0.5f))
            )
          }

          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Voice Pitch (Tony Stark / Robotic Tone)", color = TextSecondary, fontSize = 11.sp)
              Text(String.format("%.2f", voicePitch), color = CyanPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Slider(
              value = voicePitch,
              onValueChange = { voicePitch = it },
              valueRange = 0.5f..1.5f,
              colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanGlow)
            )
          }

          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Speech Rate (Speed)", color = TextSecondary, fontSize = 11.sp)
              Text(String.format("%.2f", voiceSpeed), color = CyanPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Slider(
              value = voiceSpeed,
              onValueChange = { voiceSpeed = it },
              valueRange = 0.7f..1.6f,
              colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanGlow)
            )
          }

          OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Your Name / Calling Title (e.g. Bharat)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          OutlinedTextField(
            value = customInstructions,
            onValueChange = { customInstructions = it },
            label = { Text("Custom Persona Directives for Nova") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // 6. Save Configuration Button
    item {
      Button(
        onClick = {
          prefs.aiProvider = selectedProviderId
          prefs.selectedModel = selectedModel
          prefs.geminiApiKey = geminiKey
          prefs.openAiApiKey = openAiKey
          prefs.groqApiKey = groqKey
          prefs.customApiUrl = customUrl
          prefs.customApiKey = customKey
          prefs.voicePitch = voicePitch
          prefs.voiceSpeed = voiceSpeed
          prefs.voiceTtsEnabled = autoTtsEnabled
          prefs.userName = userName
          prefs.customInstructions = customInstructions
          viewModel.voiceManager.applyVoiceSettings()

          saveStatus = "AI configuration & voice calibration saved!"
          viewModel.voiceManager.speak("Systems recalibrated with $selectedModel engine, Sir.")
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF001A24)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("save_ai_provider_button")
      ) {
        Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(text = " Apply AI & Voice Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp)
      }

      saveStatus?.let {
        Text(
          text = it,
          color = SuccessGreen,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }
  }
}
