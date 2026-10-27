package com.tonix.wifireader

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.*
import android.app.Activity
import android.content.pm.PackageManager
import kotlin.math.abs

class MainActivity : Activity() {
    private lateinit var radar: RadarView
    private lateinit var list: LinearLayout
    private lateinit var status: TextView
    private val h = Handler(Looper.getMainLooper())

    private fun tv(t: String, s: Float, c: Int, bold: Boolean = false) = TextView(this).apply {
        text = t; textSize = s; setTextColor(c); typeface = Typeface.MONOSPACE
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#05070D")); setPadding(40, 90, 40, 30)
        }
        root.addView(tv("TONIX", 30f, Color.parseColor("#00E5FF"), true).apply { gravity = Gravity.CENTER; letterSpacing = 0.4f })
        root.addView(tv("WIFI SCANNING ANIMATION", 12f, Color.parseColor("#7FDBFF")).apply { gravity = Gravity.CENTER; letterSpacing = 0.25f })
        radar = RadarView(this)
        root.addView(radar, LinearLayout.LayoutParams(-1, 0, 1.1f))
        status = tv("READY", 12f, Color.parseColor("#00FFC6")).apply { gravity = Gravity.CENTER }
        root.addView(status)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(Button(this).apply { text = "SCAN"; setOnClickListener { scan() } })
        setContentView(root)
        scan()
    }

    private fun scan() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES), 1); return
        }
        status.text = "SCANNING..."
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        @Suppress("DEPRECATION") wm.startScan()
        h.postDelayed({ show(wm) }, 2500)
    }

    override fun onRequestPermissionsResult(r: Int, p: Array<out String>, g: IntArray) { scan() }

    private fun show(wm: WifiManager) {
        val res = try { wm.scanResults } catch (e: SecurityException) { emptyList() }
        status.text = "${res.size} NETWORKS FOUND"
        radar.blips = res.map { Pair((abs(it.BSSID.hashCode()) % 360).toFloat(), (1f - (it.level + 100) / 70f).coerceIn(0.12f, 0.95f)) }
        list.removeAllViews()
        for (r in res.sortedByDescending { it.level }) {
            @Suppress("DEPRECATION") val name = r.SSID.ifEmpty { "<hidden>" }
            val ch = if (r.frequency < 3000) (r.frequency - 2407) / 5 else (r.frequency - 5000) / 5
            list.addView(tv("$name\n   ${r.level} dBm  |  CH $ch  |  ${r.frequency} MHz", 12f, Color.parseColor("#B2EBF2")).apply { setPadding(0, 14, 0, 14) })
        }
    }
}
