package com.jimmypiedrahita.accesibilidad

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class OverlayView(context: Context) : View(context) {
    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    var intensity: Int = 50 // Default (50%)
        set(value) {
            field = value.coerceIn(0, 100)
            updatePaintColor()
            invalidate()
        }

    init {
        updatePaintColor()
    }

    private fun updatePaintColor() {
        // Calculates alpha based on intensity (0-255)
        val alpha = (intensity * 2.55).toInt()
        paint.color = Color.argb(alpha, 128, 128, 128)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}