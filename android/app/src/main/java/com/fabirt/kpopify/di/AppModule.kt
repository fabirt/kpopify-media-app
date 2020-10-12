package com.fabirt.kpopify.di

import android.content.Context
import com.fabirt.kpopify.core.exoplayer.MusicPlayerServiceConnection
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideMusicPlayerServiceConnection(
        @ApplicationContext context: Context
    ): MusicPlayerServiceConnection = MusicPlayerServiceConnection(context)
}