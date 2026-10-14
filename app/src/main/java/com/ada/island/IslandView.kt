package com.ada.island

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.*

class IslandView(c: Context) : LinearLayout(c) {
    private val d = c.resources.displayMetrics.density
    private val bg = GradientDrawable().apply { setColor(Color.BLACK) }
    private val icon = ImageView(c).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP; clipToOutline = true
        outlineProvider = ViewOutlineProvider.BACKGROUND
        background = GradientDrawable().apply { cornerRadius = 10 * d; setColor(0xFF222222.toInt()) }
    }
    private val emoji = TextView(c).apply { textSize = 24f; gravity = Gravity.CENTER }
    private val title = TextView(c).apply {
        setTextColor(Color.WHITE); textSize = 14f; typeface = Typeface.DEFAULT_BOLD
        setSingleLine(); ellipsize = TextUtils.TruncateAt.END
    }
    private val sub = TextView(c).apply {
        setTextColor(0xFFAAAAAA.toInt()); textSize = 12f; setSingleLine(); ellipsize = TextUtils.TruncateAt.END
    }
    private val controls = LinearLayout(c)
    private val playBtn = ctl("⏯") { IslandBus.controller?.let { mc ->
        if (mc.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING)
            mc.transportControls.pause() else mc.transportControls.play() } }
    private val content = LinearLayout(c).apply { gravity = Gravity.CENTER_VERTICAL }

    init {
        background = bg; gravity = Gravity.CENTER
        val s = (44 * d).toInt()
        val iconBox = FrameLayout(c).apply { addView(icon, s, s); addView(emoji, s, s) }
        val col = LinearLayout(c).apply { orientation = VERTICAL; addView(title); addView(sub) }
        controls.addView(ctl("⏮") { IslandBus.controller?.transportControls?.skipToPrevious() })
        controls.addView(playBtn)
        controls.addView(ctl("⏭") { IslandBus.controller?.transportControls?.skipToNext() })
        content.addView(iconBox, s, s)
        content.addView(col, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = (10 * d).toInt() })
        content.addView(controls)
        content.setPadding((14 * d).toInt(), 0, (10 * d).toInt(), 0)
        addView(content, LayoutParams(-1, -1))
        content.visibility = View.GONE
    }

    private fun ctl(t: String, f: () -> Unit) = TextView(context).apply {
        text = t; textSize = 20f; setTextColor(Color.WHITE); setPadding((8 * d).toInt(), 0, (8 * d).toInt(), 0)
        setOnClickListener { f() }
    }

    fun setRadius(h: Int) { bg.cornerRadius = minOf(h / 2f, 32 * d) }

    fun bind(i: Info) {
        icon.visibility = if (i.icon != null) View.VISIBLE else View.GONE
        emoji.visibility = if (i.icon == null) View.VISIBLE else View.GONE
        icon.setImageDrawable(i.icon); emoji.text = i.emoji
        title.text = i.title; sub.text = i.sub
        controls.visibility = if (i.music) View.VISIBLE else View.GONE
    }

    fun setExpanded(e: Boolean) {
        content.visibility = if (e) View.VISIBLE else View.GONE
        content.alpha = 0f
        if (e) content.animate().alpha(1f).setStartDelay(120).setDuration(220).start()
    }
}
