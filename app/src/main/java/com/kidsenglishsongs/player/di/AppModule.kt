package com.kidsenglishsongs.player.di

import android.content.Context
import com.kidsenglishsongs.player.player.controller.PlayerController
import com.kidsenglishsongs.player.util.AudioMetadataReader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun providePlayerController(
        @ApplicationContext context: Context
    ): PlayerController {
        return PlayerController(context)
    }
    
    @Provides
    @Singleton
    fun provideAudioMetadataReader(): AudioMetadataReader {
        return AudioMetadataReader()
    }
}
