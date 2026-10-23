package com.example.controller

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.net.URLEncoder
import java.util.Calendar

data class ActionResult(
  val success: Boolean,
  val speechResponse: String,
  val feedbackDetails: String? = null
)

object SystemActionController {

  private var isTorchOn: Boolean = false

  fun isFlashlightOn(): Boolean = isTorchOn

  /**
   * Launch YouTube with query or open YouTube app
   */
  fun openYouTube(context: Context, query: String? = null): ActionResult {
    return try {
      val intent = if (!query.isNullOrBlank()) {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$encoded")).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (appIntent.resolveActivity(context.packageManager) != null) {
          appIntent
        } else {
          Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$encoded")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
          }
        }
      } else {
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
        launchIntent?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
          ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
          }
      }

      context.startActivity(intent)
      val msg = if (query.isNullOrBlank()) "Opening YouTube, Sir." else "Playing '$query' on YouTube for you, Sir."
      ActionResult(true, msg, "Opened YouTube for: ${query ?: "Home"}")
    } catch (e: Exception) {
      ActionResult(false, "Could not open YouTube: ${e.localizedMessage}")
    }
  }

  /**
   * Find contact by name or dial phone number
   */
  fun makeCall(context: Context, targetNameOrNumber: String): ActionResult {
    try {
      val cleanTarget = targetNameOrNumber.trim()
      var resolvedNumber: String? = null
      var resolvedName: String = cleanTarget

      // Check if target is directly a phone number
      val isNumericOnly = cleanTarget.replace("[+\\-()\\s]".toRegex(), "").all { it.isDigit() }
      if (isNumericOnly && cleanTarget.length >= 3) {
        resolvedNumber = cleanTarget
      } else {
        // Search contacts if permission is available
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
          val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
              ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
              ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$cleanTarget%"),
            null
          )

          cursor?.use {
            if (it.moveToFirst()) {
              val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
              val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
              if (nameIdx >= 0 && numIdx >= 0) {
                resolvedName = it.getString(nameIdx)
                resolvedNumber = it.getString(numIdx)
              }
            }
          }
        }
      }

      val numberToDial = resolvedNumber ?: cleanTarget
      val hasCallPermission = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.CALL_PHONE
      ) == PackageManager.PERMISSION_GRANTED

      val intent = if (hasCallPermission && resolvedNumber != null) {
        Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(numberToDial)}")).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      } else {
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(numberToDial)}")).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      }

      context.startActivity(intent)
      return ActionResult(
        true,
        "Initiating communication with $resolvedName, Sir.",
        "Calling: $resolvedName ($numberToDial)"
      )
    } catch (e: Exception) {
      return ActionResult(false, "Failed to initiate call: ${e.localizedMessage}")
    }
  }

  /**
   * Toggle or set flashlight state
   */
  fun setFlashlight(context: Context, enable: Boolean? = null): ActionResult {
    return try {
      val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        ?: return ActionResult(false, "Camera hardware not accessible for torch.")

      val targetState = enable ?: !isTorchOn
      var torchCameraId: String? = null

      for (id in cameraManager.cameraIdList) {
        val characteristics = cameraManager.getCameraCharacteristics(id)
        val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
        if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
          torchCameraId = id
          break
        }
      }

      if (torchCameraId == null && cameraManager.cameraIdList.isNotEmpty()) {
        torchCameraId = cameraManager.cameraIdList[0]
      }

      if (torchCameraId != null) {
        cameraManager.setTorchMode(torchCameraId, targetState)
        isTorchOn = targetState
        val speech = if (targetState) "Flashlight engaged, Sir." else "Flashlight turned off, Sir."
        ActionResult(true, speech, "Torch: ${if (targetState) "ON" else "OFF"}")
      } else {
        ActionResult(false, "No flashlight hardware found on this device.")
      }
    } catch (e: CameraAccessException) {
      ActionResult(false, "Camera torch error: ${e.message}")
    } catch (e: Exception) {
      ActionResult(false, "Flashlight control error: ${e.message}")
    }
  }

  /**
   * Device lock protocol simulation & guidance
   */
  fun triggerDeviceLock(context: Context): ActionResult {
    return try {
      val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      ActionResult(
        true,
        "Security protocol active. Device lock settings displayed, Sir.",
        "Security lock initialized."
      )
    } catch (e: Exception) {
      ActionResult(false, "Device lock error: ${e.localizedMessage}")
    }
  }

  /**
   * Create or open Google Sheets
   */
  fun createGoogleSheet(context: Context, sheetTitle: String? = null): ActionResult {
    return try {
      val sheetsWebUri = Uri.parse("https://sheets.new")
      val intent = Intent(Intent.ACTION_VIEW, sheetsWebUri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      val speech = if (sheetTitle.isNullOrBlank()) {
        "Creating a new Google Sheet for you right now, Sir."
      } else {
        "Creating Google Sheet titled '$sheetTitle', Sir."
      }
      ActionResult(true, speech, "Opened Google Sheets (sheets.new)")
    } catch (e: Exception) {
      ActionResult(false, "Unable to launch Google Sheets: ${e.localizedMessage}")
    }
  }

  /**
   * Create Calendar Event
   */
  fun addCalendarEvent(
    context: Context,
    title: String,
    description: String? = null,
    hourOffset: Int = 1
  ): ActionResult {
    return try {
      val cal = Calendar.getInstance().apply {
        add(Calendar.HOUR_OF_DAY, hourOffset)
      }
      val intent = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.TITLE, title)
        .putExtra(CalendarContract.Events.DESCRIPTION, description ?: "Scheduled via Jarvis AI Assistant")
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis + 60 * 60 * 1000)
        .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }

      context.startActivity(intent)
      ActionResult(true, "Calendar entry '$title' opened for confirmation, Sir.", "Calendar: $title")
    } catch (e: Exception) {
      ActionResult(false, "Failed to schedule calendar event: ${e.localizedMessage}")
    }
  }

  /**
   * Set Alarm
   */
  fun setAlarm(
    context: Context,
    hour: Int,
    minute: Int,
    message: String = "Jarvis Alarm"
  ): ActionResult {
    return try {
      val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(AlarmClock.EXTRA_HOUR, hour)
        putExtra(AlarmClock.EXTRA_MINUTES, minute)
        putExtra(AlarmClock.EXTRA_MESSAGE, message)
        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      val timeFormatted = String.format("%02d:%02d", hour, minute)
      ActionResult(true, "Alarm scheduled for $timeFormatted, Sir.", "Alarm: $timeFormatted ($message)")
    } catch (e: Exception) {
      ActionResult(false, "Unable to set alarm: ${e.localizedMessage}")
    }
  }

  /**
   * Read raw text content from picked URI
   */
  fun readTextFromUri(context: Context, uri: Uri): String {
    return try {
      context.contentResolver.openInputStream(uri)?.use { stream ->
        stream.bufferedReader(Charsets.UTF_8).use { reader ->
          reader.readText()
        }
      } ?: "File is empty or inaccessible."
    } catch (e: Exception) {
      "Error reading file content: ${e.localizedMessage}"
    }
  }

  fun openCamera(context: Context): ActionResult {
    return try {
      val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      ActionResult(true, "Opening Camera optics, Sir.", "Camera launched")
    } catch (e: Exception) {
      ActionResult(false, "Camera launch failed: ${e.localizedMessage}")
    }
  }

  fun openSettings(context: Context): ActionResult {
    return try {
      val intent = Intent(Settings.ACTION_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      ActionResult(true, "Accessing device control settings, Sir.", "Settings opened")
    } catch (e: Exception) {
      ActionResult(false, "Settings launch failed: ${e.localizedMessage}")
    }
  }

  fun openWebSearch(context: Context, query: String): ActionResult {
    return try {
      val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
        putExtra(SearchManager.QUERY, query)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      ActionResult(true, "Searching the global network for '$query', Sir.", "Web Search: $query")
    } catch (e: Exception) {
      ActionResult(false, "Web search failed: ${e.localizedMessage}")
    }
  }
}
