package com.fabirt.kpopify.data.network.model

import com.fabirt.kpopify.domain.model.Song

data class NetworkSong(
    val mediaId: String,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val mediaUrl: String
) {
    companion object {
        fun fromMap(data: Map<String, Any?>): NetworkSong {
            return NetworkSong(
                mediaId = data["media_id"] as String,
                title = data["title"] as String,
                artist = data["artist"] as String,
                imageUrl = data["image_url"] as String,
                mediaUrl = data["media_url"] as String
            )
        }
    }

    fun asDomainModel(): Song {
        return Song(mediaId, title, artist, imageUrl, mediaUrl)
    }
}