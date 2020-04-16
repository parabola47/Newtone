package com.parabola.player_feature;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class PlayerService extends Service {


    public static final String ACTION_TOGGLE_PLAYING = "com.parabola.newtone.TOGGLE_PLAYING";
    public static final String ACTION_NEXT = "com.parabola.newtone.NEXT";
    public static final String ACTION_PREVIOUS = "com.parabola.newtone.PREVIOUS";

    public static final String ACTION_TOGGLE_REPEAT_MODE = "com.parabola.newtone.TOGGLE_REPEAT_MODE";
    public static final String ACTION_TOGGLE_SHUFFLE_MODE = "com.parabola.newtone.TOGGLE_SHUFFLE_MODE";

    private static final String ACTION_START_FOREGROUND = "com.parabola.newtone.START_FOREGROUND";
    private static final String ACTION_STOP_FOREGROUND = "com.parabola.newtone.STOP_FOREGROUND";
    private static final String ACTION_STOP_SERVICE = "com.parabola.newtone.STOP_SERVICE";


    @SuppressLint("StaticFieldLeak")
    static PlayerInteractorImpl playerInteractor;

    private int notificationId;
    private Notification notification;

    @Override
    public void onCreate() {
        playerInteractor.setNewtonePlayerListener(new PlayerInteractorImpl.NewtonePlayerListener() {

            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                PlayerService.this.notificationId = notificationId;
                PlayerService.this.notification = notification;

                Intent intent = new Intent(getApplicationContext(), PlayerService.class)
                        .setAction(ongoing ? ACTION_START_FOREGROUND : ACTION_STOP_FOREGROUND);

                if (ongoing && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    getApplicationContext().startForegroundService(intent);
                else getApplicationContext().startService(intent);
            }

            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                Intent intent = new Intent(getApplicationContext(), PlayerService.class)
                        .setAction(ACTION_STOP_SERVICE);
                getApplicationContext().startService(intent);
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
            case ACTION_START_FOREGROUND:
                startForeground(notificationId, notification);
                break;
            case ACTION_STOP_FOREGROUND:
                stopForeground(false);
                break;
            case ACTION_STOP_SERVICE:
                stopForeground(true);
                stopSelf();
                break;
            default: throw new IllegalArgumentException(action);
        }
    }

    @Override
    public void onDestroy() {
        playerInteractor.setNewtonePlayerListener(null);
    }

    private LocalBinder binder = new LocalBinder();

    private class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
