package com.parabola.newtone.presentation.player

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.model.Track
import com.parabola.domain.repository.ResourceRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.utils.EmptyItems
import com.parabola.newtone.R
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.ui.router.MainRouter
import com.parabola.newtone.util.TimeFormatterTool
import com.parabola.newtone.util.TimeFormatterTool.formatMillisecondsToMinutes
import com.parabola.sleep_timer_feature.SleepTimerInteractor
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.ConsumerSingleObserver
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class PlayerPresenter(appComponent: AppComponent) : MvpPresenter<PlayerView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var playerInteractor: PlayerInteractor

    @Inject
    lateinit var timerInteractor: SleepTimerInteractor

    @Inject
    lateinit var resourceRepo: ResourceRepository

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var schedulers: SchedulerProvider

    private val disposables = CompositeDisposable()

    private var currentTrackId = EmptyItems.NO_TRACK.id
    private var isSeekbarPressed = false


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        disposables.addAll(
            observeTracklistChanging(),
            observeCurrentTrack(),
            observeBottomSlidePanelOffset(),
            observeBottomSlidePanelState(),
            observePlayerState(),
            observePlaybackPosition(),
            observeTimerState(),
            observeFavouritesChanged(),
            observeRepeatModeEnabling(),
            observeShuffleModeEnabling()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun observeRepeatModeEnabling(): Disposable {
        return playerInteractor.onRepeatModeChange()
            .subscribe(viewState::setRepeatMode)
    }

    private var enableSlideScrolling = false

    private fun observeShuffleModeEnabling(): Disposable {
        return playerInteractor.onShuffleModeChange()
            .doOnNext { isShuffleEnabled ->
                enableSlideScrolling = !isShuffleEnabled
                viewState.setViewPagerSlide(!enableSlideScrolling)
            }
            .subscribe(viewState::setShuffleEnabling)
    }

    // Слушаем обновления на избранные, в случае, если в избранные добавлен/удалён текущий трек,
    // то меняем иконку
    private fun observeFavouritesChanged(): Disposable {
        return trackRepo.observeFavouritesChanged()
            //Пропускаем, если id текущего трека равен пустому треку
            .filter { currentTrackId != EmptyItems.NO_TRACK.id }
            .map { trackRepo.isFavourite(currentTrackId) }
            .subscribe(viewState::setIsFavourite)
    }

    private fun observeCurrentTrack(): Disposable {
        return playerInteractor.onChangeCurrentTrackId()
            .doOnNext { currentTrackId = it }
            //Пропускаем, если id текущего трека неверен
            .filter { currentTrackId -> currentTrackId != EmptyItems.NO_TRACK.id }
            .flatMapSingle(trackRepo::getById)
            .observeOn(schedulers.ui())
            .subscribe(
                { track ->
                    viewState.apply {
                        setArtist(track.artistName)
                        setAlbum(track.albumTitle)
                        setTitle(track.title)
                        val durationFormatted = formatMillisecondsToMinutes(track.durationMs)
                        setDurationText(durationFormatted)
                        setDurationMs(track.durationMs.toInt())
                        setIsFavourite(track.isFavourite)
                        setAlbumImagePosition(
                            playerInteractor.currentTrackPosition(),
                            enableSlideScrolling
                        )
                    }
                },
                Functions.ERROR_CONSUMER,
            )
    }

    private fun observePlayerState(): Disposable {
        return playerInteractor.onChangePlayingState()
            .observeOn(schedulers.ui())
            .subscribe { isPlaying ->
                if (isPlaying) viewState.setPlaybackButtonAsPause()
                else viewState.setPlaybackButtonAsPlay()
            }
    }

    private fun observePlaybackPosition(): Disposable {
        return playerInteractor.onChangePlaybackPosition()
            .filter { !isSeekbarPressed }
            .subscribe { currentTimeMs -> viewState.setCurrentTimeMs(currentTimeMs.toInt()) }
    }

    private fun observeTimerState(): Disposable {
        return timerInteractor.observeIsTimerRunning()
            .observeOn(schedulers.ui())
            .subscribe(viewState::setTimerButtonVisibility)
    }

    private fun observeTracklistChanging(): Disposable {
        return playerInteractor.onTracklistChanged()
            .flatMapSingle(trackRepo::getByIds)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe { tracks ->
                viewState.refreshTracks(tracks)
                viewState.setAlbumImagePosition(playerInteractor.currentTrackPosition(), false)
            }
    }

    private fun observeBottomSlidePanelOffset(): Disposable {
        return router.observeSlidePanelOffset()
            .subscribe { offset ->
                viewState.setTrackSettingsRotation(360 * offset) //переводим в угол поворота
                viewState.setRootViewOpacity(offset)
            }
    }

    private fun observeBottomSlidePanelState(): Disposable {
        return router.observeSlidePanelState()
            .flatMap(Function<PanelState, ObservableSource<Boolean>> { state ->
                when (state) {
                    PanelState.EXPANDED, PanelState.DRAGGING -> return@Function Observable.just(true)
                    PanelState.COLLAPSED -> return@Function Observable.just(false)
                    else -> return@Function Observable.empty<Boolean>()
                }
            })
            .subscribe(viewState::setRootViewVisibility)
    }


    //    TOP MENU
    fun onClickQueue() {
        router.openQueueFromBackStackIfAvailable()
        router.collapseBottomSlider()
        router.goToTab(3, false)
    }

    fun onClickAudioEffects() {
        router.openAudioEffectsDialog()
    }

    fun onLongClickAudioEffects() {
        router.openEqPresetsSelectorDialog()
    }

    fun onClickDropDown() {
        router.collapseBottomSlider()
    }

    fun onClickFavourite() {
        trackRepo.toggleFavourite(currentTrackId)
    }

    fun onLongClickFavorite() {
        router.openFavouritesFromBackStackIfAvailable()
        router.collapseBottomSlider()
        router.goToTab(3, false)
    }

    fun onClickTimerButton() {
        if (timerInteractor.launched())
            router.openSleepTimerInfoDialog()
    }

    fun onLongClickTimerButton() {
        if (timerInteractor.launched()) {
            timerInteractor.remainingTimeToEnd()
                .onErrorReturnItem(0L)
                .map(TimeFormatterTool::formatMillisecondsToMinutes)
                .map { timeToEndFormatted ->
                    resourceRepo.getString(
                        R.string.time_to_end_sleep_info_dialog,
                        timeToEndFormatted,
                    )
                }
                .subscribe(
                    ConsumerSingleObserver(
                        router::showToast,
                        Functions.ERROR_CONSUMER,
                    )
                )
        }
    }


    //    TRACK INFO
    fun onSwipeImage(lastPosition: Int) {
        val currentTrackPosition = playerInteractor.currentTrackPosition()
        if (lastPosition == currentTrackPosition)
            return

        if (lastPosition > currentTrackPosition) playerInteractor.next()
        else playerInteractor.previous()
    }

    fun onClickArtist() {
        trackRepo.getById(currentTrackId)
            .subscribe(
                ConsumerSingleObserver(
                    { track ->
                        router.openArtistFromBackStackIfAvailable(track.artistId)
                        router.collapseBottomSlider()
                        router.goToTab(0, false)
                        router.goToArtistInTab(track.artistId)
                    },
                    Functions.ERROR_CONSUMER,
                )
            )
    }

    fun onClickAlbum() {
        trackRepo.getById(currentTrackId)
            .subscribe(
                ConsumerSingleObserver(
                    { track ->
                        router.openAlbumFromBackStackIfAvailable(track.albumId)
                        router.collapseBottomSlider()
                        router.goToTab(1, false)
                        router.goToAlbumInTab(track.albumId)
                    },
                    Functions.ERROR_CONSUMER,
                )
            )
    }

    fun onClickTrackTitle() {
        router.backToRoot()
        router.goToTab(2, false)
        router.scrollOnTabTrackToCurrentTrack()
        router.collapseBottomSlider()
    }


    //    PLAYER ACTION
    fun onClickLoop() {
        playerInteractor.toggleRepeatMode()
    }

    fun onClickPrevTrack() {
        playerInteractor.previous()
    }

    fun onClickPlayButton() {
        playerInteractor.toggle()
    }

    fun onClickNextTrack() {
        playerInteractor.next()
    }

    fun onClickShuffle() {
        playerInteractor.toggleShuffleMode()
    }


    //    SEEKBAR
    fun onStartSeekbarPressed() {
        isSeekbarPressed = true
    }

    fun onStopSeekbarPressed(progress: Int) {
        isSeekbarPressed = false
        playerInteractor.seekTo(progress.toLong())
    }


    //    MENU
    fun onClickMenuAddTrackToPlaylist() {
        if (currentTrackId != EmptyItems.NO_TRACK.id) {
            router.openAddToPlaylistDialog(currentTrackId)
        }
    }

    fun onClickMenuTimer() {
        if (timerInteractor.launched())
            router.openSleepTimerInfoDialog()
        else router.openStartSleepTimerDialog()
    }

    fun onClickMenuLyrics() {
        trackRepo.getById(currentTrackId)
            .subscribe(
                ConsumerSingleObserver(
                    router::openLyricsSearch,
                    Functions.ERROR_CONSUMER,
                )
            )
    }

    fun onClickMenuShareTrack() {
        trackRepo.getById(currentTrackId)
            .map(Track::getFilePath)
            .subscribe(
                ConsumerSingleObserver(
                    router::openShareTrack,
                    Functions.ERROR_CONSUMER,
                )
            )
    }

    fun onClickMenuAdditionalInfo() {
        router.openTrackAdditionInfo(currentTrackId)
    }

    fun onClickMenuDelete() {
        router.openDeleteTrackDialog(currentTrackId)
    }

    fun onClickMenuSettings() {
        router.openSettingsIfAvailable()
        router.collapseBottomSlider()
    }

}
