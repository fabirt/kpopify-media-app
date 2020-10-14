package com.fabirt.kpopify.presentation.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fabirt.kpopify.core.constants.K
import com.fabirt.kpopify.core.exoplayer.MusicPlayerServiceConnection
import com.fabirt.kpopify.core.exoplayer.isPlayEnabled
import com.fabirt.kpopify.core.exoplayer.isPlaying
import com.fabirt.kpopify.core.exoplayer.isPrepared
import com.fabirt.kpopify.core.util.Resource
import com.fabirt.kpopify.domain.model.Song

class MusicPlayerViewModel @ViewModelInject constructor(
    private val serviceConnection: MusicPlayerServiceConnection
) : ViewModel() {

    private val _songs = MutableLiveData<Resource<List<Song>>>()
    val songs: LiveData<Resource<List<Song>>> get() = _songs

    val isConnected = serviceConnection.isConnected
    val networkError = serviceConnection.networkError
    val currentPlayingSong = serviceConnection.currentPlayingSong
    val playbackState = serviceConnection.playbackState

    init {
        _songs.postValue(Resource.Loading)
        serviceConnection.subscribe(
            K.MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val items = children.map {
                        Song(
                            it.mediaId!!,
                            it.description.title.toString(),
                            it.description.subtitle.toString(),
                            it.description.iconUri.toString(),
                            it.description.mediaUri.toString()
                        )
                    }
                    _songs.postValue(Resource.Success(items))
                }
            })
    }

    fun skipToNextSong() {
        serviceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        serviceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        serviceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(song: Song, toggle: Boolean = false) {
        serviceConnection.startPlaybackNotification()
        val isPrepared = playbackState.value?.isPrepared ?: false
        val currentMediaId =
            currentPlayingSong.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
        if (isPrepared && song.mediaId == currentMediaId) {
            playbackState.value?.let { state ->
                when {
                    state.isPlaying -> if (toggle) serviceConnection.transportControls.pause()
                    state.isPlayEnabled -> serviceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            serviceConnection.transportControls.playFromMediaId(song.mediaId, null)
        }
    }

    fun playOrPauseCurrentSong() {
        playbackState.value?.let { state ->
            when {
                state.isPlaying -> serviceConnection.transportControls.pause()
                state.isPlayEnabled -> serviceConnection.transportControls.play()
                else -> Unit
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        serviceConnection.unsubscribe(
            K.MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}
