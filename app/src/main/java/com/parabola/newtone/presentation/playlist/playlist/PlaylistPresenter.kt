package com.parabola.newtone.presentation.playlist.playlist

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.TrackInteractor
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.model.Playlist
import com.parabola.domain.model.Track
import com.parabola.domain.repository.PlaylistRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.utils.EmptyItems
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class PlaylistPresenter(
    appComponent: AppComponent,
    private val playlistId: Int
) : MvpPresenter<PlaylistView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var playerInteractor: PlayerInteractor

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    @Inject
    lateinit var playlistRepo: PlaylistRepository

    @Inject
    lateinit var trackInteractor: TrackInteractor

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var schedulers: SchedulerProvider

    private val disposables = CompositeDisposable()

    private var currentTrackId = EmptyItems.NO_TRACK.id


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        viewState.setPlaylistChangerActivation(isPlaylistChangerActivated)

        disposables.addAll(
            refreshPlaylistInfo(),
            observePlaylistUpdates(),
            observeTrackItemViewUpdates(),
            observeIsItemDividerShowed(),
            observeCurrentTrackUpdates(),
            observeTrackDeleting()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun refreshPlaylistInfo(): Disposable {
        return playlistRepo.getById(playlistId)
            .map(Playlist::getTitle)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(viewState::setPlaylistTitle)
    }

    private fun observePlaylistUpdates(): Disposable {
        return playlistRepo.observePlaylistsUpdates()
            .flatMapSingle { trackInteractor.getByPlaylist(playlistId) }
            // ожидаем пока прогрузится анимация входа
            .doOnNext {
                @Suppress("ControlFlowWithEmptyBody")
                while (!enterSlideAnimationEnded);
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe { playlistTracks ->
                viewState.setTracksCount(playlistTracks.size)
                viewState.refreshTracks(playlistTracks)
                viewState.setCurrentTrack(currentTrackId)
            }
    }

    private fun observeTrackItemViewUpdates(): Disposable {
        return viewSettingsInteractor.observeTrackItemViewUpdates()
            .subscribe(viewState::setItemViewSettings)
    }

    private fun observeIsItemDividerShowed(): Disposable {
        return viewSettingsInteractor.observeIsItemDividerShowed()
            .subscribe(viewState::setItemDividerShowing)
    }

    private fun observeTrackDeleting(): Disposable {
        return trackRepo.observeTrackDeleting()
            .doOnNext { removedTrackId ->
                if (removedTrackId == currentTrackId)
                    currentTrackId = EmptyItems.NO_TRACK.id
            }
            .flatMapSingle { trackInteractor.getByPlaylist(playlistId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(
                { playlistTracks ->
                    if (playlistTracks.isNotEmpty()) {
                        viewState.setTracksCount(playlistTracks.size)
                        viewState.refreshTracks(playlistTracks)
                        viewState.setCurrentTrack(currentTrackId)
                    } else router.backToRoot()
                },
                { router.backToRoot() },
            )
    }

    private fun observeCurrentTrackUpdates(): Disposable {
        return playerInteractor.onChangeCurrentTrackId()
            .doOnNext { currentTrackId = it }
            .subscribe(viewState::setCurrentTrack)
    }


    fun onClickBack() {
        router.goBack()
    }

    private var isPlaylistChangerActivated = false

    fun onClickDragSwitcher() {
        isPlaylistChangerActivated = !isPlaylistChangerActivated
        viewState.setPlaylistChangerActivation(isPlaylistChangerActivated)
    }

    @Volatile
    private var enterSlideAnimationEnded = false

    fun onEnterSlideAnimationEnded() {
        enterSlideAnimationEnded = true
    }

    fun onClickTrackItem(tracks: List<Track>, selectedPosition: Int) {
        playerInteractor.start(tracks, selectedPosition)
    }

    fun onClickMenuPlay(tracks: List<Track>, selectedPosition: Int) {
        playerInteractor.start(tracks, selectedPosition)
    }

    fun onClickMenuRemoveFromCurrentPlaylist(trackId: Int) {
        playlistRepo.removeTrack(playlistId, trackId)
            .subscribe()
    }

    fun onClickMenuAddToFavourites(trackId: Int) {
        trackRepo.addToFavourites(trackId)
    }

    fun onClickMenuRemoveFromFavourites(trackId: Int) {
        trackRepo.removeFromFavourites(trackId)
    }

    fun onClickMenuShareTrack(selectedTrack: Track) {
        router.openShareTrack(selectedTrack.filePath)
    }

    fun onClickMenuDeleteTrack(trackId: Int) {
        router.openDeleteTrackDialog(trackId)
    }

    fun onClickMenuAdditionalInfo(trackId: Int) {
        router.openTrackAdditionInfo(trackId)
    }

    fun onRemoveItem(trackId: Int) {
        playlistRepo.removeTrack(playlistId, trackId)
            .subscribeOn(schedulers.io())
            .subscribe()
    }

    fun onMoveItem(positionFrom: Int, positionTo: Int) {
        playlistRepo.moveTrack(playlistId, positionFrom, positionTo)
            .subscribeOn(schedulers.io())
            .subscribe()
    }

}
