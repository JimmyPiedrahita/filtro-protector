package com.jimmypiedrahita.accesibilidad

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

class BlueLightFilterService : AccessibilityService() {

    private lateinit var overlayView: OverlayView
    private lateinit var windowManager: WindowManager

    override fun onServiceConnected() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupOverlay()
    }

    private fun setupOverlay() {
        overlayView = OverlayView(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        if (::overlayView.isInitialized && ::windowManager.isInitialized) {
            windowManager.removeView(overlayView)
        }
        super.onDestroy()
    }
}