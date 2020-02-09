package com.parabola.newtone.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.RemoteViews;

import com.parabola.domain.interactors.player.PlayerInteractor;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;

public class HomeScreenWidget extends AppWidgetProvider {

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


        PlayerInteractor playerInteractor = MainApplication.getComponent().providePlayerInteractor();
        TrackRepository trackRepo = MainApplication.getComponent().provideTrackRepo();


        boolean isNowPlaying = playerInteractor.isPlayWhenReady();
        boolean loopEnabled = playerInteractor.isRepeatModeEnabled();
        boolean shuffleEnabled = playerInteractor.isShuffleEnabled();


        Track currentTrack = trackRepo.getById(playerInteractor.currentTrackId())
                .onErrorReturnItem(EmptyItems.NO_TRACK)
                .blockingGet();


        String title, artist, album;
        title = currentTrack.getTitle();
        artist = currentTrack.getArtistName();
        album = currentTrack.getAlbumTitle();

        Bitmap albumArt = currentTrack.getArtImage();
        if (albumArt == null) {
            albumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.album_holder);
        }
        for (int widgetId : allWidgetIds) {
            setupWidget(context, appWidgetManager, widgetId, title, artist, album,
                    albumArt, isNowPlaying, loopEnabled, shuffleEnabled);
        }
    }


    private static final String SET_COLOR_FILTER_METHOD_NAME = "setColorFilter";

    private void setupWidget(Context context, AppWidgetManager appWidgetManager, int widgetId,
                             String trackTitle, String artistName, String albumTitle,
                             Bitmap albumCover,
                             boolean isNowPlaying, boolean isRepeatEnabled, boolean isShuffleEnabled) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.home_screen_widget);
        AppComponent appComponent = MainApplication.getComponent();

        remoteViews.setTextViewText(R.id.title, trackTitle);
        remoteViews.setTextViewText(R.id.artist, artistName);
        remoteViews.setTextViewText(R.id.album, albumTitle);

        remoteViews.setImageViewResource(R.id.cover, R.drawable.album_holder);


        PendingIntent playNextTrackIntent = getPendingIntent(context, 0, appComponent.provideGoNextIntent(), 0);
        remoteViews.setOnClickPendingIntent(R.id.next, playNextTrackIntent);


        PendingIntent prevButtonIntent = getPendingIntent(context, 0, appComponent.provideGoPreviousIntent(), 0);
        remoteViews.setOnClickPendingIntent(R.id.prev, prevButtonIntent);

        PendingIntent launchActivityPI = PendingIntent.getActivity(context, 0, appComponent.provideOpenActivityIntent(), 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, launchActivityPI);


        PendingIntent playerButtonIntent = getPendingIntent(context, 0, appComponent.provideTogglePlayerIntent(), 0);
        remoteViews.setImageViewResource(R.id.play, isNowPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
        remoteViews.setOnClickPendingIntent(R.id.play, playerButtonIntent);


        int color = context.getResources().getColor(isRepeatEnabled ? R.color.colorAccent : android.R.color.white);
        remoteViews.setInt(R.id.repeat, SET_COLOR_FILTER_METHOD_NAME, color);
        remoteViews.setOnClickPendingIntent(R.id.repeat, getPendingIntent(context, 0, appComponent.provideToggleRepeatModeIntent(), 0));


        color = context.getResources().getColor(isShuffleEnabled ? R.color.colorAccent : android.R.color.white);
        remoteViews.setInt(R.id.shuffle, SET_COLOR_FILTER_METHOD_NAME, color);
        remoteViews.setOnClickPendingIntent(R.id.shuffle, getPendingIntent(context, 0, appComponent.provideToggleShuffleModeIntent(), 0));

        remoteViews.setImageViewBitmap(R.id.cover, albumCover);

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
