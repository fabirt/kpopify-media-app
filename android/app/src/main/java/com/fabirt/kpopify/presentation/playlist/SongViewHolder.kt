package com.fabirt.kpopify.presentation.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fabirt.kpopify.databinding.ViewPlaylistSongBinding
import com.fabirt.kpopify.domain.model.Song

class SongViewHolder(
    private val binding: ViewPlaylistSongBinding
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): SongViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ViewPlaylistSongBinding.inflate(inflater, parent, false)
            return SongViewHolder(binding)
        }
    }

    fun bind(song: Song, dispatcher: PlaylistEventDispatcher) {
        binding.tvTitle.text = song.title
        binding.tvArtist.text = song.artist
        binding.container.setOnClickListener {
            dispatcher.onSongSelected(song)
        }
    }
}