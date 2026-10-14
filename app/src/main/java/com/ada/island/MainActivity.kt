package com.ada.island

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val p = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(p, p * 2, p, p)
            gravity = Gravity.CENTER_HORIZONTAL; setBackgroundColor(Color.BLACK)
        }
        root.addView(TextView(this).apply {
            text = "Ada\n\nİki izni aç:\n1) Erişilebilirlik → Ada (ada + kilit ekranı)\n2) Bildirim erişimi → Ada (müzik + bildirimler)"
            setTextColor(Color.WHITE); textSize = 16f
        })
        fun btn(t: String, a: String) = root.addView(Button(this).apply {
            text = t; setOnClickListener { startActivity(Intent(a)) }
        })
        btn("Erişilebilirlik ayarları", Settings.ACTION_ACCESSIBILITY_SETTINGS)
        btn("Bildirim erişimi ayarları", Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        setContentView(root)
    }
}
