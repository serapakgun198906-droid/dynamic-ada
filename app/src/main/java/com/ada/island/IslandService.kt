package com.ada.island

import android.accessibilityservice.AccessibilityService
import android.animation.ValueAnimator
import android.content.*
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.animation.OvershootInterpolator

class IslandService : AccessibilityService() {
    private lateinit var wm: WindowManager
    private lateinit var island: IslandView
    private lateinit var lp: WindowManager.LayoutParams
    private val h = Handler(Looper.getMainLooper())
    private var d = 1f
    private val collapse = Runnable { island.setExpanded(false); animateTo(CW, CH) }
    private val CW get() = (126 * d).toInt()
    private val CH get() = (36 * d).toInt()
    private var last: Info? = null

    private val rx = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            when (i.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    val bat = getSystemService(BatteryManager::class.java)
                        .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    show(Info(null, "⚡", "Şarj oluyor", "%$bat", 3000))
                }
                Intent.ACTION_POWER_DISCONNECTED -> show(Info(null, "🔌", "Şarj kesildi", "", 2000))
                // Android 3. parti uygulamaya Face ID olayı vermez; kilit açılınca animasyon oynar
                Intent.ACTION_USER_PRESENT -> show(Info(null, "🔓", "Kilit açıldı", "", 1800))
            }
        }
    }

    override fun onServiceConnected() {
        d = resources.displayMetrics.density
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        island = IslandView(this)
        lp = WindowManager.LayoutParams(
            CW, CH, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = (10 * d).toInt() }
        island.setRadius(CH)
        island.setOnClickListener { last?.let { show(it) } }
        wm.addView(island, lp)
        IslandBus.show = { i -> h.post { show(i) } }
        registerReceiver(rx, IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED); addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_USER_PRESENT)
        })
    }

    private fun show(i: Info) {
        last = i
        h.removeCallbacks(collapse)
        island.bind(i)
        animateTo((340 * d).toInt(), (80 * d).toInt())
        island.setExpanded(true)
        h.postDelayed(collapse, i.duration)
    }

    private fun animateTo(w: Int, ht: Int) {
        val w0 = lp.width; val h0 = lp.height
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 420; interpolator = OvershootInterpolator(0.8f)
            addUpdateListener {
                val f = it.animatedValue as Float
                lp.width = (w0 + (w - w0) * f).toInt().coerceAtLeast(1)
                lp.height = (h0 + (ht - h0) * f).toInt().coerceAtLeast(1)
                island.setRadius(lp.height)
                runCatching { wm.updateViewLayout(island, lp) }
            }
            start()
        }
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() {
        IslandBus.show = null
        runCatching { unregisterReceiver(rx); wm.removeView(island) }
        super.onDestroy()
    }
}
