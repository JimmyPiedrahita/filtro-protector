package com.jimmypiedrahita.accesibilidad

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

class BlueLightFilterService : AccessibilityService() {

    companion object {
        private var instance: BlueLightFilterService? = null
        private var isFilterRunning = false

        fun isFilterActive(): Boolean = isFilterRunning

        fun toggleFilter(enable: Boolean){
            if (enable){
                instance?.enableFilter()
            } else {
                instance?.disableFilter()
            }
        }
    }

    private var overlayView: OverlayView? = null
    private var windowManager: WindowManager? = null

    override fun onServiceConnected() {
        instance = this
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    fun enableFilter() {
        if (overlayView == null) {
            overlayView = OverlayView(this).apply {
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                )
                windowManager?.addView(this, params)
            }
            isFilterRunning = true
        }
    }

    fun disableFilter() {
        try {
            overlayView?.let { view ->
                windowManager?.removeView(view)
            }
        } catch (e: IllegalArgumentException) {
            Log.e("Overlay", "View already removed: ${e.message}")
        } finally {
            overlayView = null
            isFilterRunning = false
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        disableFilter()
        instance = null
        super.onDestroy()
    }
}