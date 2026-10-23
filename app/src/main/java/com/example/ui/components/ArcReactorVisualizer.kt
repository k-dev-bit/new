package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkNavyTertiary
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.StarkGold

@Composable
fun ArcReactorVisualizer(
  modifier: Modifier = Modifier,
  size: Dp = 140.dp,
  isListening: Boolean = false,
  isSpeaking: Boolean = false,
  amplitude: Float = 0.2f,
  onClick: () -> Unit = {}
) {
  val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")

  // Outer rotation
  val outerRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = if (isListening) 4000 else 12000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "outer_rotation"
  )

  // Inner counter-rotation
  val innerRotation by infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = if (isSpeaking) 3000 else 8000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "inner_rotation"
  )

  // Glow pulse
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = if (isListening || isSpeaking) 600 else 1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_pulse"
  )

  val dynamicGlowColor = when {
    isListening -> CyanGlow
    isSpeaking -> StarkGold
    else -> CyanPrimary
  }

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
      val radius = (size.toPx() / 2f) * 0.92f

      // 1. Ambient Glow Backing
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(dynamicGlowColor.copy(alpha = 0.35f * pulseScale), Color.Transparent),
          center = center,
          radius = radius * 1.15f
        ),
        radius = radius * 1.15f,
        center = center
      )

      // 2. Outer Armor Ring
      drawCircle(
        color = Color(0xFF13283E),
        radius = radius,
        center = center,
        style = Stroke(width = 4.dp.toPx())
      )

      // 3. Segmented Rotating Outer Ring (10 segments)
      rotate(outerRotation, pivot = center) {
        val segmentCount = 10
        val sweepAngle = 26f
        val gap = (360f - (segmentCount * sweepAngle)) / segmentCount
        for (i in 0 until segmentCount) {
          val startAngle = i * (sweepAngle + gap)
          drawArc(
            color = dynamicGlowColor.copy(alpha = 0.85f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius * 0.88f, center.y - radius * 0.88f),
            size = androidx.compose.ui.geometry.Size(radius * 1.76f, radius * 1.76f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
          )
        }
      }

      // 4. Inner Mid Ring Counter-Rotating
      rotate(innerRotation, pivot = center) {
        val count = 8
        val sweep = 20f
        val gap = (360f - (count * sweep)) / count
        for (i in 0 until count) {
          val start = i * (sweep + gap)
          drawArc(
            color = SkySecondary,
            startAngle = start,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(center.x - radius * 0.65f, center.y - radius * 0.65f),
            size = androidx.compose.ui.geometry.Size(radius * 1.3f, radius * 1.3f),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Square)
          )
        }
      }

      // 5. Dynamic Core Energy Orb
      val coreRadius = (radius * 0.42f) * (0.85f + (amplitude * 0.35f))
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.White,
            dynamicGlowColor,
            DarkNavyTertiary,
            Color(0xFF060B12)
          ),
          center = center,
          radius = coreRadius
        ),
        radius = coreRadius,
        center = center
      )

      // 6. Center Triangle / Arc Core Reticle
      drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = coreRadius * 0.35f,
        center = center,
        style = Stroke(width = 1.8.dp.toPx())
      )
    }

    // Status Label in center
    val statusText = when {
      isListening -> "LISTENING"
      isSpeaking -> "SPEAKING"
      else -> "NOVA"
    }
    Text(
      text = statusText,
      color = Color.White,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.2.sp
    )
  }
}
