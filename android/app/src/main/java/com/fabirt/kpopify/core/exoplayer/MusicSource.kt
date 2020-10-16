package com.fabirt.kpopify.core.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import com.fabirt.kpopify.data.network.RemoteMusicDatabase
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

typealias OnReadyListener = (Boolean) -> Unit

@Singleton
class MusicSource @Inject constructor(
    private val remoteMusicDatabase: RemoteMusicDatabase
) {
    var mediaMetadataSongs: List<MediaMetadataCompat> = emptyList()

    private val onReadyListeners = mutableListOf<OnReadyListener>()

    private var state: MusicSourceState =
        MusicSourceState.CREATED
        set(value) {
            if (value == MusicSourceState.INITIALIZED || value == MusicSourceState.ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(isReady)
                    }
                }
            } else {
                field = value
            }
        }

    private val isReady: Boolean
        get() = state == MusicSourceState.INITIALIZED

    suspend fun requestMediaData() = withContext(Dispatchers.IO) {
        try {
            state = MusicSourceState.INITIALIZING
            mediaMetadataSongs = remoteMusicDatabase.getAllSongs().map { song ->
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.mediaId)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.mediaUrl)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
                    .build()
            }
            state = if (mediaMetadataSongs.isEmpty()) MusicSourceState.ERROR
            else MusicSourceState.INITIALIZED

        } catch (e: Exception) {
            state = MusicSourceState.ERROR
        }
    }

    fun asMediaSource(dataSourceFactory: DataSource.Factory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        mediaMetadataSongs.forEach { metadata ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(
                    metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri()
                )
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = mediaMetadataSongs.map { metadata ->
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(metadata.description.mediaId)
            .setTitle(metadata.description.title)
            .setSubtitle(metadata.description.subtitle)
            .setIconUri(metadata.description.iconUri)
            .setMediaUri(metadata.description.mediaUri)
            .build()
        MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }.toMutableList()

    fun whenReady(listener: OnReadyListener): Boolean {
        return if (state == MusicSourceState.CREATED || state == MusicSourceState.INITIALIZING) {
            onReadyListeners += listener
            false
        } else {
            listener(isReady)
            true
        }
    }

    fun refresh() {
        onReadyListeners.clear()
        state = MusicSourceState.CREATED
    }
}

enum class MusicSourceState {
    CREATED,
    INITIALIZING,
    INITIALIZED,
    ERROR
}
