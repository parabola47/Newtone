package com.parabola.player_feature

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.NotificationUtil
import com.parabola.domain.interactor.RepositoryInteractor
import com.parabola.domain.interactor.RepositoryInteractor.LoadingState
import com.parabola.domain.interactor.observer.ConsumerObserver
import com.parabola.domain.interactor.player.AudioEffectsInteractor
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.interactor.player.PlayerInteractor.RepeatMode
import com.parabola.domain.interactor.player.PlayerSetting
import com.parabola.domain.model.Track
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.utils.EmptyItems
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.ConsumerSingleObserver
import io.reactivex.subjects.BehaviorSubject
import java.io.File
import java.util.concurrent.TimeUnit


private const val PLAYBACK_UPDATE_TIME_MS = 200L

private const val NOTIFICATION_CHANNEL_ID =
    "com.parabola.player_feature.PlayerInteractorImpl.NOTIFICATION_CHANNEL_ID"
private const val NOTIFICATION_ID = 47

class PlayerInteractorImpl(
    private val context: Context,
    preferences: SharedPreferences,
    private val trackRepo: TrackRepository,
    repositoryInteractor: RepositoryInteractor,
    private val notificationClickIntent: Intent
) : PlayerInteractor {


    private val audioEffects: AudioEffectsInteractorImpl
    private val playerSetting: PlayerSetting
    private val settingSaver: PlayerSettingSaver


    //    ExoPlayer
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context, AudioRenderersFactory(context))
        .setTrackSelector(DefaultTrackSelector(context))
        .build()

    private val notificationManager: PlayerNotificationManager


    //    RX Update listeners
    private val currentTrackIdObserver = BehaviorSubject.createDefault(EmptyItems.NO_TRACK.id)
    private val currentTracklistUpdate = BehaviorSubject.createDefault(emptyList<Int>())
    private val isPlayingObserver = BehaviorSubject.createDefault(false)
    private val repeatModeObserver: BehaviorSubject<RepeatMode>
    private val shuffleModeObserver: BehaviorSubject<Boolean>


    init {
        exoPlayer.apply {
            prepare()

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true) // приостанавливаем воспроизведении при отключении наушников
            setWakeMode(C.WAKE_MODE_LOCAL)
        }

        val mediaSession = setupMediaSession(context)
        notificationManager = setupNotificationManager(context, mediaSession)


        settingSaver = PlayerSettingSaver(preferences)
        audioEffects = AudioEffectsInteractorImpl(exoPlayer, settingSaver)
        playerSetting = PlayerSettingImpl(settingSaver, notificationManager)


        //  Восстанавливаем режим повторения
        val repeatMode = settingSaver.repeatMode
        exoPlayer.repeatMode = when (repeatMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            else -> throw IllegalArgumentException(repeatMode.name)
        }
        repeatModeObserver = BehaviorSubject.createDefault(repeatMode)


        //  Восстанавливаем режим перемешивания
        exoPlayer.shuffleModeEnabled = settingSaver.isShuffleModeEnabled
        shuffleModeObserver = BehaviorSubject.createDefault(settingSaver.isShuffleModeEnabled)

        exoPlayer.addListener(PlayerListener())

        //  Исключаем трек из плейлиста если он был удалён с устройства
        trackRepo.observeTrackDeleting()
            .observeOn(AndroidSchedulers.from(exoPlayer.applicationLooper))
            .subscribe(ConsumerObserver(this::removeAllById))
        trackRepo.observeFavouritesChanged()
            .subscribe(ConsumerObserver { notificationManager.invalidate() })

        //  Восстанавливаем состояние плеера, которое было перед выходом из приложения
        repositoryInteractor.observeLoadingState()
            .filter { loadingState: LoadingState -> loadingState == LoadingState.LOADED }
            .firstOrError()
            .flatMap { trackRepo.getByIds(settingSaver.savedPlaylist) }
            .onErrorReturnItem(emptyList()) //в случае ошибки восстановления не происходит
            .subscribe(
                ConsumerSingleObserver(
                    { tracks: List<Track> ->
                        start(
                            tracks,
                            settingSaver.savedWindowIndex,
                            false,
                            settingSaver.playbackPosition
                        )
                    },
                    Functions.ERROR_CONSUMER
                )
            )

        // после обновления списка исключённых папок из плеера удаляются те треки, у которых папки есть в списке исключения
        // игнорируем первую загрузку, так как первая загрзука не будет списком исключённых папок
        repositoryInteractor.observeLoadingState()
            .filter { loadingState: LoadingState -> loadingState == LoadingState.LOADED }
            .skip(1)
            .subscribe(ConsumerObserver.fromConsumer {
                for (trackId in trackIds) {
                    if (!trackRepo.isExists(trackId)) {
                        removeAllById(trackId)
                    }
                }
            })

        //закрываем уведомление, если очередь воспроизведения пуста
        currentTracklistUpdate
            .observeOn(AndroidSchedulers.from(exoPlayer.applicationLooper))
            .subscribe(ConsumerObserver.fromConsumer { trackIds: List<Int> -> if (trackIds.isEmpty()) clearNotificationManagerAndUnbindService() })

        PlayerService.playerInteractor = this
    }


    fun closeNotificationIfPaused() {
        if (!isPlayWhenReady) clearNotificationManagerAndUnbindService()
    }

    private fun setupMediaSession(context: Context): MediaSessionCompat {
        val mediaSession = MediaSessionCompat(context, "Newtone")
        val mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoPlayer)

        return mediaSession
    }


    private fun setupNotificationManager(
        context: Context,
        mediaSession: MediaSessionCompat,
    ): PlayerNotificationManager {
        NotificationUtil.createNotificationChannel(
            context,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_channel_id,
            0,
            NotificationUtil.IMPORTANCE_LOW
        )
        return PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID,
        )
            .setMediaDescriptionAdapter(PlayerMediaDescriptorAdapter())
            .setNotificationListener(playerNotificationListener)
            .build()
            .apply {
                setMediaSessionToken(mediaSession.sessionToken)
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                setPriority(NotificationCompat.PRIORITY_MAX)
                setUseChronometer(false)
            }
    }


    override fun start(
        tracklist: List<Track>,
        trackPosition: Int,
        startImmediately: Boolean,
        playbackPositionMs: Long
    ) {
        if (tracklist.isEmpty() || trackPosition < 0 || trackPosition >= tracklist.size) return

        val isPlaylistChanged = !isNewPlaylistIdentical(tracklist)

        if (isPlaylistChanged) {
            exoPlayer.pause()
            exoPlayer.setMediaItems(
                createMediaItemsFromTrackList(tracklist),
                trackPosition,
                playbackPositionMs
            )
        } else {
            val isCurrentTrackChanged = currentTrackPosition() != trackPosition
            if (isCurrentTrackChanged) {
                exoPlayer.pause()
                exoPlayer.seekTo(trackPosition, playbackPositionMs)
            }
        }
        exoPlayer.playWhenReady = startImmediately
    }

    private fun isNewPlaylistIdentical(second: List<Track>): Boolean {
        if (tracksCount() != second.size) {
            return false
        }

        for (i in 0 until tracksCount()) {
            if (getTrackIdByPosition(i) != second[i].id) return false
        }

        return true
    }

    private fun createMediaItemsFromTrackList(tracklist: List<Track>): List<MediaItem> {
        return tracklist.map(this::createMediaItemFromTrack)
    }

    private fun createMediaItemFromTrack(track: Track): MediaItem {
        return MediaItem.Builder()
            .setUri(Uri.fromFile(File(track.filePath)))
            .setTag(track.id)
            .build()
    }


    override fun startInShuffleMode(tracklist: List<Track>) {
        if (tracklist.isEmpty()) return

        exoPlayer.pause()

        exoPlayer.setMediaItems(createMediaItemsFromTrackList(tracklist))
        setShuffleMode(true)

        exoPlayer.play()
    }

    override fun stop() {
        pause()
        exoPlayer.stop()
        seekTo(0L)
    }

    override fun next() {
        exoPlayer.next()
    }

    override fun previous() {
        exoPlayer.previous()
    }

    override fun tracksCount(): Int = exoPlayer.mediaItemCount

    override fun currentTrackPosition(): Int =
        if (exoPlayer.mediaItemCount != 0) exoPlayer.currentWindowIndex else -1

    override fun currentTrackId(): Int {
        if (exoPlayer.currentMediaItem == null || exoPlayer.currentMediaItem!!.playbackProperties == null)
            return EmptyItems.NO_TRACK.id

        return exoPlayer.currentMediaItem!!.playbackProperties!!.tag as Int
    }


    override fun moveTrack(oldPosition: Int, newPosition: Int): Completable =
        Completable.fromAction { exoPlayer.moveMediaItem(oldPosition, newPosition) }

    override fun remove(trackPosition: Int): Completable {
        //сейчас exoPlayer перекидывает на первый трек в случае, если установлен режим повтора одного трека
        if (repeatMode == RepeatMode.ONE && trackPosition == currentTrackPosition()) {
            next()
        }

        return Completable.fromAction { exoPlayer.removeMediaItem(trackPosition) }
    }


    private fun removeAllById(deletedTrackId: Int) {
        for (i in 0 until exoPlayer.mediaItemCount) {
            if (getTrackIdByPosition(i) == deletedTrackId) {
                exoPlayer.removeMediaItem(i)
            }
        }
    }

    override fun resume() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun seekTo(playbackPositionMs: Long) {
        exoPlayer.seekTo(playbackPositionMs)
        settingSaver.playbackPosition = playbackPositionMs
    }


    private var lastPlaybackPosition = -1L

    override fun playbackPosition(): Long = exoPlayer.currentPosition

    override fun onChangePlaybackPosition(): Flowable<Long> =
        Flowable.interval(
            0,
            PLAYBACK_UPDATE_TIME_MS,
            TimeUnit.MILLISECONDS,
            AndroidSchedulers.from(exoPlayer.applicationLooper)
        )
            .filter { lastPlaybackPosition != exoPlayer.currentPosition }
            .doOnNext { lastPlaybackPosition = exoPlayer.currentPosition }
            .map { exoPlayer.currentPosition }

    override fun toggleRepeatMode() {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
            else -> RepeatMode.OFF
        }
    }

    override fun setRepeatMode(repeatMode: RepeatMode) {
        settingSaver.repeatMode = repeatMode

        exoPlayer.repeatMode = when (repeatMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    override fun onRepeatModeChange(): Observable<RepeatMode> = repeatModeObserver

    override fun getRepeatMode(): RepeatMode = repeatModeObserver.value!!

    override fun toggleShuffleMode() {
        setShuffleMode(!exoPlayer.shuffleModeEnabled)
    }

    override fun setShuffleMode(enable: Boolean) {
        settingSaver.isShuffleModeEnabled = enable
        exoPlayer.shuffleModeEnabled = enable
    }

    override fun onShuffleModeChange(): Observable<Boolean> = shuffleModeObserver

    override fun isShuffleEnabled(): Boolean = exoPlayer.shuffleModeEnabled

    override fun isPlayWhenReady(): Boolean = exoPlayer.playWhenReady

    override fun onChangeCurrentTrackId(): Observable<Int> = currentTrackIdObserver

    private fun getTrackIdByPosition(trackPosition: Int): Int {
        val mediaItem = exoPlayer.getMediaItemAt(trackPosition)
        if (mediaItem.playbackProperties == null || mediaItem.playbackProperties!!.tag == null)
            throw NullPointerException()

        return mediaItem.playbackProperties!!.tag as Int
    }

    private val trackIds: List<Int>
        get() {
            val ids: MutableList<Int> = ArrayList(exoPlayer.mediaItemCount)
            for (i in 0 until exoPlayer.mediaItemCount) {
                ids.add(getTrackIdByPosition(i))
            }
            return ids
        }

    override fun onTracklistChanged(): Observable<List<Int>> = currentTracklistUpdate

    override fun onChangePlayingState(): Observable<Boolean> = isPlayingObserver

    override fun getAudioEffectInteractor(): AudioEffectsInteractor = audioEffects

    override fun getPlayerSetting(): PlayerSetting = playerSetting


    private fun clearNotificationManagerAndUnbindService() {
        notificationManager.setPlayer(null)
        if (isServiceBound) {
            context.unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    private var newtonePlayerListener: NewtonePlayerListener? = null

    fun setNewtonePlayerListener(listener: NewtonePlayerListener?) {
        newtonePlayerListener = listener
    }

    interface NewtonePlayerListener {
        fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean)
        fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean)
    }

    private val playerNotificationListener: PlayerNotificationManager.NotificationListener =
        object : PlayerNotificationManager.NotificationListener {

            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                newtonePlayerListener?.onNotificationPosted(
                    notificationId,
                    notification,
                    ongoing
                )
            }

            override fun onNotificationCancelled(
                notificationId: Int,
                dismissedByUser: Boolean,
            ) {
                newtonePlayerListener?.onNotificationCancelled(notificationId, dismissedByUser)
                clearNotificationManagerAndUnbindService()
            }
        }


    private var isServiceBound = false
    private val serviceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            notificationManager.setPlayer(exoPlayer)
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    private inner class PlayerListener : Player.EventListener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
                val trackIds = trackIds
                currentTracklistUpdate.onNext(trackIds)
                settingSaver.savePlaylist(trackIds, exoPlayer.currentWindowIndex)
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            if (playWhenReady != isPlayingObserver.value) {
                isPlayingObserver.onNext(playWhenReady)
                settingSaver.playbackPosition = exoPlayer.currentPosition
                runServiceIfNeeded(playWhenReady)
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                onQueueEnded()
            }
        }

        private fun runServiceIfNeeded(isPlaying: Boolean) {
            if (isPlaying && !isServiceBound) {
                val intent = Intent(context, PlayerService::class.java)
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                isServiceBound = true
            }
        }

        private fun onQueueEnded() {
            pause()
            seekTo(0)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            refreshCurrentTrack()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            when (repeatMode) {
                Player.REPEAT_MODE_OFF -> repeatModeObserver.onNext(RepeatMode.OFF)
                Player.REPEAT_MODE_ALL -> repeatModeObserver.onNext(RepeatMode.ALL)
                Player.REPEAT_MODE_ONE -> repeatModeObserver.onNext(RepeatMode.ONE)
                else -> throw IllegalArgumentException("Repeat mode: $repeatMode")
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            shuffleModeObserver.onNext(shuffleModeEnabled)
        }

        private fun refreshCurrentTrack() {
            if (exoPlayer.mediaItemCount == 0) {
                currentTrackIdObserver.onNext(EmptyItems.NO_TRACK.id)
            } else {
                currentTrackIdObserver.onNext(currentTrackId())

                settingSaver.savedWindowIndex = exoPlayer.currentWindowIndex
                settingSaver.playbackPosition = exoPlayer.currentPosition
            }
        }
    }


    private lateinit var defaultNotificationAlbumArt: Bitmap

    fun setDefaultNotificationAlbumArt(bitmap: Bitmap) {
        defaultNotificationAlbumArt = bitmap
        notificationManager.invalidate()
    }


    private inner class PlayerMediaDescriptorAdapter :
        PlayerNotificationManager.MediaDescriptionAdapter {
        private var currentTrack: Track = EmptyItems.NO_TRACK

        private fun getCurrentTrack(player: Player): Track {
            if (player.mediaItemCount == 0) {
                currentTrack = EmptyItems.NO_TRACK
                return currentTrack
            }

            val currentTrackId = currentTrackId()

            if (currentTrackId != currentTrack.id) {
                currentTrack = trackRepo.getById(currentTrackId)
                    .onErrorReturnItem(EmptyItems.NO_TRACK)
                    .blockingGet()
            }

            return currentTrack
        }

        override fun getCurrentContentTitle(player: Player): String =
            getCurrentTrack(player).title

        override fun createCurrentContentIntent(player: Player): PendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                notificationClickIntent,
                PendingIntent.FLAG_MUTABLE,
            )

        override fun getCurrentContentText(player: Player): String =
            getCurrentTrack(player).run { "$artistName - $albumTitle" }

        override fun getCurrentLargeIcon(
            player: Player,
            bitmapCallback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            if (!playerSetting.isNotificationArtworkShow) {
                return null
            }

            val track = getCurrentTrack(player)
            return track.getArtImage<Bitmap>() ?: defaultNotificationAlbumArt
        }
    }

}
