package com.jimmypiedrahita.accesibilidad

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

class BlueLightFilterService : AccessibilityService() {

    companion object {
        private var instance: BlueLightFilterService? = null

        fun stopFilter() {
            instance?.disableOverlay()
            instance?.disableSelf() //Especial method for accessible services
        }
    }

    private var overlayView: OverlayView? = null
    private var windowManager: WindowManager? = null

    override fun onServiceConnected() {
        instance = this
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupOverlay()
    }

    fun disableOverlay() {
        try {
            overlayView?.let { view ->
                windowManager?.removeView(view)
            }
        } catch (e: IllegalArgumentException) {
            Log.e("overlay",e.message.toString())
            } finally {
            overlayView = null
        }
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

        windowManager?.addView(overlayView, params)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        disableOverlay()
        instance = null
        super.onDestroy()
    }
}