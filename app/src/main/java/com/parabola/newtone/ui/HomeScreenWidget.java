package com.parabola.newtone.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor.RepeatMode;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;

public final class HomeScreenWidget extends AppWidgetProvider {

    public static void updateHomeScreenWidget(Context context) {
        Intent intent = new Intent(context, HomeScreenWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, HomeScreenWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }


    @Override
    public void onEnabled(Context context) {
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context, HomeScreenWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);


        AppComponent appComponent = ((MainApplication) context.getApplicationContext()).getAppComponent();
        PlayerInteractor playerInteractor = appComponent.providePlayerInteractor();
        TrackRepository trackRepo = appComponent.provideTrackRepo();


        boolean isNowPlaying = playerInteractor.isPlayWhenReady();
        RepeatMode repeatMode = playerInteractor.getRepeatMode();
        boolean shuffleEnabled = playerInteractor.isShuffleEnabled();


        Track currentTrack = trackRepo.getById(playerInteractor.currentTrackId())
                .onErrorReturnItem(EmptyItems.NO_TRACK)
                .blockingGet();


        String title, artist, album;
        title = currentTrack.getTitle();
        artist = currentTrack.getArtistName();
        album = currentTrack.getAlbumTitle();

        Bitmap albumArt = currentTrack.getArtImage();

        for (int widgetId : allWidgetIds) {
            setupWidget(context, appComponent, appWidgetManager, widgetId, title, artist, album,
                    albumArt, isNowPlaying, repeatMode, shuffleEnabled);
        }
    }


    private static final String SET_COLOR_FILTER_METHOD_NAME = "setColorFilter";

    private void setupWidget(Context context, AppComponent appComponent, AppWidgetManager appWidgetManager, int widgetId,
                             String trackTitle, String artistName, String albumTitle,
                             @Nullable Bitmap albumCover,
                             boolean isNowPlaying, RepeatMode repeatMode, boolean isShuffleEnabled) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.home_screen_widget);

        remoteViews.setTextViewText(R.id.title, trackTitle);
        remoteViews.setTextViewText(R.id.artist, artistName);
        remoteViews.setTextViewText(R.id.album, albumTitle);


        PendingIntent playNextTrackIntent = getPendingIntent(context, 0, appComponent.provideGoNextIntent(), 0);
        remoteViews.setOnClickPendingIntent(R.id.next, playNextTrackIntent);


        PendingIntent prevButtonIntent = getPendingIntent(context, 0, appComponent.provideGoPreviousIntent(), 0);
        remoteViews.setOnClickPendingIntent(R.id.prev, prevButtonIntent);

        PendingIntent launchActivityPI = PendingIntent.getActivity(context, 0, appComponent.provideOpenActivityIntent(), 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, launchActivityPI);


        PendingIntent playerButtonIntent = getPendingIntent(context, 0, appComponent.provideTogglePlayerIntent(), 0);
        remoteViews.setImageViewResource(R.id.play, isNowPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
        remoteViews.setOnClickPendingIntent(R.id.play, playerButtonIntent);


        switch (repeatMode) {
            case OFF:
                remoteViews.setInt(R.id.repeat, SET_COLOR_FILTER_METHOD_NAME, context.getResources().getColor(android.R.color.white));
                remoteViews.setImageViewResource(R.id.repeat, R.drawable.ic_loop);
                break;
            case ONE:
                remoteViews.setInt(R.id.repeat, SET_COLOR_FILTER_METHOD_NAME, context.getResources().getColor(R.color.colorAccent));
                remoteViews.setImageViewResource(R.id.repeat, R.drawable.ic_loop_one);
                break;
            case ALL:
                remoteViews.setInt(R.id.repeat, SET_COLOR_FILTER_METHOD_NAME, context.getResources().getColor(R.color.colorAccent));
                remoteViews.setImageViewResource(R.id.repeat, R.drawable.ic_loop);
                break;
        }
        remoteViews.setOnClickPendingIntent(R.id.repeat, getPendingIntent(context, 0, appComponent.provideToggleRepeatModeIntent(), 0));


        int color = context.getResources().getColor(isShuffleEnabled ? R.color.colorAccent : android.R.color.white);
        remoteViews.setInt(R.id.shuffle, SET_COLOR_FILTER_METHOD_NAME, color);
        remoteViews.setOnClickPendingIntent(R.id.shuffle, getPendingIntent(context, 0, appComponent.provideToggleShuffleModeIntent(), 0));

        if (albumCover != null) remoteViews.setImageViewBitmap(R.id.cover, albumCover);
        else remoteViews.setImageViewResource(R.id.cover, R.drawable.album_default);


        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }


    private static PendingIntent getPendingIntent(Context context, int requestCode, Intent intent, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(context, requestCode, intent, flags);
        } else {
            return PendingIntent.getService(context, requestCode, intent, flags);
        }
    }


    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }
}
