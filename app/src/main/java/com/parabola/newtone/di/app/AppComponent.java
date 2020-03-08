package com.parabola.newtone.di.app;

import android.content.Intent;

import com.parabola.domain.interactor.SleepTimerInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.di.app.modules.AndroidAppModule;
import com.parabola.newtone.di.app.modules.ConfigModule;
import com.parabola.newtone.di.app.modules.DataModule;
import com.parabola.newtone.di.app.modules.IntentModule;
import com.parabola.newtone.di.app.modules.InteractorModule;
import com.parabola.newtone.di.app.modules.NavigationModule;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;

import static com.parabola.newtone.di.app.modules.IntentModule.GO_NEXT_TRACK_INTENT;
import static com.parabola.newtone.di.app.modules.IntentModule.GO_PREVIOUS_TRACK_INTENT;
import static com.parabola.newtone.di.app.modules.IntentModule.OPEN_ACTIVITY_INTENT;
import static com.parabola.newtone.di.app.modules.IntentModule.TOGGLE_PLAYER_STATE_INTENT;
import static com.parabola.newtone.di.app.modules.IntentModule.TOGGLE_REPEAT_MODE_INTENT;
import static com.parabola.newtone.di.app.modules.IntentModule.TOGGLE_SHUFFLE_MODE_INTENT;

@Singleton
@Component(modules = {
        AndroidAppModule.class,
        IntentModule.class,
        InteractorModule.class,
        DataModule.class,
        NavigationModule.class,
        ConfigModule.class}
)
public interface AppComponent extends AppComponentInjects {

    final class Initializer {

        private Initializer() {
        } // No instances.

        public static AppComponent init(MainApplication app) {
            return DaggerAppComponent.builder()
                    .androidAppModule(new AndroidAppModule(app))
                    .build();
        }
    }

    PlayerInteractor providePlayerInteractor();
    SleepTimerInteractor provideSleepTimerInteractor();
    TrackRepository provideTrackRepo();


    @Named(TOGGLE_PLAYER_STATE_INTENT)
    Intent provideTogglePlayerIntent();

    @Named(GO_NEXT_TRACK_INTENT)
    Intent provideGoNextIntent();
    @Named(GO_PREVIOUS_TRACK_INTENT)
    Intent provideGoPreviousIntent();

    @Named(TOGGLE_REPEAT_MODE_INTENT)
    Intent provideToggleRepeatModeIntent();
    @Named(TOGGLE_SHUFFLE_MODE_INTENT)
    Intent provideToggleShuffleModeIntent();

    @Named(OPEN_ACTIVITY_INTENT)
    Intent provideOpenActivityIntent();

}
