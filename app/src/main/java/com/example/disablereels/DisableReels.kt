package com.example.disablereels
import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent


class DisableReels : AccessibilityService() {
    private var blockNextEventReason : CharSequence? = null
    private var canBlockEvent : Boolean = true

    private fun trySetBlockNextEventReason(event: AccessibilityEvent)
    {
        // Reels button
        if(event.contentDescription == "Reels")
            blockNextEventReason = "ReelsButton"

        // View post in chat
        if(event.className == "androidx.viewpager.widget.ViewPager" && event.itemCount == -1)
            blockNextEventReason = null

        // View a reel
        else if (event.className == "androidx.viewpager.widget.ViewPager")
            blockNextEventReason = "Reels"

        // Exit chat
        else if (event.className == "com.instagram.mainactivity.InstagramMainActivity")
            blockNextEventReason = null

        // Back event and scrolling feed
        else if(event.className == "androidx.recyclerview.widget.RecyclerView")
            blockNextEventReason = null

        // View reel in chat
        else if(event.className == "com.instagram.modal.ModalActivity")
            blockNextEventReason = null
    }

    private fun blockEvent()
    {
        blockNextEventReason = null

        // Prevent multiple consecutive back actions
        if(canBlockEvent) {
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            canBlockEvent = false

            // Allow back actions again after 200 ms
            Handler(Looper.getMainLooper()).postDelayed({ canBlockEvent = true }, 500)
        }
    }

    private fun tryBlockEvent(event : AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.className == "android.view.View" &&
            blockNextEventReason == "ReelsButton")
            blockEvent()

        else if (event.className == "androidx.viewpager.widget.ViewPager" &&
            blockNextEventReason != null)
            blockEvent()
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if(event == null)
            return

        tryBlockEvent(event)
        trySetBlockNextEventReason(event)
    }
}