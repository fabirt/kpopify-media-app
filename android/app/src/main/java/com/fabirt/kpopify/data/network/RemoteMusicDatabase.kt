package com.fabirt.kpopify.data.network

import com.fabirt.kpopify.core.constants.K.PLAYLIST_COLLECTION_NAME
import com.fabirt.kpopify.data.network.model.NetworkSong
import com.fabirt.kpopify.domain.model.Song
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteMusicDatabase @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getAllSongs(): List<Song> {
        return firestore.collection(PLAYLIST_COLLECTION_NAME)
            .get()
            .await()
            .documents
            .map { snapshot ->
                NetworkSong.fromMap(snapshot.data!!).asDomainModel()
            }.sortedBy { it.mediaId }
    }
}
