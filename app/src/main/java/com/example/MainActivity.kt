package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.NovaTab
import com.example.ui.NovaViewModel
import com.example.ui.screens.AiProvidersScreen
import com.example.ui.screens.CallScreenerScreen
import com.example.ui.screens.FileReaderScreen
import com.example.ui.screens.NovaHudScreen
import com.example.ui.screens.MemoryBankScreen
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TitaniumSurface

class MainActivity : ComponentActivity() {

  private var pendingTriggerVoice = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    pendingTriggerVoice = intent?.getBooleanExtra("TRIGGER_VOICE", false) == true

    setContent {
      MyApplicationTheme {
        val viewModel: NovaViewModel = viewModel()
        val currentTab by viewModel.currentTab.collectAsState()
        val context = LocalContext.current

        LaunchedEffect(pendingTriggerVoice) {
          if (pendingTriggerVoice) {
            viewModel.startVoiceListening()
            pendingTriggerVoice = false
          }
        }

        // BackHandler: navigate back to HUD from secondary tabs
        BackHandler(enabled = currentTab != NovaTab.HUD) {
          viewModel.setTab(NovaTab.HUD)
        }

        // File picker launcher
        val openDocumentLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
          if (uri != null) {
            var fileName = "Document"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
              val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
              if (nameIdx >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIdx)
              }
            }
            viewModel.loadFileFromUri(uri, fileName)
          }
        }

        // Runtime permission request
        val permissionLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
          // If audio permission granted, start continuous wake-word listener
          if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
            viewModel.voiceManager.toggleContinuousWakeWord(true)
          }
        }

        LaunchedEffect(Unit) {
          val needed = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CAMERA
          )
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
          }
          val toRequest = needed.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
          }
          if (toRequest.isNotEmpty()) {
            permissionLauncher.launch(toRequest.toTypedArray())
          }
        }

        NovaAppScaffold(
          viewModel = viewModel,
          currentTab = currentTab,
          onOpenDocPicker = {
            openDocumentLauncher.launch(
              arrayOf(
                "text/plain",
                "text/*",
                "application/pdf",
                "application/json",
                "application/msword",
                "*/*"
              )
            )
          }
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    if (intent.getBooleanExtra("TRIGGER_VOICE", false)) {
      pendingTriggerVoice = true
    }
  }
}

@OptIn(
  ExperimentalMaterial3Api::class,
  androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
@Composable
fun NovaAppScaffold(
  viewModel: NovaViewModel,
  currentTab: NovaTab,
  onOpenDocPicker: () -> Unit
) {
  val isImeVisible = WindowInsets.isImeVisible

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(CyanGlow)
            )
            Text(
              text = " N.O.V.A.",
              color = CyanPrimary,
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 2.sp
            )
            Text(
              text = " // NEURAL VOICE AI",
              color = TextSecondary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.padding(start = 6.dp)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = ObsidianBackground
        )
      )
    },
    bottomBar = {
      if (!isImeVisible) {
        NavigationBar(
          containerColor = TitaniumSurface,
          modifier = Modifier.border(1.dp, CyberBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
          NovaTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            val icon = when (tab) {
              NovaTab.HUD -> Icons.Default.Adjust
              NovaTab.MEMORY -> Icons.Default.Bookmark
              NovaTab.CALL_SCREEN -> Icons.Default.PhoneInTalk
              NovaTab.AI_MODELS -> Icons.Default.SmartToy
              NovaTab.FILE_READER -> Icons.Default.Description
            }

            NavigationBarItem(
              selected = isSelected,
              onClick = { viewModel.setTab(tab) },
              icon = {
                Icon(
                  imageVector = icon,
                  contentDescription = tab.title,
                  tint = if (isSelected) CyanGlow else TextSecondary
                )
              },
              label = {
                Text(
                  text = tab.title,
                  fontSize = 9.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontFamily = FontFamily.Monospace
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanGlow,
                selectedTextColor = CyanPrimary,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color(0xFF0C2B40)
              )
            )
          }
        }
      }
    },
    contentWindowInsets = WindowInsets.safeDrawing,
    containerColor = ObsidianBackground
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .consumeWindowInsets(paddingValues)
    ) {
      when (currentTab) {
        NovaTab.HUD -> NovaHudScreen(
          viewModel = viewModel,
          onOpenDocPicker = onOpenDocPicker
        )
        NovaTab.MEMORY -> MemoryBankScreen(viewModel = viewModel)
        NovaTab.CALL_SCREEN -> CallScreenerScreen(viewModel = viewModel)
        NovaTab.AI_MODELS -> AiProvidersScreen(viewModel = viewModel)
        NovaTab.FILE_READER -> FileReaderScreen(
          viewModel = viewModel,
          onPickFile = onOpenDocPicker
        )
      }
    }
  }
}
