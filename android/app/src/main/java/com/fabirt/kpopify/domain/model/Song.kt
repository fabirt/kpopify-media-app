package com.fabirt.kpopify.domain.model

data class Song(
    val mediaId: String,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val mediaUrl: String
)