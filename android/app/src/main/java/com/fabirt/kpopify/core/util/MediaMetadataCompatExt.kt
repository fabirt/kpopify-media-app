package com.fabirt.kpopify.core.util

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.fabirt.kpopify.domain.model.Song

fun MediaMetadataCompat.toSong(): Song {
    return Song(
        description.mediaId.toString(),
        description.title.toString(),
        description.subtitle.toString(),
        description.iconUri.toString(),
        description.mediaUri.toString()
    )
}

fun MediaBrowserCompat.MediaItem.toSong(): Song {
    return Song(
        mediaId!!,
        description.title.toString(),
        description.subtitle.toString(),
        description.iconUri.toString(),
        description.mediaUri.toString()
    )
}
