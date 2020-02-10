package com.parabola.player_feature;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class PlayerService extends Service {


    public static final String ACTION_TOGGLE_PLAYING = "com.parabola.newtone.TOGGLE_PLAYING";
    public static final String ACTION_NEXT = "com.parabola.newtone.NEXT";
    public static final String ACTION_PREVIOUS = "com.parabola.newtone.PREVIOUS";

    public static final String ACTION_TOGGLE_REPEAT_MODE = "com.parabola.newtone.TOGGLE_REPEAT_MODE";
    public static final String ACTION_TOGGLE_SHUFFLE_MODE = "com.parabola.newtone.TOGGLE_SHUFFLE_MODE";


    static PlayerInteractorImpl playerInteractor;
    static boolean isRunning = false;


    @Override
    public void onCreate() {
        isRunning = true;
        super.onCreate();
        playerInteractor.setNewtonePlayerListener(new PlayerInteractorImpl.NewtonePlayerListener() {
            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                if (ongoing) {
                    startForeground(notificationId, notification);
                } else {
                    stopForeground(false);
                }
            }

            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                stopForeground(true);
                stopSelf();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null)
            handleAction(intent.getAction());

        return START_STICKY;
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
        isRunning = false;
        playerInteractor.setNewtonePlayerListener(null);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
