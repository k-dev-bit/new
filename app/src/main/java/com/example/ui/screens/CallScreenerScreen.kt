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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.call.CallScreenState
import com.example.controller.SystemActionController
import com.example.data.model.ScreenedCallRecord
import com.example.ui.NovaViewModel
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

@Composable
fun CallScreenerScreen(viewModel: NovaViewModel) {
  val context = LocalContext.current
  val prefs = viewModel.preferences
  val screenState by viewModel.callManager.screenState.collectAsState()
  val screenedCalls by viewModel.screenedCalls.collectAsState()

  var screeningEnabled by remember { mutableStateOf(prefs.callScreeningEnabled) }
  var timeoutSec by remember { mutableFloatStateOf(prefs.callScreeningTimeoutSec.toFloat()) }
  var customScript by remember { mutableStateOf(prefs.callScreeningScript) }
  var rejectionMsg by remember { mutableStateOf(prefs.callScreeningRejectionMsg) }
  var urgentKeywords by remember { mutableStateOf(prefs.urgentKeywords) }
  var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "NOVA AI CALL SCREENER",
            color = CyanPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
          )
          Text(
            text = "Autonomous AI screening for incoming calls",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }

        Switch(
          checked = screeningEnabled,
          onCheckedChange = {
            screeningEnabled = it
            prefs.callScreeningEnabled = it
          },
          colors = SwitchDefaults.colors(
            checkedThumbColor = CyanGlow,
            checkedTrackColor = CyanPrimary.copy(alpha = 0.5f),
            uncheckedTrackColor = TitaniumSurface
          ),
          modifier = Modifier.testTag("call_screening_toggle")
        )
      }
    }

    // 2. Interactive Screening Simulator Bar
    item {
      CardBox {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.SupportAgent, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
              Text(
                text = " LIVE SCREENING TESTER",
                color = CyanPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            Button(
              onClick = {
                viewModel.callManager.triggerIncomingCall("Rajesh Sharma - Bank Loan", "+91 98765 43210")
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF001A24)),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.testTag("simulate_incoming_call_button")
            ) {
              Icon(Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(16.dp))
              Text(text = " Test Incoming Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Display state transitions
          when (val state = screenState) {
            is CallScreenState.Idle -> {
              Text(
                text = "Screening standby. If an incoming call is unanswered for ${timeoutSec.toInt()}s, Nova will autonomously intercept and question the caller.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
              )
            }

            is CallScreenState.Ringing -> {
              RingingCallBox(
                callerName = state.callerName,
                callerPhone = state.callerPhone,
                secondsLeft = state.secondsLeft,
                onAnswerMyself = { viewModel.callManager.dismissCall() },
                onDelegateJarvis = { viewModel.callManager.delegateToNova() }
              )
            }

            is CallScreenState.JarvisScreening -> {
              JarvisActiveDialogueBox(
                state = state,
                onSubmitCallerUtterance = { viewModel.callManager.submitCallerResponse(it) },
                onDismiss = { viewModel.callManager.dismissCall() }
              )
            }

            is CallScreenState.Finished -> {
              FinishedScreeningBox(
                record = state.record,
                onDone = { viewModel.callManager.dismissCall() }
              )
            }
          }
        }
      }
    }

    // 3. Custom Commands & Speech Configuration (Assistant Se Kya Bulvana Hai)
    item {
      CardBox {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "CUSTOM SCRIPT & VOICE DIRECTIVES",
            color = StarkGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )

          // 15s Timer configuration
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Unanswered Call Trigger Delay",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "${timeoutSec.toInt()} Seconds",
                color = CyanPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            Slider(
              value = timeoutSec,
              onValueChange = { timeoutSec = it },
              valueRange = 5f..30f,
              steps = 4,
              colors = SliderDefaults.colors(
                thumbColor = CyanPrimary,
                activeTrackColor = CyanGlow,
                inactiveTrackColor = CyberBorder
              ),
              modifier = Modifier.testTag("timeout_slider")
            )
          }

          // Custom Assistant Opening Line ("assistant Se Kya bulvana Hai")
          Column {
            Text(
              text = "What Nova Speaks to Caller (Opening Script):",
              color = TextPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
              value = customScript,
              onValueChange = { customScript = it },
              placeholder = { Text("Write what Nova should say when answering...") },
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .testTag("custom_screening_script_input"),
              maxLines = 4,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = CyberBorder,
                focusedContainerColor = Color(0xFF091420),
                unfocusedContainerColor = Color(0xFF091420)
              )
            )
          }

          // Routine rejection message
          Column {
            Text(
              text = "Routine / Non-Urgent Rejection Message:",
              color = TextPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
              value = rejectionMsg,
              onValueChange = { rejectionMsg = it },
              placeholder = { Text("What Nova says if call is not urgent...") },
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
              maxLines = 3,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = CyberBorder,
                focusedContainerColor = Color(0xFF091420),
                unfocusedContainerColor = Color(0xFF091420)
              )
            )
          }

          // Urgent Keywords
          Column {
            Text(
              text = "Urgent Emergency Keywords (comma separated):",
              color = TextPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
              value = urgentKeywords,
              onValueChange = { urgentKeywords = it },
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = CyberBorder,
                focusedContainerColor = Color(0xFF091420),
                unfocusedContainerColor = Color(0xFF091420)
              )
            )
          }

          // Save Button
          Button(
            onClick = {
              prefs.callScreeningTimeoutSec = timeoutSec.toInt()
              prefs.callScreeningScript = customScript
              prefs.callScreeningRejectionMsg = rejectionMsg
              prefs.urgentKeywords = urgentKeywords
              saveSuccessMessage = "Settings and custom speech script updated!"
              viewModel.voiceManager.speak("Call screening protocol configured, Sir.")
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF001A24)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("save_screening_config_button")
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text = " Save Screening Protocol", fontWeight = FontWeight.Bold)
          }

          saveSuccessMessage?.let { msg ->
            Text(
              text = msg,
              color = SuccessGreen,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.padding(top = 2.dp)
            )
          }
        }
      }
    }

    // 4. Screened Calls Inbox
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "SCREENED CALLS INBOX (${screenedCalls.size})",
          color = CyanPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )
        if (screenedCalls.isNotEmpty()) {
          Text(
            text = "CLEAR ALL",
            color = DangerRed,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
              .clickable { viewModel.clearScreenedCalls() }
              .padding(4.dp)
          )
        }
      }
    }

    if (screenedCalls.isEmpty()) {
      item {
        CardBox {
          Text(
            text = "No calls screened yet. Intercepted caller messages will appear here with full transcript & urgency classification.",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }
    } else {
      items(screenedCalls, key = { it.id }) { call ->
        ScreenedCallCard(
          record = call,
          onCallBack = { SystemActionController.makeCall(context, call.callerPhone) },
          onPlayTranscript = { viewModel.voiceManager.speak(call.transcript) },
          onDelete = { viewModel.deleteScreenedCall(call.id) }
        )
      }
    }
  }
}

@Composable
fun RingingCallBox(
  callerName: String,
  callerPhone: String,
  secondsLeft: Int,
  onAnswerMyself: () -> Unit,
  onDelegateJarvis: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF140D05))
      .border(1.dp, StarkAmber, RoundedCornerShape(12.dp))
      .padding(14.dp)
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "INCOMING CALL RINGING",
        color = StarkAmber,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = callerName,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
      )
      Text(
        text = callerPhone,
        color = TextSecondary,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Countdown timer badge
      Row(
        modifier = Modifier
          .clip(CircleShape)
          .background(StarkAmber.copy(alpha = 0.2f))
          .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = StarkAmber, modifier = Modifier.size(16.dp))
        Text(
          text = " Auto-Screening in ${secondsLeft}s",
          color = StarkAmber,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onAnswerMyself,
          colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color(0xFF00291D)),
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("I'll Answer", fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = onDelegateJarvis,
          colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF001A24)),
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Let Nova Answer", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun JarvisActiveDialogueBox(
  state: CallScreenState.JarvisScreening,
  onSubmitCallerUtterance: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var testCustomMessage by remember { mutableStateOf("") }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF041926))
      .border(1.dp, CyanGlow, RoundedCornerShape(12.dp))
      .padding(14.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(20.dp))
          Text(
            text = " NOVA ON CALL WITH ${state.callerName.uppercase()}",
            color = CyanGlow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.CallEnd, contentDescription = "End", tint = DangerRed)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Nova Spoke: \"${state.jarvisSpeech}\"",
        color = TextPrimary,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 16.sp,
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF072435))
          .padding(8.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Test Caller Replies (Simulate caller's response):",
        color = TextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(6.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedButton(
          onClick = { onSubmitCallerUtterance("Emergency! Please tell Bharat his uncle had an accident and is in hospital.") },
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text("🚨 Emergency Test", fontSize = 10.sp)
        }

        OutlinedButton(
          onClick = { onSubmitCallerUtterance("Hi, this is Suresh. Just calling about tomorrow's lunch meeting.") },
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = SkySecondary),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text("💼 Routine Test", fontSize = 10.sp)
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
          value = testCustomMessage,
          onValueChange = { testCustomMessage = it },
          placeholder = { Text("Or type caller's response...", fontSize = 11.sp) },
          modifier = Modifier.weight(1f),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )
        IconButton(
          onClick = {
            if (testCustomMessage.isNotBlank()) {
              onSubmitCallerUtterance(testCustomMessage)
            }
          }
        ) {
          Icon(Icons.Default.Send, contentDescription = "Send", tint = CyanPrimary)
        }
      }
    }
  }
}

@Composable
fun FinishedScreeningBox(
  record: ScreenedCallRecord,
  onDone: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(if (record.isUrgent) Color(0xFF260A10) else Color(0xFF042018))
      .border(1.dp, if (record.isUrgent) DangerRed else SuccessGreen, RoundedCornerShape(12.dp))
      .padding(14.dp)
  ) {
    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (record.isUrgent) Icons.Default.Warning else Icons.Default.CheckCircle,
          contentDescription = null,
          tint = if (record.isUrgent) DangerRed else SuccessGreen
        )
        Text(
          text = if (record.isUrgent) " URGENT CALL INTERCEPTED!" else " CALL SCREENED & RESOLVED",
          color = if (record.isUrgent) DangerRed else SuccessGreen,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Caller: ${record.callerName} (${record.callerPhone})",
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Message: \"${record.callerMessage}\"",
        color = TextPrimary,
        fontSize = 12.sp,
        modifier = Modifier.padding(vertical = 4.dp)
      )

      Button(
        onClick = onDone,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (record.isUrgent) DangerRed else SuccessGreen
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 6.dp),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Dismiss to Inbox", color = Color.White, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun ScreenedCallCard(
  record: ScreenedCallRecord,
  onCallBack: () -> Unit,
  onPlayTranscript: () -> Unit,
  onDelete: () -> Unit
) {
  val timeFormat = SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault())
  val timeStr = timeFormat.format(Date(record.timestamp))

  CardBox {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = record.callerName,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (record.isUrgent) DangerRed.copy(alpha = 0.25f) else SuccessGreen.copy(alpha = 0.2f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = if (record.isUrgent) "URGENT" else "ROUTINE",
              color = if (record.isUrgent) DangerRed else SuccessGreen,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Text(
          text = timeStr,
          color = TextSecondary,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Text(
        text = record.callerPhone,
        color = TextSecondary,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Message: ${record.callerMessage}",
        color = TextPrimary,
        fontSize = 12.sp,
        lineHeight = 16.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = onCallBack,
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color(0xFF00291D)),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(6.dp)
          ) {
            Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(14.dp))
            Text(text = " Call Back", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = onPlayTranscript,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(6.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(14.dp))
            Text(text = " Listen", fontSize = 11.sp)
          }
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed.copy(alpha = 0.8f))
        }
      }
    }
  }
}

@Composable
fun CardBox(content: @Composable () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(TitaniumSurface)
      .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
      .padding(14.dp)
  ) {
    content()
  }
}
