package com.tonix.wifireader

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.*

class RadarView(c: Context, a: AttributeSet? = null) : View(c, a) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var sweep = 0f
    private var pulse = 0f
    /** angle (deg), distance (0..1) */
    var blips: List<Pair<Float, Float>> = emptyList()

    private val anim = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 3000; repeatCount = ValueAnimator.INFINITE; interpolator = LinearInterpolator()
        addUpdateListener { pulse = it.animatedValue as Float; sweep = pulse * 360f; invalidate() }
    }
    override fun onAttachedToWindow() { super.onAttachedToWindow(); anim.start() }
    override fun onDetachedFromWindow() { anim.cancel(); super.onDetachedFromWindow() }

    override fun onDraw(cv: Canvas) {
        val cx = width / 2f; val cy = height / 2f; val r = min(cx, cy) * 0.95f
        p.style = Paint.Style.FILL
        p.shader = RadialGradient(cx, cy, r, 0x3300E5FF, 0x00000000, Shader.TileMode.CLAMP)
        cv.drawCircle(cx, cy, r, p); p.shader = null
        p.style = Paint.Style.STROKE; p.strokeWidth = 2f; p.color = 0x5500E5FF
        for (i in 1..4) cv.drawCircle(cx, cy, r * i / 4, p)
        cv.drawLine(cx - r, cy, cx + r, cy, p); cv.drawLine(cx, cy - r, cx, cy + r, p)
        p.color = 0xFF00E5FF.toInt(); p.alpha = ((1 - pulse) * 180).toInt(); p.strokeWidth = 4f
        cv.drawCircle(cx, cy, r * pulse, p); p.alpha = 255
        p.style = Paint.Style.FILL
        p.shader = SweepGradient(cx, cy, intArrayOf(0x0000E5FF, 0xAA00E5FF.toInt()), floatArrayOf(0.8f, 1f))
        cv.save(); cv.rotate(sweep, cx, cy); cv.drawCircle(cx, cy, r, p); cv.restore(); p.shader = null
        for ((ang, d) in blips) {
            val x = cx + cos(Math.toRadians(ang.toDouble())).toFloat() * d * r
            val y = cy + sin(Math.toRadians(ang.toDouble())).toFloat() * d * r
            val diff = ((sweep - ang) % 360 + 360) % 360
            val glow = (1f - diff / 360f).pow(3)
            p.color = 0xFF00FFC6.toInt(); p.alpha = (80 + 175 * glow).toInt()
            cv.drawCircle(x, y, 6f + 8f * glow, p)
        }
        p.alpha = 255; p.color = 0xFF00E5FF.toInt(); cv.drawCircle(cx, cy, 6f, p)
    }
}
