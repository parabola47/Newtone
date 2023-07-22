package com.parabola.newtone.di.app.modules

import android.content.Context
import android.content.Intent
import com.parabola.newtone.presentation.mainactivity.MainActivity
import com.parabola.player_feature.PlayerService
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class IntentModule {

    @Singleton
    @Provides
    @Named(OPEN_ACTIVITY_INTENT)
    fun provideOpenActivityIntent(newtoneApp: Context): Intent =
        Intent(newtoneApp, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    @Singleton
    @Provides
    @Named(TOGGLE_PLAYER_STATE_INTENT)
    fun provideTogglePlayerStateIntent(newtoneApp: Context): Intent =
        Intent(newtoneApp, PlayerService::class.java)
            .setAction(PlayerService.ACTION_TOGGLE_PLAYING)

    @Singleton
    @Provides
    @Named(GO_NEXT_TRACK_INTENT)
    fun provideGoNextIntent(newtoneApp: Context): Intent =
        Intent(newtoneApp, PlayerService::class.java)
            .setAction(PlayerService.ACTION_NEXT)

    @Singleton
    @Provides
    @Named(GO_PREVIOUS_TRACK_INTENT)
    fun provideGoPreviousIntent(newtoneApp: Context): Intent =
        Intent(newtoneApp, PlayerService::class.java)
            .setAction(PlayerService.ACTION_PREVIOUS)

    @Singleton
    @Provides
    @Named(TOGGLE_REPEAT_MODE_INTENT)
    fun provideToggleRepeatModeIntent(newtoneApp: Context): Intent =
        Intent(newtoneApp, PlayerService::class.java)
            .setAction(PlayerService.ACTION_TOGGLE_REPEAT_MODE)

    @Singleton
    @Provides
    @Named(TOGGLE_SHUFFLE_MODE_INTENT)
    fun provideToggleShuffleModeIntent(newtoneApp: Context): Intent =
        Intent(newtoneApp, PlayerService::class.java)
            .setAction(PlayerService.ACTION_TOGGLE_SHUFFLE_MODE)

    companion object {
        const val OPEN_ACTIVITY_INTENT = "OPEN_ACTIVITY_INTENT"

        const val TOGGLE_PLAYER_STATE_INTENT = "TOGGLE_PLAYER_STATE_INTENT"

        const val GO_NEXT_TRACK_INTENT = "GO_NEXT_TRACK_INTENT"
        const val GO_PREVIOUS_TRACK_INTENT = "GO_PREVIOUS_TRACK_INTENT"

        const val TOGGLE_REPEAT_MODE_INTENT = "TOGGLE_REPEAT_MODE_INTENT"
        const val TOGGLE_SHUFFLE_MODE_INTENT = "TOGGLE_SHUFFLE_MODE_INTENT"
    }
}