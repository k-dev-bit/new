package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.NovaTab
import com.example.ui.NovaViewModel
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.SystemTelemetryBar
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.StarkAmber
import com.example.ui.theme.StarkGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TitaniumSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun NovaHudScreen(
  viewModel: NovaViewModel,
  onOpenDocPicker: () -> Unit
) {
  val chatHistory by viewModel.chatHistory.collectAsState()
  val memoryList by viewModel.memoryList.collectAsState()
  val inputText by viewModel.inputText.collectAsState()
  val isListening by viewModel.voiceManager.isListening.collectAsState()
  val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
  val speechAmplitude by viewModel.voiceManager.speechAmplitude.collectAsState()
  val recognizedText by viewModel.voiceManager.recognizedText.collectAsState()
  val isTorchOn by viewModel.isFlashlightOn.collectAsState()
  val isServiceActive by viewModel.isServiceActive.collectAsState()
  val isWakeWordActive by viewModel.isWakeWordListening.collectAsState()
  val isImeVisible = WindowInsets.isImeVisible

  val listState = rememberLazyListState()

  LaunchedEffect(chatHistory.size, isImeVisible) {
    if (chatHistory.isNotEmpty()) {
      listState.animateScrollToItem(chatHistory.size - 1)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .imePadding()
  ) {
    // 1. Top Telemetry Status Bar with Utho Nova toggle
    SystemTelemetryBar(
      activeModelName = viewModel.preferences.aiProvider.replace("_", " "),
      isTorchOn = isTorchOn,
      isServiceActive = isServiceActive,
      isWakeWordActive = isWakeWordActive,
      memoryCount = memoryList.size,
      onTorchToggle = { viewModel.toggleFlashlight() },
      onServiceToggle = { viewModel.toggleBackgroundService() },
      onWakeWordToggle = { viewModel.toggleContinuousWakeWord() }
    )

    Spacer(modifier = Modifier.height(if (isImeVisible) 4.dp else 8.dp))

    // 2. Arc Reactor Core & Waveform Section (compact when typing)
    val reactorSize = if (isImeVisible) 56.dp else 120.dp
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(
          Brush.verticalGradient(
            colors = listOf(
              CyberSurfaceVariant.copy(alpha = 0.7f),
              TitaniumSurface.copy(alpha = 0.9f)
            )
          )
        )
        .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
        .padding(vertical = if (isImeVisible) 4.dp else 10.dp, horizontal = 12.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        ArcReactorVisualizer(
          size = reactorSize,
          isListening = isListening,
          isSpeaking = isSpeaking,
          amplitude = speechAmplitude,
          onClick = {
            if (isSpeaking) {
              viewModel.stopSpeaking()
            } else if (isListening) {
              viewModel.stopVoiceListening()
            } else {
              viewModel.startVoiceListening()
            }
          }
        )

        if (!isImeVisible) {
          Spacer(modifier = Modifier.height(6.dp))

          // Dynamic Waveform
          VoiceWaveformVisualizer(
            isActive = isListening || isSpeaking || isWakeWordActive,
            amplitude = speechAmplitude,
            barCount = 20,
            maxHeight = 20.dp,
            color = if (isListening) CyanGlow else if (isSpeaking) StarkGold else SkySecondary
          )

          // Wake word status banner
          Row(
            modifier = Modifier
              .padding(top = 4.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(if (isWakeWordActive) Color(0xFF062232) else Color(0xFF131B24))
              .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.GraphicEq,
              contentDescription = null,
              tint = if (isWakeWordActive) CyanGlow else TextSecondary,
              modifier = Modifier.size(12.dp)
            )
            Text(
              text = if (isWakeWordActive) " WAKE-WORD ACTIVE: Say 'Utho Nova'" else " Wake-word paused. Tap to enable.",
              color = if (isWakeWordActive) CyanGlow else TextSecondary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          // Subtitle voice status
          val statusSubtitle = when {
            isListening -> "Listening to command..."
            isSpeaking -> "NOVA responding..."
            else -> "Tap Arc Reactor or say 'Utho Nova' anytime"
          }
          Text(
            text = statusSubtitle,
            color = if (isListening) CyanGlow else if (isSpeaking) StarkGold else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 3.dp)
          )
        }

        if (isListening && recognizedText.isNotBlank()) {
          Text(
            text = "\"$recognizedText\"",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }

    if (!isImeVisible) {
      Spacer(modifier = Modifier.height(8.dp))

      // 3. Quick Action Chips (Direct Phone Action Triggers)
      Text(
        text = "SYSTEM CONTROLS & VOICE DIRECTIVES",
        color = TextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
      )

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
      ) {
      item {
        QuickActionChip(
          icon = Icons.Default.PlayArrow,
          label = "YouTube",
          accentColor = DangerRed,
          onClick = { viewModel.handleUserCommand("YouTube par video chalao") }
        )
      }
      item {
        QuickActionChip(
          icon = Icons.Default.Call,
          label = "Call Contact",
          accentColor = SuccessGreen,
          onClick = { viewModel.handleUserCommand("Call Papa") }
        )
      }
      item {
        QuickActionChip(
          icon = Icons.Default.FlashlightOn,
          label = if (isTorchOn) "Torch OFF" else "Torch ON",
          accentColor = StarkAmber,
          onClick = { viewModel.toggleFlashlight() }
        )
      }
      item {
        QuickActionChip(
          icon = Icons.Default.CameraAlt,
          label = "Camera",
          accentColor = CyanPrimary,
          onClick = { viewModel.handleUserCommand("Camera kholo") }
        )
      }
      item {
        QuickActionChip(
          icon = Icons.Default.TableChart,
          label = "Google Sheet",
          accentColor = SuccessGreen,
          onClick = { viewModel.handleUserCommand("Google sheet banao") }
        )
      }
      item {
        QuickActionChip(
          icon = Icons.Default.CalendarMonth,
          label = "Calendar",
          accentColor = SkySecondary,
          onClick = { viewModel.handleUserCommand("Calendar me event add karo") }
        )
      }
      item {
        QuickActionChip(
          icon = Icons.Default.Alarm,
          label = "Set Alarm",
          accentColor = StarkGold,
          onClick = { viewModel.handleUserCommand("Alarm lagao 7:00 baje") }
        )
      }
      item {
        QuickActionChip(
          icon = Icons.Default.Description,
          label = "Read Doc",
          accentColor = CyanPrimary,
          onClick = {
            viewModel.setTab(NovaTab.FILE_READER)
            onOpenDocPicker()
          }
        )
      }
      item {
        QuickActionChip(
          icon = Icons.Default.Lock,
          label = "Phone Lock",
          accentColor = DangerRed,
          onClick = { viewModel.handleUserCommand("Phone lock karo") }
        )
      }
      item {
        QuickActionChip(
          icon = Icons.Default.Settings,
          label = "Settings",
          accentColor = SkySecondary,
          onClick = { viewModel.handleUserCommand("Settings kholo") }
        )
      }
      item {
        QuickActionChip(
          icon = Icons.Default.PhoneInTalk,
          label = "Screening Test",
          accentColor = CyanGlow,
          onClick = {
            viewModel.setTab(NovaTab.CALL_SCREEN)
            viewModel.callManager.triggerIncomingCall("Rajesh Sharma - Bank Loan", "+91 98765 00000")
          }
        )
      }
    }
  }

  Spacer(modifier = Modifier.height(8.dp))

    // 4. Conversation Terminal History
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "TERMINAL LOGS",
        color = TextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
      )
      if (chatHistory.isNotEmpty()) {
        Text(
          text = "CLEAR LOGS",
          color = DangerRed,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier
            .clickable { viewModel.clearChatHistory() }
            .padding(4.dp)
        )
      }
    }

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(TitaniumSurface)
        .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
        .padding(8.dp)
    ) {
      if (chatHistory.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Terminal initialized. Say 'Utho Nova' or type a command.",
            color = TextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      } else {
        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(chatHistory, key = { it.id }) { msg ->
            NovaChatLogItem(
              message = msg,
              onReplay = { viewModel.voiceManager.speak(msg.message) }
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 5. Command Bar (Text Input + Voice Mic Button + Send)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(CyberSurfaceVariant)
        .border(1.dp, if (isListening) CyanPrimary else CyberBorder, RoundedCornerShape(16.dp))
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Voice Mic Button
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(if (isListening) DangerRed else CyanPrimary)
          .clickable {
            if (isListening) viewModel.stopVoiceListening() else viewModel.startVoiceListening()
          }
          .testTag("voice_mic_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
          contentDescription = "Voice Command",
          tint = if (isListening) Color.White else Color(0xFF001A24),
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Input field
      OutlinedTextField(
        value = inputText,
        onValueChange = { viewModel.setInputText(it) },
        placeholder = {
          Text(
            text = "Say 'Utho Nova' or type command...",
            color = TextSecondary.copy(alpha = 0.6f),
            fontSize = 12.sp
          )
        },
        modifier = Modifier
          .weight(1f)
          .testTag("command_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          focusedBorderColor = Color.Transparent,
          unfocusedBorderColor = Color.Transparent,
          cursorColor = CyanPrimary
        ),
        singleLine = true
      )

      // Send Button
      IconButton(
        onClick = { viewModel.handleUserCommand(inputText) },
        enabled = inputText.isNotBlank(),
        modifier = Modifier.testTag("send_command_button")
      ) {
        Icon(
          imageVector = Icons.Default.Send,
          contentDescription = "Send Command",
          tint = if (inputText.isNotBlank()) CyanPrimary else TextSecondary.copy(alpha = 0.4f)
        )
      }
    }
  }
}

@Composable
fun NovaChatLogItem(
  message: ChatMessage,
  onReplay: () -> Unit
) {
  val isUser = message.sender == "user"
  val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  val timeStr = timeFormat.format(Date(message.timestamp))

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 2.dp)
    ) {
      Text(
        text = if (isUser) "COMMANDER" else "N.O.V.A.",
        color = if (isUser) SkySecondary else CyanPrimary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = " • $timeStr",
        color = TextSecondary,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(start = 4.dp)
      )
    }

    Box(
      modifier = Modifier
        .clip(
          RoundedCornerShape(
            topStart = 10.dp,
            topEnd = 10.dp,
            bottomStart = if (isUser) 10.dp else 2.dp,
            bottomEnd = if (isUser) 2.dp else 10.dp
          )
        )
        .background(
          if (isUser) Color(0xFF0F2742) else Color(0xFF081422)
        )
        .border(
          1.dp,
          if (isUser) SkySecondary.copy(alpha = 0.5f) else CyanPrimary.copy(alpha = 0.35f),
          RoundedCornerShape(10.dp)
        )
        .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Column {
        Text(
          text = message.message,
          color = TextPrimary,
          fontSize = 13.sp,
          lineHeight = 18.sp
        )

        // Action Badge if action was triggered
        if (message.actionType != null) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFF022B3A))
              .border(0.5.dp, CyanGlow, RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Action Executed",
              tint = SuccessGreen,
              modifier = Modifier.size(12.dp)
            )
            Text(
              text = "${message.actionType}: ${message.actionDetails ?: "Executed"}",
              color = CyanGlow,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.padding(start = 4.dp)
            )
          }
        }

        // Voice Replay button for Nova messages
        if (!isUser) {
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier
              .clickable { onReplay() }
              .padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.VolumeUp,
              contentDescription = "Speak Aloud",
              tint = TextSecondary,
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = "Speak",
              color = TextSecondary,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.padding(start = 3.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun QuickActionChip(
  icon: ImageVector,
  label: String,
  accentColor: Color,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(Color(0xFF0D1B2A))
      .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 7.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = accentColor,
      modifier = Modifier.size(16.dp)
    )
    Text(
      text = label,
      color = TextPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(start = 6.dp)
    )
  }
}


