package com.example.jarvis

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import org.json.JSONObject

object PhoneActions {

    /** Runs the action Gemini chose and returns a short status line to speak. */
    fun run(context: Context, action: String, args: JSONObject): String = try {
        when (action) {
            "flashlight_on" -> setFlashlight(context, true)
            "flashlight_off" -> setFlashlight(context, false)
            "open_app" -> openApp(context, args.optString("name"))
            "dial" -> {
                start(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:${args.optString("number")}")))
                "Opening the dialer."
            }
            "set_alarm" -> {
                setAlarm(context, args.optInt("hour", 7), args.optInt("minute", 0), args.optString("label", "JARVIS"))
                "Alarm set."
            }
            "web_search" -> {
                start(context, Intent(Intent.ACTION_WEB_SEARCH).putExtra("query", args.optString("query")))
                "Searching."
            }
            "open_settings" -> {
                start(context, Intent(Settings.ACTION_SETTINGS))
                "Opening settings."
            }
            else -> ""
        }
    } catch (e: Exception) {
        "That action failed: ${e.message}"
    }

    private fun setFlashlight(context: Context, on: Boolean): String {
        val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val id = cm.cameraIdList.firstOrNull {
            cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return "This phone has no flashlight."
        cm.setTorchMode(id, on)
        return if (on) "Flashlight on." else "Flashlight off."
    }

    private fun openApp(context: Context, name: String): String {
        val pm = context.packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(launcher, 0)
        val match = apps.firstOrNull { it.loadLabel(pm).toString().equals(name, ignoreCase = true) }
            ?: apps.firstOrNull { it.loadLabel(pm).toString().contains(name, ignoreCase = true) }
            ?: return "I could not find an app called $name."

        val intent = pm.getLaunchIntentForPackage(match.activityInfo.packageName)
            ?: return "${match.loadLabel(pm)} cannot be opened."
        start(context, intent)
        return "Opening ${match.loadLabel(pm)}."
    }

    private fun setAlarm(context: Context, hour: Int, minute: Int, label: String) {
        start(context, Intent(AlarmClock.ACTION_SET_ALARM)
            .putExtra(AlarmClock.EXTRA_HOUR, hour)
            .putExtra(AlarmClock.EXTRA_MINUTES, minute)
            .putExtra(AlarmClock.EXTRA_MESSAGE, label)
            .putExtra(AlarmClock.EXTRA_SKIP_UI, true))
    }

    private fun start(context: Context, intent: Intent) {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
