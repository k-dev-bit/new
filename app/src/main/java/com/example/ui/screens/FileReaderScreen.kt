package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NovaViewModel
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TitaniumSurface

@Composable
fun FileReaderScreen(
  viewModel: NovaViewModel,
  onPickFile: () -> Unit
) {
  val activeFileContent by viewModel.activeFileContent.collectAsState()
  val activeFileName by viewModel.activeFileName.collectAsState()
  val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
  val speechAmplitude by viewModel.voiceManager.speechAmplitude.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .padding(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "FILE MANAGER & VOICE READER",
          color = CyanPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.2.sp
        )
        Text(
          text = "Select any file to read and hear it spoken aloud by Nova",
          color = TextSecondary,
          fontSize = 11.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Top action bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = onPickFile,
        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF001A24)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("pick_file_button")
      ) {
        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(text = " Open File Manager", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }

      OutlinedButton(
        onClick = {
          val sampleText = """
STARK INDUSTRIES AUTOMATION REPORT
Project: J.A.R.V.I.S. Android Integration
Status: All Core Protocols Operational.

1. Phone Controls: YouTube video launcher, contact dialer, flashlight relay, and security lock routines are active.
2. Call Screener: Configured with 15-second autonomous answer timeout. Incoming callers will be screened and messages filed in user inbox.
3. Multi-AI Architecture: Hybrid local offline intelligence with support for Gemini 2.5 Flash, OpenAI GPT-4o, and Groq Llama 3.
4. Memory Retention: All persistent memories and transcripts safely secured in local Room database.

Have a productive day, Sir.
          """.trimIndent()
          viewModel.loadFileFromUri(android.net.Uri.EMPTY, "Stark_Core_Briefing.txt")
          // Inject content
          viewModel.handleUserCommand("File loaded: Stark_Core_Briefing.txt")
        },
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 10.dp)
      ) {
        Text("Load Sample", color = SkySecondary, fontSize = 11.sp)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Document Content Display Area
    if (activeFileContent == null) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(TitaniumSurface)
          .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Description, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(52.dp))
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "No document selected",
            color = TextSecondary,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Tap 'Open File Manager' to choose any text, code, pdf, or log file.",
            color = TextSecondary.copy(alpha = 0.7f),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    } else {
      // Document active
      CardBox {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Description, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
              Text(
                text = " ${activeFileName ?: "Document"}",
                color = CyanPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            IconButton(onClick = { viewModel.clearActiveFile() }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = DangerRed)
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          val wordCount = activeFileContent?.split("\\s+".toRegex())?.size ?: 0
          Text(
            text = "$wordCount words • Estimated speech time: ${wordCount / 130 + 1} min",
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Waveform indicator when speaking
          if (isSpeaking) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF072435))
                .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "NOVA READING ALOUD...",
                color = StarkGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              VoiceWaveformVisualizer(
                isActive = true,
                amplitude = speechAmplitude,
                barCount = 12,
                color = StarkGold
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
          }

          // Voice Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { viewModel.readLoadedFileAloud() },
              colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF001A24)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).testTag("read_aloud_button")
            ) {
              Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
              Text(text = " Read Aloud", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            if (isSpeaking) {
              Button(
                onClick = { viewModel.stopSpeaking() },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(text = " Stop Voice", fontWeight = FontWeight.Bold, fontSize = 11.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Scrollable Document Text
          Box(
            modifier = Modifier
              .weight(1f, fill = false)
              .height(300.dp)
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF060D15))
              .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
              .padding(12.dp)
              .verticalScroll(rememberScrollState())
          ) {
            Text(
              text = activeFileContent ?: "",
              color = TextPrimary,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              lineHeight = 18.sp
            )
          }
        }
      }
    }
  }
}
