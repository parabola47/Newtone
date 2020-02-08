package com.parabola.newtone.ui;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.parabola.domain.interactors.player.PlayerInteractor;
import com.parabola.domain.interactors.type.Irrelevant;
import com.parabola.newtone.MainApplication;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class AudioPlayerService extends Service {

    public static final String ACTION_TOGGLE_PLAYING = "com.parabola.newtone.TOGGLE_PLAYING";
    public static final String ACTION_NEXT = "com.parabola.newtone.NEXT";
    public static final String ACTION_PREVIOUS = "com.parabola.newtone.PREVIOUS";

    public static final String ACTION_TOGGLE_REPEAT_MODE = "com.parabola.newtone.TOGGLE_REPEAT_MODE";
    public static final String ACTION_TOGGLE_SHUFFLE_MODE = "com.parabola.newtone.TOGGLE_SHUFFLE_MODE";


    @Inject PlayerInteractor playerInteractor;
    private Disposable observeWidgetUpdates;


    @Override
    public void onCreate() {
        super.onCreate();
        MainApplication.getComponent().inject(this);

        observeWidgetUpdates = Observable.combineLatest(
                MainApplication.getComponent().providePlayerInteractor().onChangeCurrentTrackId(),
                MainApplication.getComponent().providePlayerInteractor().onChangePlayingState(),
                MainApplication.getComponent().providePlayerInteractor().onRepeatModeChange(),
                MainApplication.getComponent().providePlayerInteractor().onShuffleModeChange(), (i, b1, b2, b3) -> Irrelevant.INSTANCE)
                .subscribe(irrelevant -> HomeScreenWidget.updateHomeScreenWidget(AudioPlayerService.this));
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action != null)
            handleAction(action);

        return super.onStartCommand(intent, flags, startId);
    }

    private void handleAction(String action) {
        switch (action) {
            case ACTION_TOGGLE_PLAYING:
                playerInteractor.toggle();
                break;
            case ACTION_NEXT:
                playerInteractor.next();
                break;
            case ACTION_PREVIOUS:
                playerInteractor.previous();
                break;
            case ACTION_TOGGLE_REPEAT_MODE:
                playerInteractor.toggleRepeatMode();
                break;
            case ACTION_TOGGLE_SHUFFLE_MODE:
                playerInteractor.toggleShuffleMode();
                break;
            default: throw new IllegalArgumentException(action);
        }
    }

    @Override
    public void onDestroy() {
        observeWidgetUpdates.dispose();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
