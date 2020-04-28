package com.parabola.player_feature;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.C;
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
import com.parabola.domain.interactor.observer.ConsumerObserver;
import com.parabola.domain.interactor.player.AudioEffectsInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.interactor.player.PlayerSetting;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.EmptyItems;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static java.util.Objects.requireNonNull;


public class PlayerInteractorImpl implements PlayerInteractor {
    private static final String LOG_TAG = PlayerInteractorImpl.class.getSimpleName();

    private final AudioEffectsInteractorImpl audioEffects;
    private final PlayerSetting playerSetting;
    private final PlayerSettingSaver settingSaver;


    //    ExoPlayer
    private final ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource();
    private final DataSource.Factory dataSourceFactory = new FileDataSource.Factory();
    private final SimpleExoPlayer exoPlayer;
    private final PlayerNotificationManager notificationManager;


    private final Context context;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
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

    public PlayerInteractorImpl(Context context, SharedPreferences preferences,
                                TrackRepository trackRepo, Intent notificationClickIntent) {
        exoPlayer = new SimpleExoPlayer.Builder(context, new AudioRenderersFactory(context))
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
        exoPlayer.setWakeMode(C.WAKE_MODE_LOCAL);

        MediaSessionCompat mediaSession = setupMediaSession(context);
        notificationManager = setupNotificationManager(context, mediaSession);


        settingSaver = new PlayerSettingSaver(preferences);
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
                .subscribe(new ConsumerObserver<>(this::removeAllById));

        //  Восстанавливаем состояние плеера перед выходом из приложения
        trackRepo.getByIds(settingSaver.getSavedPlaylist())
                .subscribe(new ConsumerSingleObserver<>(
                        tracks -> start(tracks, settingSaver.getSavedWindowIndex(), false, settingSaver.getSavedPlaybackPosition()),
                        null));

        //закрываем уведомление, если очередь воспроизведения пуста
        currentTracklistUpdate
                .observeOn(AndroidSchedulers.from(exoPlayer.getApplicationLooper()))
                .subscribe(new ConsumerObserver<>(trackIds -> {
                    if (trackIds.isEmpty())
                        clearNotificationManagerAndUnbindService();
                }));

        PlayerService.playerInteractor = this;
    }


    public void closeNotificationIfPaused() {
        if (!isPlayWhenReady())
            clearNotificationManagerAndUnbindService();
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
        @SuppressWarnings("deprecation")
        public void onNotificationStarted(int notificationId, Notification notification) {
        }

        @Override
        @SuppressWarnings("deprecation")
        public void onNotificationCancelled(int notificationId) {
        }

        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            if (newtonePlayerListener != null) {
                newtonePlayerListener.onNotificationCancelled(notificationId, dismissedByUser);
            }
            clearNotificationManagerAndUnbindService();
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
        if (trackPosition >= tracklist.size())
            return;

        boolean isPlaylistChanged = !isNewPlaylistIdentical(tracklist);
        boolean isCurrentTrackChanged;

        if (isPlaylistChanged) {
            isCurrentTrackChanged = true;

            concatenatedSource.clear();
            for (Track track : tracklist) {
                concatenatedSource.addMediaSource(mediaSourceFromTrack(track));
            }
        } else {
            isCurrentTrackChanged = currentTrackPosition() != trackPosition;
        }
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
        exoPlayer.setPlayWhenReady(startImmediately);

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

    private final PublishSubject<MovedTrackItem> onMoveTrackObserver = PublishSubject.create();

    @Override
    public Observable<MovedTrackItem> onMoveTrack() {
        return onMoveTrackObserver;
    }

    @Override
    public Completable moveTrack(int oldPosition, int newPosition) {
        return Completable.fromAction(() -> {
            AtomicBoolean isFinished = new AtomicBoolean(false);
            concatenatedSource.moveMediaSource(oldPosition, newPosition, mainThreadHandler,
                    () -> isFinished.set(true));

            while (!isFinished.get()) ;

            onMoveTrackObserver.onNext(PlayerInteractor.createMoveTrackItem(oldPosition, newPosition));
            currentTracklistUpdate.onNext(getTrackIds());

            settingSaver.setSavedPlaylist(concatenatedSource, exoPlayer.getCurrentWindowIndex());
        });
    }


    private final PublishSubject<RemovedTrackItem> onRemoveTrackObserver = PublishSubject.create();

    @Override
    public Observable<RemovedTrackItem> onRemoveTrack() {
        return onRemoveTrackObserver;
    }

    @Override
    public Completable remove(int trackPosition) {
        return Completable.fromAction(() -> {
            Integer trackId = (Integer) concatenatedSource.removeMediaSource(trackPosition).getTag();

            onRemoveTrackObserver.onNext(PlayerInteractor.createRemoveTrackItem(requireNonNull(trackId), trackPosition));
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

                onRemoveTrackObserver.onNext(PlayerInteractor.createRemoveTrackItem(deletedTrackId, i));
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

    private void clearNotificationManagerAndUnbindService() {
        notificationManager.setPlayer(null);
        if (isServiceBound) {
            context.unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    private boolean isServiceBound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notificationManager.setPlayer(exoPlayer);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private class NewtonePlayerEventListener implements Player.EventListener {

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            isPlayingObserver.onNext(isPlaying);
            settingSaver.setPlaybackPosition(exoPlayer.getCurrentPosition());
            runServiceIfNeeded(isPlaying);
        }

        private void runServiceIfNeeded(boolean isPlaying) {
            if (isPlaying && !isServiceBound) {
                Intent intent = new Intent(context, PlayerService.class);
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                isServiceBound = true;
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
                currentTrackIdObserver.onNext(requireNonNull(trackId));

                settingSaver.setCurrentWindowIndex(currentTrackIndex);
                settingSaver.setPlaybackPosition(exoPlayer.getCurrentPosition());
            }
        }
    }

    private Bitmap defaultNotificationAlbumArt;

    public void setDefaultNotificationAlbumArt(Bitmap bitmap) {
        this.defaultNotificationAlbumArt = bitmap;
        notificationManager.invalidate();
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
                image = defaultNotificationAlbumArt;
            }
            return image;
        }
    }

}
