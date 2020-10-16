package com.fabirt.kpopify.core.exoplayer

import com.fabirt.kpopify.core.service.MusicPlayerService
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player

class MusicPlayerEventListener(
    private val musicPlayerService: MusicPlayerService
) : Player.EventListener {
    /*
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        Stop foreground service
        if (playbackState == Player.STATE_READY && !playWhenReady) {
            musicPlayerService.stopForeground(false)
        }
    }
     */

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        // PLayback state is error
        // Toast.makeText(musicPlayerService, "An unknown error occured", Toast.LENGTH_LONG).show()
    }
}