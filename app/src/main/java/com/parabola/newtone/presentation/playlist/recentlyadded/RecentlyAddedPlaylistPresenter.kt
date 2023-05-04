package com.parabola.newtone.presentation.playlist.recentlyadded

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.TrackInteractor
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.model.Track
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
class RecentlyAddedPlaylistPresenter(appComponent: AppComponent) :
    MvpPresenter<RecentlyAddedPlaylistView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var trackInteractor: TrackInteractor

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var schedulers: SchedulerProvider

    @Inject
    lateinit var playerInteractor: PlayerInteractor

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    private val disposables = CompositeDisposable()

    @Volatile
    private var currentTrackId = EmptyItems.NO_TRACK.id


    init {
        appComponent.inject(this)
    }


    public override fun onFirstViewAttach() {
        disposables.addAll(
            refreshPlaylists(),
            observeTrackItemViewUpdates(),
            observeIsItemDividerShowed(),
            observeCurrentTrack(),
            observeTrackDeleting()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun refreshPlaylists(): Disposable {
        return trackInteractor.recentlyAddedTracks
            // ожидаем пока прогрузится анимация входа
            .doOnSuccess {
                @Suppress("ControlFlowWithEmptyBody")
                while (!enterSlideAnimationEnded);
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe { tracks ->
                viewState.refreshTracks(tracks)
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

    private fun observeCurrentTrack(): Disposable {
        return playerInteractor.onChangeCurrentTrackId()
            .doOnNext { currentTrackId = it }
            .subscribe(viewState::setCurrentTrack)
    }

    private fun observeTrackDeleting(): Disposable {
        return trackRepo.observeTrackDeleting()
            .doOnNext { deletedTrackId ->
                if (deletedTrackId == currentTrackId)
                    currentTrackId = EmptyItems.NO_TRACK.id
            }
            .observeOn(schedulers.ui())
            .subscribe { deletedTrackId ->
                viewState.removeTrack(deletedTrackId)
                viewState.setCurrentTrack(currentTrackId)
            }
    }


    fun onClickTrackItem(tracks: List<Track>, selectedPosition: Int) {
        playerInteractor.start(tracks, selectedPosition)
    }

    fun onClickMenuPlay(tracks: List<Track>, selectedPosition: Int) {
        playerInteractor.start(tracks, selectedPosition)
    }


    fun onClickBack() {
        router.goBack()
    }

    @Volatile
    private var enterSlideAnimationEnded = false

    fun onEnterSlideAnimationEnded() {
        enterSlideAnimationEnded = true
    }

    fun onClickMenuAddToPlaylist(trackId: Int) {
        router.openAddToPlaylistDialog(trackId)
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

}
