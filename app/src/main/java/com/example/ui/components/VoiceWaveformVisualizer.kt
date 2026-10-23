package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StarkGold

@Composable
fun VoiceWaveformVisualizer(
  modifier: Modifier = Modifier,
  isActive: Boolean = false,
  amplitude: Float = 0.2f,
  barCount: Int = 18,
  maxHeight: Dp = 32.dp,
  barWidth: Dp = 3.dp,
  color: Color = CyanPrimary
) {
  val infiniteTransition = rememberInfiniteTransition(label = "waveform")

  Row(
    modifier = modifier.height(maxHeight),
    horizontalArrangement = Arrangement.spacedBy(3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (i in 0 until barCount) {
      val delay = (i * 70) % 500
      val animMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = 400 + (i % 4) * 80, delayMillis = delay, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse
        ),
        label = "bar_$i"
      )

      val dynamicHeightFraction = if (isActive) {
        val base = 0.25f + (amplitude * 0.75f) * animMultiplier
        base.coerceIn(0.15f, 1.0f)
      } else {
        0.15f
      }

      val barColor = if (isActive && amplitude > 0.6f) StarkGold else color

      Box(
        modifier = Modifier
          .width(barWidth)
          .fillMaxHeight(dynamicHeightFraction)
          .clip(RoundedCornerShape(2.dp))
          .background(barColor)
      )
    }
  }
}
