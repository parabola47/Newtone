package com.parabola.player_feature;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
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


    static PlayerInteractorImpl playerInteractor;
    static boolean isRunning = false;

    private int notificationId;
    private Notification notification;

    @Override
    public void onCreate() {
        isRunning = true;
        playerInteractor.setNewtonePlayerListener(new PlayerInteractorImpl.NewtonePlayerListener() {

            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                PlayerService.this.notificationId = notificationId;
                PlayerService.this.notification = notification;

                Intent intent = new Intent(PlayerService.this, PlayerService.class)
                        .setAction(ongoing ? ACTION_START_FOREGROUND : ACTION_STOP_FOREGROUND);

                if (ongoing && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startForegroundService(intent);
                else startService(intent);
            }

            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                Intent intent = new Intent(PlayerService.this, PlayerService.class);
                intent.setAction(ACTION_STOP_SERVICE);
                startService(intent);
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
        isRunning = false;
        playerInteractor.setNewtonePlayerListener(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
