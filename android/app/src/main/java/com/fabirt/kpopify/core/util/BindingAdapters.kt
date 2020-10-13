package com.fabirt.kpopify.core.util

import android.widget.ImageView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fabirt.kpopify.R

fun bindNetworkImage(imgView: ImageView, imageUrl: String?) {
    imageUrl?.let {
        val imgUri = it.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder((R.drawable.playlist_image))
                    .error(R.drawable.playlist_image)
            )
            .into(imgView)
    }
}