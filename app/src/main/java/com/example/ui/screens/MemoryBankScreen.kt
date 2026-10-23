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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JarvisMemory
import com.example.ui.NovaViewModel
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.StarkAmber
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TitaniumSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoryBankScreen(viewModel: NovaViewModel) {
  val memories by viewModel.memoryList.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("All") }
  var showAddDialog by remember { mutableStateOf(false) }
  var showClearAllDialog by remember { mutableStateOf(false) }

  val categories = listOf("All", "Personal", "Saved Facts", "Contact", "Custom Command", "Preference")

  val filteredMemories = memories.filter { memory ->
    val matchesCategory = (selectedCategory == "All") || (memory.category.equals(selectedCategory, ignoreCase = true))
    val matchesSearch = searchQuery.isBlank() ||
      memory.keyTitle.contains(searchQuery, ignoreCase = true) ||
      memory.content.contains(searchQuery, ignoreCase = true)
    matchesCategory && matchesSearch
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .padding(16.dp)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "LONG-TERM MEMORY BANK",
            color = CyanPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
          )
          Text(
            text = "Persistent on-device storage • ${memories.size} records",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }

        if (memories.isNotEmpty()) {
          OutlinedButton(
            onClick = { showClearAllDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.testTag("wipe_all_memories_button")
          ) {
            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Wipe", modifier = Modifier.size(16.dp))
            Text(text = " WIPE ALL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search field
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search memories or facts...", color = TextSecondary.copy(alpha = 0.6f), fontSize = 12.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CyanPrimary) },
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(CyberSurfaceVariant),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          focusedBorderColor = CyanPrimary,
          unfocusedBorderColor = CyberBorder
        ),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Category Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
      ) {
        items(categories) { cat ->
          val isSelected = selectedCategory == cat
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) CyanPrimary else TitaniumSurface)
              .border(1.dp, if (isSelected) CyanGlow else CyberBorder, RoundedCornerShape(8.dp))
              .clickable { selectedCategory = cat }
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = cat,
              color = if (isSelected) Color(0xFF001A24) else TextSecondary,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Memories List
      if (filteredMemories.isEmpty()) {
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
            Icon(Icons.Default.Bookmark, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (searchQuery.isNotBlank()) "No records matching query" else "Memory bank empty.",
              color = TextSecondary,
              fontSize = 13.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Say 'Utho Nova, yaad rakho [fact]' or tap '+' to commit to memory.",
              color = TextSecondary.copy(alpha = 0.7f),
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredMemories, key = { it.id }) { memory ->
            MemoryCard(
              memory = memory,
              onSpeak = { viewModel.voiceManager.speak("${memory.keyTitle}: ${memory.content}") },
              onDelete = { viewModel.deleteMemory(memory.id) }
            )
          }
        }
      }
    }

    // Add Memory FAB
    FloatingActionButton(
      onClick = { showAddDialog = true },
      containerColor = CyanPrimary,
      contentColor = Color(0xFF001A24),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 12.dp, end = 4.dp)
        .testTag("add_memory_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add Memory")
    }
  }

  // Add Memory Dialog
  if (showAddDialog) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = {
        Text("Save to Memory Bank", color = CyanPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title / Key (e.g. Papa's Birthday)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Information to Remember") },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (Personal, Contact, Fact)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (content.isNotBlank()) {
              viewModel.addCustomMemory(title, content, category)
              showAddDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF001A24))
        ) {
          Text("Commit to Memory")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showAddDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      },
      containerColor = TitaniumSurface
    )
  }

  // Wipe All Dialog
  if (showClearAllDialog) {
    AlertDialog(
      onDismissRequest = { showClearAllDialog = false },
      title = { Text("Purge Entire Memory Bank?", color = DangerRed, fontWeight = FontWeight.Bold) },
      text = {
        Text(
          "Are you sure you want to delete all saved memories? This action is permanent and cannot be undone.",
          color = TextPrimary
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearAllMemories()
            showClearAllDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
        ) {
          Text("Yes, Wipe All", color = Color.White)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showClearAllDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      },
      containerColor = TitaniumSurface
    )
  }
}

@Composable
fun MemoryCard(
  memory: JarvisMemory,
  onSpeak: () -> Unit,
  onDelete: () -> Unit
) {
  val timeFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
  val timeStr = timeFormat.format(Date(memory.timestamp))

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(TitaniumSurface)
      .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = memory.keyTitle,
            color = CyanPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFF072738))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = memory.category.uppercase(),
              color = SkySecondary,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Row {
          IconButton(onClick = onSpeak, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.VolumeUp, contentDescription = "Speak", tint = TextSecondary, modifier = Modifier.size(16.dp))
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = memory.content,
        color = TextPrimary,
        fontSize = 13.sp,
        lineHeight = 18.sp
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Logged: $timeStr",
        color = TextSecondary.copy(alpha = 0.7f),
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
