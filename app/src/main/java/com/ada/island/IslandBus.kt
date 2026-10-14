package com.ada.island

import android.graphics.drawable.Drawable
import android.media.session.MediaController

data class Info(
    val icon: Drawable?, val emoji: String, val title: String, val sub: String,
    val duration: Long = 3500, val music: Boolean = false
)

object IslandBus {
    var show: ((Info) -> Unit)? = null
    var controller: MediaController? = null
}
