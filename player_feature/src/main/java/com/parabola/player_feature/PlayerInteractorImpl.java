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
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.parabola.domain.interactor.RepositoryInteractor;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;


public final class PlayerInteractorImpl implements PlayerInteractor {
    private static final String LOG_TAG = PlayerInteractorImpl.class.getSimpleName();

    private final AudioEffectsInteractorImpl audioEffects;
    private final PlayerSetting playerSetting;
    private final PlayerSettingSaver settingSaver;


    //    ExoPlayer
    private final SimpleExoPlayer exoPlayer;
    private final DefaultControlDispatcher DEFAULT_CONTROL_DISPATCHER = new DefaultControlDispatcher(0, 0);
    private final PlayerNotificationManager notificationManager;


    private final Context context;
    private final TrackRepository trackRepo;
    private final Intent notificationClickIntent;


    //    RX Update listeners
    private final BehaviorSubject<Integer> currentTrackIdObserver = BehaviorSubject.createDefault(EmptyItems.NO_TRACK.getId());
    private final BehaviorSubject<List<Integer>> currentTracklistUpdate = BehaviorSubject.createDefault(Collections.emptyList());
    private final BehaviorSubject<Boolean> isPlayingObserver = BehaviorSubject.createDefault(Boolean.FALSE);
    private final BehaviorSubject<RepeatMode> repeatModeObserver;
    private final BehaviorSubject<Boolean> shuffleModeObserver;

    private static final long PLAYBACK_UPDATE_TIME_MS = 200;

    public PlayerInteractorImpl(Context context, SharedPreferences preferences,
                                TrackRepository trackRepo,
                                RepositoryInteractor repositoryInteractor,
                                Intent notificationClickIntent) {
        exoPlayer = new SimpleExoPlayer.Builder(context, new AudioRenderersFactory(context))
                .setTrackSelector(new DefaultTrackSelector(context))
                .build();
        exoPlayer.prepare();
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
        RepeatMode repeatMode = settingSaver.getSavedRepeatMode();
        switch (repeatMode) {
            case OFF: exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF); break;
            case ALL: exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL); break;
            case ONE: exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE); break;
            default: throw new IllegalArgumentException(repeatMode.name());
        }
        repeatModeObserver = BehaviorSubject.createDefault(repeatMode);


        //  Восстанавливаем режим перемешивания
        exoPlayer.setShuffleModeEnabled(settingSaver.isShuffleModeEnabled());
        shuffleModeObserver = BehaviorSubject.createDefault(settingSaver.isShuffleModeEnabled());

        exoPlayer.addListener(new PlayerListener());

        //  Исключаем трек из плейлиста если он был удалён с устройства
        this.trackRepo.observeTrackDeleting()
                .subscribe(new ConsumerObserver<>(this::removeAllById));

        this.trackRepo.observeFavouritesChanged()
                .subscribe(new ConsumerObserver<>(i -> notificationManager.invalidate()));

        //  Восстанавливаем состояние плеера, которое было перед выходом из приложения
        repositoryInteractor.observeLoadingState()
                .filter(loadingState -> loadingState == RepositoryInteractor.LoadingState.LOADED)
                .firstOrError()
                .flatMap(loadingState -> trackRepo.getByIds(settingSaver.getSavedPlaylist()))
                .onErrorReturnItem(Collections.emptyList()) //в случае ошибки восстановления не происходит
                .subscribe(new ConsumerSingleObserver<>(
                        tracks -> start(tracks, settingSaver.getSavedWindowIndex(), false, settingSaver.getSavedPlaybackPosition()),
                        Functions.ERROR_CONSUMER));

        // после обновления списка исключённых папок из плеера удаляются те треки, у которых папки есть в списке исключения
        // игнорируем первую загрузку, так как первая загрзука не будет списком исключённых папок
        repositoryInteractor.observeLoadingState()
                .filter(loadingState -> loadingState == RepositoryInteractor.LoadingState.LOADED)
                .skip(1)
                .subscribe(ConsumerObserver.fromConsumer(loadingState -> {
                    for (Integer trackId : getTrackIds()) {
                        if (!trackRepo.isExists(trackId)) {
                            removeAllById(trackId);
                        }
                    }
                }));

        //закрываем уведомление, если очередь воспроизведения пуста
        currentTracklistUpdate
                .observeOn(AndroidSchedulers.from(exoPlayer.getApplicationLooper()))
                .subscribe(ConsumerObserver.fromConsumer(trackIds -> {
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
        NotificationUtil.createNotificationChannel(context,
                NOTIFICATION_CHANNEL_ID, R.string.notification_channel_id, 0,
                NotificationUtil.IMPORTANCE_LOW);
        PlayerNotificationManager notificationManager = new PlayerNotificationManager(
                context, NOTIFICATION_CHANNEL_ID, NOTIFICATION_ID,
                mediaDescriptionAdapter, playerNotificationListener, customActionReceiver);

        notificationManager.setMediaSessionToken(mediaSession.getSessionToken());
        notificationManager.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationManager.setUseNavigationActionsInCompactView(true);
        notificationManager.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationManager.setUseChronometer(false);
        notificationManager.setControlDispatcher(DEFAULT_CONTROL_DISPATCHER);

        return notificationManager;
    }


    @Override
    public void start(List<Track> tracklist, int trackPosition, boolean startImmediately, long playbackPositionMs) {
        if (tracklist.isEmpty() || trackPosition < 0 || trackPosition >= tracklist.size())
            return;

        boolean isPlaylistChanged = !isNewPlaylistIdentical(tracklist);

        if (isPlaylistChanged) {
            exoPlayer.pause();
            exoPlayer.setMediaItems(getMediaItemsFromTrackTrackList(tracklist), trackPosition, playbackPositionMs);
        } else {
            boolean isCurrentTrackChanged = currentTrackPosition() != trackPosition;
            if (isCurrentTrackChanged) {
                exoPlayer.pause();
                exoPlayer.seekTo(trackPosition, playbackPositionMs);
            }
        }
        exoPlayer.setPlayWhenReady(startImmediately);
    }

    private boolean isNewPlaylistIdentical(List<Track> second) {
        if (second == null
                || tracksCount() != second.size()) {
            return false;
        }

        for (int i = 0; i < tracksCount(); i++) {
            if ((int) exoPlayer.getMediaItemAt(i).playbackProperties.tag != second.get(i).getId())
                return false;
        }

        return true;
    }

    private List<MediaItem> getMediaItemsFromTrackTrackList(List<Track> tracklist) {
        return tracklist.stream()
                .map(this::mediaSourceFromTrack)
                .collect(Collectors.toList());
    }

    private MediaItem mediaSourceFromTrack(Track track) {
        return new MediaItem.Builder()
                .setUri(Uri.fromFile(new File(track.getFilePath())))
                .setTag(track.getId())
                .build();
    }


    @Override
    public void startInShuffleMode(List<Track> tracklist) {
        if (tracklist.isEmpty())
            return;
        exoPlayer.pause();

        exoPlayer.setMediaItems(getMediaItemsFromTrackTrackList(tracklist));
        setShuffleMode(true);

        exoPlayer.play();
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
        return exoPlayer.getMediaItemCount();
    }

    @Override
    public int currentTrackPosition() {
        return exoPlayer.getMediaItemCount() != 0
                ? exoPlayer.getCurrentWindowIndex()
                : -1;
    }

    @Override
    public int currentTrackId() {
        if (exoPlayer.getCurrentMediaItem() == null || exoPlayer.getCurrentMediaItem().playbackProperties == null)
            return EmptyItems.NO_TRACK.getId();
        return (int) exoPlayer.getCurrentMediaItem().playbackProperties.tag;
    }

    private final PublishSubject<MovedTrackItem> onMoveTrackObserver = PublishSubject.create();

    @Override
    public Observable<MovedTrackItem> onMoveTrack() {
        return onMoveTrackObserver;
    }

    @Override
    public Completable moveTrack(int oldPosition, int newPosition) {
        return Completable.fromAction(() -> {
            exoPlayer.moveMediaItem(oldPosition, newPosition);

            onMoveTrackObserver.onNext(PlayerInteractor.createMoveTrackItem(oldPosition, newPosition));
        });
    }


    private final PublishSubject<RemovedTrackItem> onRemoveTrackObserver = PublishSubject.create();

    @Override
    public Observable<RemovedTrackItem> onRemoveTrack() {
        return onRemoveTrackObserver;
    }

    @Override
    public Completable remove(int trackPosition) {
        //сейчас exoPlayer перекидывает на первый трек в случае, если установлен режим повтора одного трека
        if (getRepeatMode() == RepeatMode.ONE
                && trackPosition == currentTrackPosition())
            next();

        return Completable.fromAction(() -> {
            MediaItem removedItem = exoPlayer.getMediaItemAt(trackPosition);
            int trackId = (Integer) removedItem.playbackProperties.tag;
            exoPlayer.removeMediaItem(trackPosition);

            onRemoveTrackObserver.onNext(PlayerInteractor.createRemoveTrackItem(trackId, trackPosition));
        });
    }


    private void removeAllById(int deletedTrackId) {
        for (int i = 0; i < exoPlayer.getMediaItemCount(); i++) {
            if ((int) exoPlayer.getMediaItemAt(i).playbackProperties.tag == deletedTrackId) {
                exoPlayer.removeMediaItem(i);

                onRemoveTrackObserver.onNext(PlayerInteractor.createRemoveTrackItem(deletedTrackId, i));
            }
        }
    }


    @Override
    public void resume() {
        exoPlayer.play();
    }

    @Override
    public void pause() {
        exoPlayer.pause();
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
        RepeatMode repeatMode = getRepeatMode();
        switch (repeatMode) {
            case OFF: setRepeatMode(RepeatMode.ALL); break;
            case ALL: setRepeatMode(RepeatMode.ONE); break;
            case ONE: setRepeatMode(RepeatMode.OFF); break;
            default: throw new IllegalStateException(repeatMode.name());
        }
    }

    @Override
    public void setRepeatMode(RepeatMode repeatMode) {
        settingSaver.setRepeatMode(repeatMode);

        switch (repeatMode) {
            case OFF: exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF); break;
            case ALL: exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL); break;
            case ONE: exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE); break;
            default: throw new IllegalArgumentException(repeatMode.name());
        }
    }

    @Override
    public Observable<RepeatMode> onRepeatModeChange() {
        return repeatModeObserver;
    }

    @Override
    public RepeatMode getRepeatMode() {
        return repeatModeObserver.getValue();
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
        List<Integer> ids = new ArrayList<>(exoPlayer.getMediaItemCount());
        for (int i = 0; i < exoPlayer.getMediaItemCount(); i++) {
            Integer trackId = (Integer) exoPlayer.getMediaItemAt(i).playbackProperties.tag;
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


    private NewtonePlayerListener newtonePlayerListener;

    void setNewtonePlayerListener(NewtonePlayerListener listener) {
        newtonePlayerListener = listener;
    }

    interface NewtonePlayerListener {
        void onNotificationPosted(int notificationId, Notification notification, boolean ongoing);
        void onNotificationCancelled(int notificationId, boolean dismissedByUser);
    }


    private final PlayerNotificationManager.NotificationListener playerNotificationListener = new PlayerNotificationManager.NotificationListener() {
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


    private boolean isServiceBound = false;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notificationManager.setPlayer(exoPlayer);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    private class PlayerListener implements Player.EventListener {
        @Override
        public void onTimelineChanged(Timeline timeline, int reason) {
            if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
                List<Integer> trackIds = getTrackIds();
                currentTracklistUpdate.onNext(trackIds);
                settingSaver.setSavedPlaylist(trackIds, exoPlayer.getCurrentWindowIndex());
            }
        }

        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            if (playWhenReady != isPlayingObserver.getValue()) {
                isPlayingObserver.onNext(playWhenReady);
                settingSaver.setPlaybackPosition(exoPlayer.getCurrentPosition());
                runServiceIfNeeded(playWhenReady);
            }
        }

        @Override
        public void onPlaybackStateChanged(int state) {
            if (state == Player.STATE_ENDED) {
                onQueueEnded();
            }
        }

        private void runServiceIfNeeded(boolean isPlaying) {
            if (isPlaying && !isServiceBound) {
                Intent intent = new Intent(context, PlayerService.class);
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                isServiceBound = true;
            }
        }

        private void onQueueEnded() {
            pause();
            seekTo(0);
        }

        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
            refreshCurrentTrack();
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            switch (repeatMode) {
                case Player.REPEAT_MODE_OFF: repeatModeObserver.onNext(RepeatMode.OFF); break;
                case Player.REPEAT_MODE_ALL: repeatModeObserver.onNext(RepeatMode.ALL); break;
                case Player.REPEAT_MODE_ONE: repeatModeObserver.onNext(RepeatMode.ONE); break;
                default: throw new IllegalArgumentException("Repeat mode: " + repeatMode);
            }
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            shuffleModeObserver.onNext(shuffleModeEnabled);
        }

        private void refreshCurrentTrack() {
            if (exoPlayer.getMediaItemCount() == 0) {
                currentTrackIdObserver.onNext(EmptyItems.NO_TRACK.getId());
            } else {
                currentTrackIdObserver.onNext(currentTrackId());

                settingSaver.setCurrentWindowIndex(exoPlayer.getCurrentWindowIndex());
                settingSaver.setPlaybackPosition(exoPlayer.getCurrentPosition());
            }
        }
    }


    private Bitmap defaultNotificationAlbumArt;

    public void setDefaultNotificationAlbumArt(Bitmap bitmap) {
        this.defaultNotificationAlbumArt = bitmap;
        notificationManager.invalidate();
    }

    private final PlayerNotificationManager.MediaDescriptionAdapter mediaDescriptionAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {
        private Track currentTrack = EmptyItems.NO_TRACK;

        private Track getCurrentTrack(Player player) {
            if (player.getMediaItemCount() == 0) {
                currentTrack = EmptyItems.NO_TRACK;
                return currentTrack;
            }

            int currentTrackId = currentTrackId();

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
    };


    private final PlayerNotificationManager.CustomActionReceiver customActionReceiver = new PlayerNotificationManager.CustomActionReceiver() {

        private static final String CUSTOM_ACTION_ADD_TO_FAVORITES = "com.parabola.player.feature.PlayerInteractorImpl.ADD_TO_FAVORITES";
        private static final String CUSTOM_ACTION_REMOVE_FROM_FAVORITES = "com.parabola.player.feature.PlayerInteractorImpl.REMOVE_FROM_FAVORITES";


        private PendingIntent createBroadcastIntent(String action, int instanceId) {
            Intent intent = new Intent(action).setPackage(context.getPackageName());
            intent.putExtra(PlayerNotificationManager.EXTRA_INSTANCE_ID, instanceId);

            return PendingIntent.getBroadcast(
                    context, instanceId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }


        @NonNull
        @Override
        public Map<String, NotificationCompat.Action> createCustomActions(@NonNull Context context, int instanceId) {
            Map<String, NotificationCompat.Action> customActions = new HashMap<>();

            customActions.put(CUSTOM_ACTION_ADD_TO_FAVORITES, new NotificationCompat.Action(
                    R.drawable.ic_notification_not_favourite, context.getString(R.string.notification_action_add_to_favourites),
                    createBroadcastIntent(CUSTOM_ACTION_ADD_TO_FAVORITES, instanceId)));
            customActions.put(CUSTOM_ACTION_REMOVE_FROM_FAVORITES, new NotificationCompat.Action(
                    R.drawable.ic_notification_favourite, context.getString(R.string.notification_action_remove_from_favourites),
                    createBroadcastIntent(CUSTOM_ACTION_REMOVE_FROM_FAVORITES, instanceId)));

            return customActions;
        }

        @NonNull
        @Override
        public List<String> getCustomActions(@NonNull Player player) {
            List<String> customActions = new ArrayList<>();
            if (trackRepo.isFavourite(currentTrackId()))
                customActions.add(CUSTOM_ACTION_REMOVE_FROM_FAVORITES);
            else customActions.add(CUSTOM_ACTION_ADD_TO_FAVORITES);

            return customActions;
        }

        @Override
        public void onCustomAction(@NonNull Player player, @NonNull String action, @NonNull Intent intent) {
            switch (action) {
                case CUSTOM_ACTION_ADD_TO_FAVORITES:
                    trackRepo.addToFavourites(currentTrackId());
                    break;
                case CUSTOM_ACTION_REMOVE_FROM_FAVORITES:
                    trackRepo.removeFromFavourites(currentTrackId());
                    break;
            }
        }
    };

}
