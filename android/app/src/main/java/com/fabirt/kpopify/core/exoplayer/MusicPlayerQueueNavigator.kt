package com.fabirt.kpopify.core.exoplayer

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator

class MusicPlayerQueueNavigator(
    mediaSession: MediaSessionCompat,
    private val musicSource: MusicSource
) : TimelineQueueNavigator(mediaSession) {

    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
        return musicSource.mediaMetadataSongs[windowIndex].description
    }
}