package com.fabirt.kpopify.presentation.playlist

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.fabirt.kpopify.domain.model.Song

class SongAdapter(
    private val dispatcher: PlaylistEventDispatcher
) : ListAdapter<Song, SongViewHolder>(SongDiffCallback) {
    private var currentMediaId: String? = null

    fun setCurrentMediaId(mediaId: String?) {
        currentMediaId = mediaId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song, dispatcher, highlightTitle = song.mediaId == currentMediaId)
    }

    object SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}