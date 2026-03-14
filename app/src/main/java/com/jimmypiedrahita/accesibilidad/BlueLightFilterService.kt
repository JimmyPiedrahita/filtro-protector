package com.jimmypiedrahita.accesibilidad

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color as ComposeColor

class BlueLightFilterService : AccessibilityService() {

    companion object {
        private var instance: BlueLightFilterService? = null
        private var isFilterRunning = false
        private var currentIntensity = 50 //Intensity for default
        private const val MAX_INTENSITY = 90 // Max Intensity 90%
        private var currentColor = AndroidColor.rgb(36, 30, 0) //Color for default

        fun isFilterActive(): Boolean = isFilterRunning
        fun getCurrentIntensity(): Int = currentIntensity
        fun getCurrentColor(): ComposeColor {
            return ComposeColor(currentColor)
        }

        fun toggleFilter(enable: Boolean){
            if (instance == null) {
                if (!enable) isFilterRunning = false
                return
            }

            if (enable){
                instance?.enableFilter()
            } else {
                instance?.disableFilter()
            }
        }

        fun setIntensity(intensity: Int) {
            currentIntensity = intensity.coerceIn(0, MAX_INTENSITY)
            instance?.updateIntensity(currentIntensity)
        }

        fun setColor(color: ComposeColor) {
            currentColor = AndroidColor.rgb(
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt()
            )
            instance?.updateFilter()
        }
    }

    private var overlayView: OverlayView? = null
    private var windowManager: WindowManager? = null

    override fun onServiceConnected() {
        instance = this
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        // If the filter was supposed to be running (e.g. after a crash/restart), restore it
        if (isFilterRunning) {
            enableFilter()
        }
    }

    fun enableFilter() {
        if (overlayView == null) {
            try {
                val newOverlay = OverlayView(this)
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                )
                
                // Initialize with current saved state
                newOverlay.intensity = currentIntensity
                newOverlay.filterColor = currentColor
                
                windowManager?.addView(newOverlay, params)
                overlayView = newOverlay
                isFilterRunning = true
            } catch (e: Exception) {
                Log.e("BlueLightFilterService", "Error enabling filter", e)
                overlayView = null
                isFilterRunning = false
            }
        }
    }

    fun updateFilter() {
        overlayView?.let {
            it.intensity = currentIntensity
            it.filterColor = currentColor
        }
    }

    fun updateIntensity(intensity: Int) {
        overlayView?.intensity = intensity
    }

    fun disableFilter() {
        try {
            overlayView?.let { view ->
                windowManager?.removeView(view)
            }
        } catch (e: Exception) {
            Log.e("Overlay", "View already removed or error removing: ${e.message}")
        } finally {
            overlayView = null
            isFilterRunning = false
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    
    override fun onInterrupt() {
        disableFilter()
    }

    override fun onDestroy() {
        super.onDestroy()
        disableFilter()
        instance = null
    }
}