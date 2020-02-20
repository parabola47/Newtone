package com.parabola.player_feature;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.parabola.domain.interactors.player.AudioEffectsInteractor;
import com.parabola.domain.interactors.player.PlayerInteractor;
import com.parabola.domain.interactors.player.PlayerSetting;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;


public class PlayerInteractorImpl implements PlayerInteractor {
    private static final String LOG_TAG = PlayerInteractorImpl.class.getSimpleName();

    private final AudioEffectsInteractorImpl audioEffects;
    private final PlayerSetting playerSetting;
    private final PlayerSettingSaver settingSaver;


    //    ExoPlayer
    private final ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource();
    private final DataSource.Factory dataSourceFactory = new FileDataSource.Factory();
    private final SimpleExoPlayer exoPlayer;


    private final Context context;
    private final TrackRepository trackRepo;
    private final Intent notificationClickIntent;


    private final Player.EventListener exoPlayerEventListener = new NewtonePlayerEventListener();
    private final PlayerNotificationManager.MediaDescriptionAdapter mediaDescriptionAdapter = new NewtoneMediaDescriptorAdapter();


    //    RX Update listeners
    private final BehaviorSubject<Integer> currentTrackIdObserver = BehaviorSubject.createDefault(EmptyItems.NO_TRACK.getId());
    private final BehaviorSubject<List<Integer>> currentTracklistUpdate = BehaviorSubject.createDefault(Collections.emptyList());
    private final BehaviorSubject<Boolean> isPlayingObserver = BehaviorSubject.createDefault(Boolean.FALSE);
    private final BehaviorSubject<Boolean> repeatModeObserver;
    private final BehaviorSubject<Boolean> shuffleModeObserver;

    private static final long PLAYBACK_UPDATE_TIME_MS = 200;

    //TODO посмотреть время выполнения, возможно стоит оптимизировать
    public PlayerInteractorImpl(Context context, TrackRepository trackRepo, Intent notificationClickIntent) {
        long startTime = System.currentTimeMillis();
        exoPlayer = new SimpleExoPlayer.Builder(context, new DefaultRenderersFactory(context))
                .setTrackSelector(new DefaultTrackSelector(context))
                .build();
        this.context = context;
        this.trackRepo = trackRepo;
        this.notificationClickIntent = notificationClickIntent;


        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build();

        exoPlayer.setAudioAttributes(audioAttributes, true);
        exoPlayer.setHandleAudioBecomingNoisy(true);    // приостанавливаем воспроизведении при отключении наушников


        MediaSessionCompat mediaSession = setupMediaSession(context);
        PlayerNotificationManager notificationManager = setupNotificationManager(context, mediaSession);


        settingSaver = new PlayerSettingSaver(context);
        audioEffects = new AudioEffectsInteractorImpl(exoPlayer, settingSaver);
        playerSetting = new PlayerSettingImpl(settingSaver, notificationManager);

        //  Восстанавливаем режим повторения
        exoPlayer.setRepeatMode(settingSaver.isRepeatModeEnabled() ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        repeatModeObserver = BehaviorSubject.createDefault(settingSaver.isRepeatModeEnabled());


        //  Восстанавливаем режим перемешивания
        exoPlayer.setShuffleModeEnabled(settingSaver.isShuffleModeEnabled());
        shuffleModeObserver = BehaviorSubject.createDefault(settingSaver.isShuffleModeEnabled());

        exoPlayer.addListener(exoPlayerEventListener);

        //  Исключаем трек из плейлиста если он был удалён с устройства
        trackRepo.observeTrackDeleting()
                .subscribe(this::removeAllById);

        //  Восстанавливаем состояние плеера перед выходом из приложения
        trackRepo.getByIds(settingSaver.getSavedPlaylist())
                .subscribe(tracks -> start(tracks, settingSaver.getSavedWindowIndex(), false, settingSaver.getSavedPlaybackPosition()));

        PlayerService.playerInteractor = this;

        long endTime = System.currentTimeMillis();
        Log.d(LOG_TAG, "PLAYER INTERACTOR CONSTRUCTOR END EXECUTION WITH " + (endTime - startTime) + " MS");
    }


    private MediaSessionCompat setupMediaSession(Context context) {
        MediaSessionCompat mediaSession = new MediaSessionCompat(context, "Newtone");
        MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(exoPlayer);

        return mediaSession;
    }


    private static final String NOTIFICATION_CHANNEL_ID = "com.parabola.player_feature.PlayerInteractorImpl.NOTIFICATION_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 47;

    private PlayerNotificationManager setupNotificationManager(Context context, MediaSessionCompat mediaSession) {
        PlayerNotificationManager notificationManager = PlayerNotificationManager
                .createWithNotificationChannel(
                        context, NOTIFICATION_CHANNEL_ID, R.string.app_name, R.string.app_name, NOTIFICATION_ID,
                        mediaDescriptionAdapter, playerNotificationListener);

        notificationManager.setPlayer(exoPlayer);
        notificationManager.setMediaSessionToken(mediaSession.getSessionToken());
        notificationManager.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationManager.setUseNavigationActionsInCompactView(true);
        notificationManager.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationManager.setUseChronometer(false);
        notificationManager.setFastForwardIncrementMs(0);
        notificationManager.setRewindIncrementMs(0);

        return notificationManager;
    }

    private PlayerNotificationManager.NotificationListener playerNotificationListener = new PlayerNotificationManager.NotificationListener() {
        @Override
        public void onNotificationStarted(int notificationId, Notification notification) {
        }

        @Override
        public void onNotificationCancelled(int notificationId) {
        }

        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            if (newtonePlayerListener != null) {
                newtonePlayerListener.onNotificationCancelled(notificationId, dismissedByUser);
            }
        }

        @Override
        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            if (newtonePlayerListener != null) {
                newtonePlayerListener.onNotificationPosted(notificationId, notification, ongoing);
            }
        }
    };

    private NewtonePlayerListener newtonePlayerListener;

    void setNewtonePlayerListener(NewtonePlayerListener listener) {
        newtonePlayerListener = listener;
    }

    interface NewtonePlayerListener {
        void onNotificationPosted(int notificationId, Notification notification, boolean ongoing);
        void onNotificationCancelled(int notificationId, boolean dismissedByUser);
    }


    @Override
    public void start(List<Track> tracklist, int trackPosition, boolean startImmediately, long playbackPositionMs) {
        boolean isPlaylistChanged = !isNewPlaylistIdentical(tracklist);
        boolean isCurrentTrackChanged;

        if (isPlaylistChanged) {
            isCurrentTrackChanged = true;

            concatenatedSource.clear();
            for (int i = 0; i < tracklist.size(); i++) {
                concatenatedSource.addMediaSource(mediaSourceFromTrack(tracklist.get(i)));
            }
        } else {
            isCurrentTrackChanged = currentTrackPosition() != trackPosition;
        }
        exoPlayer.setPlayWhenReady(startImmediately);
        if (isCurrentTrackChanged) {
            exoPlayer.prepare(concatenatedSource);
            if (isPlaylistChanged) {
                currentTracklistUpdate.onNext(getTrackIds());
            }
            exoPlayer.seekToDefaultPosition(trackPosition);
            if (playbackPositionMs != 0) {
                exoPlayer.seekTo(playbackPositionMs);
            }
        }

        settingSaver.setSavedPlaylist(concatenatedSource, trackPosition);
    }

    private boolean isNewPlaylistIdentical(List<Track> second) {
        if (second == null
                || tracksCount() != second.size()) {
            return false;
        }

        for (int i = 0; i < tracksCount(); i++) {
            if ((int) concatenatedSource.getMediaSource(i).getTag() != second.get(i).getId())
                return false;
        }

        return true;
    }

    private MediaSource mediaSourceFromTrack(Track track) {
        Uri uri = Uri.fromFile(new File(track.getFilePath()));

        return new ProgressiveMediaSource.Factory(dataSourceFactory).setTag(track.getId())
                .createMediaSource(uri);
    }

    @Override
    public void stop() {
        pause();
        exoPlayer.stop();
        seekTo(0L);
    }

    @Override
    public void next() {
        exoPlayer.next();
    }

    @Override
    public void previous() {
        exoPlayer.previous();
    }

    @Override
    public int tracksCount() {
        return concatenatedSource.getSize();
    }

    @Override
    public int currentTrackPosition() {
        return concatenatedSource.getSize() != 0
                ? exoPlayer.getCurrentWindowIndex()
                : -1;
    }

    @Override
    public int currentTrackId() {
        if (concatenatedSource.getSize() == 0) return -1;
        return (int) concatenatedSource.getMediaSource(exoPlayer.getCurrentWindowIndex()).getTag();
    }

    private final PublishSubject<Entry<Integer, Integer>> onMoveTrackObserver = PublishSubject.create();

    @Override
    public Observable<Entry<Integer, Integer>> onMoveTrack() {
        return onMoveTrackObserver;
    }

    @Override
    public Completable moveTrack(int oldPosition, int newPosition) {
        return Completable.fromAction(() -> {
            concatenatedSource.moveMediaSource(oldPosition, newPosition);

            Entry<Integer, Integer> entry = new SimpleImmutableEntry<>(oldPosition, newPosition);
            onMoveTrackObserver.onNext(entry);
            currentTracklistUpdate.onNext(getTrackIds());

            settingSaver.setSavedPlaylist(concatenatedSource, exoPlayer.getCurrentWindowIndex());
        });
    }


    private final PublishSubject<Entry<Integer, Integer>> onRemoveTrackObserver = PublishSubject.create();

    @Override
    public Observable<Entry<Integer, Integer>> onRemoveTrack() {
        return onRemoveTrackObserver;
    }

    @Override
    public Completable remove(int trackPosition) {
        return Completable.fromAction(() -> {
            Integer trackId = (Integer) concatenatedSource.getMediaSource(trackPosition).getTag();
            concatenatedSource.removeMediaSource(trackPosition);

            Entry<Integer, Integer> entry = new SimpleImmutableEntry<>(trackId, trackPosition);
            onRemoveTrackObserver.onNext(entry);
            currentTracklistUpdate.onNext(getTrackIds());

            settingSaver.setSavedPlaylist(concatenatedSource, exoPlayer.getCurrentWindowIndex());
        });
    }


    private void removeAllById(int deletedTrackId) {
        boolean hasRemoved = false;
        for (int i = 0; i < concatenatedSource.getSize(); i++) {
            if ((int) concatenatedSource.getMediaSource(i).getTag() == deletedTrackId) {
                hasRemoved = true;
                concatenatedSource.removeMediaSource(i);

                Entry<Integer, Integer> entry = new SimpleImmutableEntry<>(deletedTrackId, i);
                onRemoveTrackObserver.onNext(entry);
            }
        }
        if (hasRemoved) {
            currentTracklistUpdate.onNext(getTrackIds());
        }
    }


    @Override
    public void resume() {
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        exoPlayer.setPlayWhenReady(false);
    }


    @Override
    public void seekTo(long playbackPositionMs) {
        exoPlayer.seekTo(playbackPositionMs);
        settingSaver.setPlaybackPosition(playbackPositionMs);
    }


    private Long lastPlaybackPosition = -1L;

    @Override
    public long playbackPosition() {
        return exoPlayer.getCurrentPosition();
    }

    @Override
    public Flowable<Long> onChangePlaybackPosition() {
        return Flowable.interval(0, PLAYBACK_UPDATE_TIME_MS, TimeUnit.MILLISECONDS, AndroidSchedulers.from(exoPlayer.getApplicationLooper()))
                .filter(count -> lastPlaybackPosition != exoPlayer.getCurrentPosition())
                .doOnNext(count -> lastPlaybackPosition = exoPlayer.getCurrentPosition())
                .map(count -> exoPlayer.getCurrentPosition());
    }

    @Override
    public void toggleRepeatMode() {
        setRepeat(!(exoPlayer.getRepeatMode() == Player.REPEAT_MODE_ONE));
    }

    @Override
    public void setRepeat(boolean enable) {
        settingSaver.setRepeatMode(enable);
        exoPlayer.setRepeatMode(enable ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
    }

    @Override
    public Observable<Boolean> onRepeatModeChange() {
        return repeatModeObserver;
    }

    @Override
    public boolean isRepeatModeEnabled() {
        return exoPlayer.getRepeatMode() == Player.REPEAT_MODE_ONE;
    }

    @Override
    public void toggleShuffleMode() {
        setShuffleMode(!exoPlayer.getShuffleModeEnabled());
    }

    @Override
    public void setShuffleMode(boolean enable) {
        settingSaver.setShuffleMode(enable);
        exoPlayer.setShuffleModeEnabled(enable);
    }

    @Override
    public Observable<Boolean> onShuffleModeChange() {
        return shuffleModeObserver;
    }

    @Override
    public boolean isShuffleEnabled() {
        return exoPlayer.getShuffleModeEnabled();
    }

    @Override
    public boolean isPlayWhenReady() {
        return exoPlayer.getPlayWhenReady();
    }

    @Override
    public Observable<Integer> onChangeCurrentTrackId() {
        return currentTrackIdObserver;
    }


    private List<Integer> getTrackIds() {
        List<Integer> ids = new ArrayList<>(concatenatedSource.getSize());
        for (int i = 0; i < concatenatedSource.getSize(); i++) {
            Integer trackId = (Integer) concatenatedSource.getMediaSource(i).getTag();
            ids.add(trackId);
        }
        return ids;
    }

    @Override
    public Observable<List<Integer>> onTracklistChanged() {
        return currentTracklistUpdate;
    }

    @Override
    public Observable<Boolean> onChangePlayingState() {
        return isPlayingObserver;
    }

    @Override
    public AudioEffectsInteractor getAudioEffectInteractor() {
        return audioEffects;
    }

    @Override
    public PlayerSetting getPlayerSetting() {
        return playerSetting;
    }

    private class NewtonePlayerEventListener implements Player.EventListener {

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            isPlayingObserver.onNext(isPlaying);
            settingSaver.setPlaybackPosition(exoPlayer.getCurrentPosition());
            runServiceIfNeeded(isPlaying);
        }

        private void runServiceIfNeeded(boolean isPlaying) {
            if (isPlaying && !PlayerService.isRunning) {
                Intent intent = new Intent(context, PlayerService.class);
                context.startService(intent);
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION
                    || reason == Player.DISCONTINUITY_REASON_SEEK) {
                refreshCurrentTrack();
            }
        }

        @Override
        public void onTimelineChanged(Timeline timeline, int reason) {
            if (reason == Player.TIMELINE_CHANGE_REASON_DYNAMIC) {
                refreshCurrentTrack();
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            repeatModeObserver.onNext(repeatMode == Player.REPEAT_MODE_ONE);
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            shuffleModeObserver.onNext(shuffleModeEnabled);
        }

        private void refreshCurrentTrack() {
            if (concatenatedSource.getSize() == 0) {
                currentTrackIdObserver.onNext(EmptyItems.NO_TRACK.getId());
            } else {
                int currentTrackIndex = exoPlayer.getCurrentWindowIndex();
                Integer trackId = (Integer) concatenatedSource.getMediaSource(currentTrackIndex).getTag();
                currentTrackIdObserver.onNext(Objects.requireNonNull(trackId));

                settingSaver.setCurrentWindowIndex(currentTrackIndex);
                settingSaver.setPlaybackPosition(exoPlayer.getCurrentPosition());
            }
        }
    }


    private class NewtoneMediaDescriptorAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {
        private Track currentTrack = EmptyItems.NO_TRACK;

        private Track getCurrentTrack(Player player) {
            if (concatenatedSource.getSize() == 0) {
                currentTrack = EmptyItems.NO_TRACK;
                return currentTrack;
            }
            MediaSource currentMediaSource = concatenatedSource.getMediaSource(player.getCurrentWindowIndex());
            int currentTrackId = (int) currentMediaSource.getTag();

            if (currentTrackId != currentTrack.getId()) {
                currentTrack = trackRepo.getById(currentTrackId).blockingGet();
            }

            return currentTrack;
        }

        @Override
        @NonNull
        public String getCurrentContentTitle(@NonNull Player player) {
            return getCurrentTrack(player).getTitle();
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(@NonNull Player player) {
            return PendingIntent.getActivity(context, 0, notificationClickIntent, 0);
        }

        @Override
        public String getCurrentContentText(@NonNull Player player) {
            Track currentTrack = getCurrentTrack(player);
            return currentTrack.getArtistName() + " - " + currentTrack.getAlbumTitle();
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(@NonNull Player player, @NonNull PlayerNotificationManager.BitmapCallback bitmapCallback) {
            if (!playerSetting.isNotificationArtworkShow()) {
                return null;
            }

            Track track = getCurrentTrack(player);
            Bitmap image = track.getArtImage();
            if (image == null) {
                image = getDefaultBitmap();
            }
            return image;
        }
    }

    private WeakReference<Bitmap> defaultArtWeak;

    private Bitmap getDefaultBitmap() {
        if (defaultArtWeak == null || defaultArtWeak.get() == null) {
            defaultArtWeak = new WeakReference<>(BitmapFactory.decodeResource(context.getResources(), R.drawable.album_holder));
        }
        return defaultArtWeak.get();
    }

}
