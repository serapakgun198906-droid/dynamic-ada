package com.ada.island

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotifService : NotificationListenerService() {
    private var mc: MediaController? = null
    private val cb = object : MediaController.Callback() {
        override fun onMetadataChanged(m: MediaMetadata?) = pushMusic()
        override fun onPlaybackStateChanged(s: PlaybackState?) {
            if (s?.state == PlaybackState.STATE_PLAYING) pushMusic()
        }
    }

    override fun onListenerConnected() {
        val msm = getSystemService(MediaSessionManager::class.java)
        val cn = ComponentName(this, NotifService::class.java)
        msm.addOnActiveSessionsChangedListener({ attach(it) }, cn)
        attach(msm.getActiveSessions(cn))
    }

    private fun attach(list: List<MediaController>?) {
        mc?.unregisterCallback(cb)
        mc = list?.firstOrNull()
        mc?.registerCallback(cb)
        IslandBus.controller = mc
    }

    private fun pushMusic() {
        val m = mc?.metadata ?: return
        val art: Bitmap? = m.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: m.getBitmap(MediaMetadata.METADATA_KEY_ART)
        IslandBus.show?.invoke(Info(
            art?.let { BitmapDrawable(resources, it) }, "🎵",
            m.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Müzik",
            m.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "", 5000, true
        ))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val n = sbn.notification
        if (sbn.packageName == packageName || sbn.isOngoing ||
            n.category == android.app.Notification.CATEGORY_TRANSPORT) return
        val e = n.extras
        val t = e.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: return
        val x = e.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
        val ic = runCatching { packageManager.getApplicationIcon(sbn.packageName) }.getOrNull()
        IslandBus.show?.invoke(Info(ic, "🔔", t, x, 4000))
    }
}
