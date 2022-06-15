package com.parabola.newtone.di.app.modules;

import android.content.Context;
import android.content.Intent;

import com.parabola.newtone.ui.activity.MainActivity;
import com.parabola.player_feature.PlayerService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class IntentModule {

    public static final String OPEN_ACTIVITY_INTENT = "OPEN_ACTIVITY_INTENT";

    public static final String PAUSE_PLAYER_INTENT = "PAUSE_PLAYER_INTENT";
    public static final String RESUME_PLAYER_INTENT = "RESUME_PLAYER_INTENT";
    public static final String TOGGLE_PLAYER_STATE_INTENT = "TOGGLE_PLAYER_STATE_INTENT";

    public static final String GO_NEXT_TRACK_INTENT = "GO_NEXT_TRACK_INTENT";
    public static final String GO_PREVIOUS_TRACK_INTENT = "GO_PREVIOUS_TRACK_INTENT";

    public static final String TOGGLE_REPEAT_MODE_INTENT = "TOGGLE_REPEAT_MODE_INTENT";
    public static final String TOGGLE_SHUFFLE_MODE_INTENT = "TOGGLE_SHUFFLE_MODE_INTENT";


    @Singleton
    @Provides
    @Named(OPEN_ACTIVITY_INTENT)
    Intent provideOpenActivityIntent(Context newtoneApp) {
        return new Intent(newtoneApp, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Singleton
    @Provides
    @Named(TOGGLE_PLAYER_STATE_INTENT)
    Intent provideTogglePlayerStateIntent(Context newtoneApp) {
        return new Intent(newtoneApp, PlayerService.class)
                .setAction(PlayerService.ACTION_TOGGLE_PLAYING);
    }

    @Singleton
    @Provides
    @Named(GO_NEXT_TRACK_INTENT)
    Intent provideGoNextIntent(Context newtoneApp) {
        return new Intent(newtoneApp, PlayerService.class)
                .setAction(PlayerService.ACTION_NEXT);
    }

    @Singleton
    @Provides
    @Named(GO_PREVIOUS_TRACK_INTENT)
    Intent provideGoPreviousIntent(Context newtoneApp) {
        return new Intent(newtoneApp, PlayerService.class)
                .setAction(PlayerService.ACTION_PREVIOUS);
    }

    @Singleton
    @Provides
    @Named(TOGGLE_REPEAT_MODE_INTENT)
    Intent provideToggleRepeatModeIntent(Context newtoneApp) {
        return new Intent(newtoneApp, PlayerService.class)
                .setAction(PlayerService.ACTION_TOGGLE_REPEAT_MODE);
    }

    @Singleton
    @Provides
    @Named(TOGGLE_SHUFFLE_MODE_INTENT)
    Intent provideToggleShuffleModeIntent(Context newtoneApp) {
        return new Intent(newtoneApp, PlayerService.class)
                .setAction(PlayerService.ACTION_TOGGLE_SHUFFLE_MODE);
    }

}
