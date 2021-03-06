package com.fabirt.kpopify.core.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.fabirt.kpopify.core.constant.K
import com.fabirt.kpopify.core.exoplayer.*
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MusicPlayerService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: CacheDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var musicSource: MusicSource

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var musicPlayerNotificationManager: MusicPlayerNotificationManager

    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    private var currentPlayingSong: MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    var isForegroundService: Boolean = false

    companion object {
        private const val TAG = "MusicPlayerService"

        var currentSongDuration: Long = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()

        resquestMediaData()

        val activityPendingIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        mediaSession = MediaSessionCompat(this, TAG).apply {
            setSessionActivity(activityPendingIntent)
            isActive = true
        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(musicSource) { mediaMetadata ->
            currentPlayingSong = mediaMetadata
            preparePlayer(musicSource.mediaMetadataSongs, mediaMetadata, true)
        }
        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlaybackPreparer(musicPlaybackPreparer)
            setQueueNavigator(MusicPlayerQueueNavigator(mediaSession, musicSource))
            setPlayer(exoPlayer)
        }

        this.sessionToken = mediaSession.sessionToken

        musicPlayerNotificationManager = MusicPlayerNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {
            currentSongDuration = exoPlayer.duration
        }

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
    }


    override fun onCustomAction(action: String, extras: Bundle?, result: Result<Bundle>) {
        super.onCustomAction(action, extras, result)
        when (action) {
            K.START_MEDIA_PLAYBACK_ACTION -> {
                musicPlayerNotificationManager.showNotification(exoPlayer)
            }
            K.REFRESH_MEDIA_BROWSER_CHILDREN -> {
                musicSource.refresh()
                resquestMediaData()
                notifyChildrenChanged(K.MEDIA_ROOT_ID)
            }
            else -> Unit
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            K.MEDIA_ROOT_ID -> {
                val resultsSent = musicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(musicSource.asMediaItems())
                        if (!isPlayerInitialized && musicSource.mediaMetadataSongs.isNotEmpty()) {
                            // Uncomment this to prepare the player when children are loaded
                            // val mediaSongs = musicSource.mediaMetadataSongs
                            // preparePlayer(mediaSongs, mediaSongs.first(), false)
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(K.NETWORK_ERROR_EVENT, null)
                        result.sendResult(null)
                    }
                }
                if (!resultsSent) {
                    result.detach()
                }
            }
            else -> Unit
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(K.MEDIA_ROOT_ID, null)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Here you can stop the player when he user remove the application's task
        // exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playWhenReady: Boolean
    ) {
        val indexToPlay = if (currentPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.prepare(musicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(indexToPlay, 0L)
        exoPlayer.playWhenReady = playWhenReady
    }

    private fun resquestMediaData() {
        serviceScope.launch {
            musicSource.requestMediaData()
        }
    }
}
