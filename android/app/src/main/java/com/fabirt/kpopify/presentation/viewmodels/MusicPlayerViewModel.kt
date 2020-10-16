package com.fabirt.kpopify.presentation.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.fabirt.kpopify.core.constants.K
import com.fabirt.kpopify.core.exoplayer.MusicPlayerServiceConnection
import com.fabirt.kpopify.core.services.MusicPlayerService
import com.fabirt.kpopify.core.util.*
import com.fabirt.kpopify.domain.model.Song
import kotlinx.coroutines.delay

class MusicPlayerViewModel @ViewModelInject constructor(
    private val serviceConnection: MusicPlayerServiceConnection,
) : ViewModel() {
    companion object {
        private const val TAG = "MusicPlayerViewModel"
    }

    private val _songs = MutableLiveData<Resource<List<Song>>>()
    val songs: LiveData<Resource<List<Song>>> get() = _songs

    val currentSongDuration: LiveData<Long>
        get() = Transformations.map(serviceConnection.playbackState) {
            MusicPlayerService.currentSongDuration
        }

    private val _currentPlaybackPosition = MutableLiveData<Long>()
    val currentPlaybackPosition: LiveData<Long> get() = _currentPlaybackPosition

    val isConnected = serviceConnection.isConnected
    val networkError = serviceConnection.networkError
    val currentPlayingSong = serviceConnection.currentPlayingSong
    val playbackState = serviceConnection.playbackState

    val isInRepeatMode: Boolean
        get() = serviceConnection.mediaController.repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE

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
                    val items = children.map { it.toSong() }
                    _songs.postValue(Resource.Success(items))
                }

                override fun onError(parentId: String) {
                    super.onError(parentId)
                    _songs.postValue(Resource.Error(parentId))
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

    fun toggleRepeatMode() {
        if (isInRepeatMode) {
            serviceConnection.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)
        } else {
            serviceConnection.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)
        }
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

    suspend fun updateCurrentPlaybackPosition() {
        val currentPosition = playbackState.value?.currentPosition
        if (currentPosition != _currentPlaybackPosition.value) {
            _currentPlaybackPosition.postValue(currentPosition)
        }
        delay(K.PLAYBACK_POSITION_UPDATE_TIME)
        updateCurrentPlaybackPosition()
    }

    fun refreshMediaBrowserChildren() {
        _songs.value = Resource.Loading
        serviceConnection.refreshMediaBrowserChildren()
    }

    override fun onCleared() {
        super.onCleared()
        serviceConnection.unsubscribe(
            K.MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}
