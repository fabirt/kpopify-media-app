package com.fabirt.kpopify.presentation.playlist

import com.fabirt.kpopify.domain.model.Song

interface PlaylistEventDispatcher {
    fun onSongSelected(song: Song)
}