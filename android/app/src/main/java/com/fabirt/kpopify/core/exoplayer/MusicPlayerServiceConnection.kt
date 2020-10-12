package com.fabirt.kpopify.core.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fabirt.kpopify.core.constants.K
import com.fabirt.kpopify.core.services.MusicPlayerService
import com.fabirt.kpopify.core.util.Event
import com.fabirt.kpopify.core.util.Resource

class MusicPlayerServiceConnection(context: Context) {

    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> get() = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> get() = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> get() = _playbackState

    private val _currentPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val currentPlayingSong: LiveData<MediaMetadataCompat?> get() = _currentPlayingSong

    lateinit var mediaController: MediaControllerCompat

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser =
        MediaBrowserCompat(
            context,
            ComponentName(context, MusicPlayerService::class.java),
            mediaBrowserConnectionCallback,
            null
        ).apply { connect() }

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken)
            _isConnected.postValue(Event(Resource.Success(true)))
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            _isConnected.postValue(Event(Resource.Error("The connection was suspended")))
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            _isConnected.postValue(Event(Resource.Error("Couldn't connect to media browser")))
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            _currentPlayingSong.postValue(metadata)
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when (event) {
                K.NETWORK_ERROR_EVENT -> {
                    _networkError.postValue(Event(Resource.Error("Couldn't connect to the server. Please check your internet connection.")))
                }
            }
        }
    }
}