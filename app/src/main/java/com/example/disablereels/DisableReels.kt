package com.example.disablereels
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent


class DisableReels : AccessibilityService() {
    private var blockNextEventReason : CharSequence? = null
    private var setBlockedOnNextEvent : Boolean = false

    private fun tryClearBlockNextEventReason()
    {
        // suggested reels and reels button
        if(blockNextEventReason == "Reels"){
            blockNextEventReason = null
        }
    }

    private fun trySetBlockNextEventReason(event: AccessibilityEvent)
    {
        if(blockNextEventReason != null)
            return

        // Reels button
        if(event.contentDescription == "Reels")
            blockNextEventReason = "Reels"

        // chats
        if(event.contentDescription != null)
        {
            val len = event.contentDescription!!.length
            val desc = event.contentDescription!!.subSequence(len - 3, len)
            if(desc == "ago")
                blockNextEventReason = event.contentDescription
        }

        if(setBlockedOnNextEvent)
        {
            blockNextEventReason = "Reels"
            setBlockedOnNextEvent = false
        }

        // suggested reels (allow opening previewed reels but block scrolling)
        if(event.className == "android.widget.FrameLayout" && event.source != null && event.contentDescription == null) {
            setBlockedOnNextEvent = true
        }
    }

    private fun tryBlockEvent(event : AccessibilityEvent)
    {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.className == "android.view.View" &&
            blockNextEventReason != null)
        {
            tryClearBlockNextEventReason()
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if(event == null)
            return

        // event for chat exit
        if (event.className == "com.instagram.mainactivity.InstagramMainActivity")
            blockNextEventReason = null

        trySetBlockNextEventReason(event)
        tryBlockEvent(event)
    }
}