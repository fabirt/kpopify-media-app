package com.fabirt.kpopify.presentation.playlist

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fabirt.kpopify.R
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

    fun bind(song: Song, dispatcher: PlaylistEventDispatcher, highlightTitle: Boolean = false) {
        binding.tvTitle.text = song.title
        binding.tvArtist.text = song.artist
        binding.container.setOnClickListener {
            dispatcher.onSongSelected(song)
        }

        val color = if (highlightTitle)
            binding.root.context.getColor(R.color.colorAccent)
        else Color.WHITE

        binding.tvTitle.setTextColor(color)
    }
}
