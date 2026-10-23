package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.StarkAmber
import com.example.ui.theme.StarkGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary

@Composable
fun SystemTelemetryBar(
  modifier: Modifier = Modifier,
  activeModelName: String,
  isTorchOn: Boolean,
  isServiceActive: Boolean,
  isWakeWordActive: Boolean = true,
  memoryCount: Int,
  onTorchToggle: () -> Unit,
  onServiceToggle: () -> Unit,
  onWakeWordToggle: () -> Unit = {}
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(CyberSurfaceVariant.copy(alpha = 0.8f))
      .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // 1. Status & Core
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(SuccessGreen)
      )
      Column(modifier = Modifier.padding(start = 6.dp)) {
        Text(
          text = "NOVA ONLINE",
          color = CyanPrimary,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.8.sp
        )
        Text(
          text = activeModelName,
          color = TextSecondary,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    // 2. Continuous Wake-Word ("Utho Nova") Button
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(if (isWakeWordActive) CyanPrimary.copy(alpha = 0.2f) else Color.Transparent)
        .border(1.dp, if (isWakeWordActive) CyanPrimary else CyberBorder, RoundedCornerShape(8.dp))
        .clickable { onWakeWordToggle() }
        .padding(horizontal = 6.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = if (isWakeWordActive) Icons.Default.Mic else Icons.Default.MicOff,
        contentDescription = "Wake-word listener",
        tint = if (isWakeWordActive) CyanGlow else TextSecondary,
        modifier = Modifier.size(13.dp)
      )
      Text(
        text = if (isWakeWordActive) "UTHO NOVA" else "WAKE OFF",
        color = if (isWakeWordActive) CyanPrimary else TextSecondary,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(start = 3.dp)
      )
    }

    // 3. Quick Torch Button
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(if (isTorchOn) StarkAmber.copy(alpha = 0.2f) else Color.Transparent)
        .border(1.dp, if (isTorchOn) StarkAmber else CyberBorder, RoundedCornerShape(8.dp))
        .clickable { onTorchToggle() }
        .padding(horizontal = 6.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
        contentDescription = "Torch",
        tint = if (isTorchOn) StarkGold else TextSecondary,
        modifier = Modifier.size(13.dp)
      )
      Text(
        text = if (isTorchOn) "TORCH ON" else "TORCH",
        color = if (isTorchOn) StarkGold else TextSecondary,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(start = 3.dp)
      )
    }

    // 4. Background Guardian Service Button
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(if (isServiceActive) CyanPrimary.copy(alpha = 0.2f) else Color.Transparent)
        .border(1.dp, if (isServiceActive) CyanPrimary else CyberBorder, RoundedCornerShape(8.dp))
        .clickable { onServiceToggle() }
        .padding(horizontal = 6.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Shield,
        contentDescription = "Guardian Service",
        tint = if (isServiceActive) CyanGlow else TextSecondary,
        modifier = Modifier.size(13.dp)
      )
      Text(
        text = if (isServiceActive) "BG ACTIVE" else "BG STANDBY",
        color = if (isServiceActive) CyanPrimary else TextSecondary,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(start = 3.dp)
      )
    }
  }
}
