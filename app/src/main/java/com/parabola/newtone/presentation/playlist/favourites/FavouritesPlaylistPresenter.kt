package com.parabola.newtone.presentation.playlist.favourites

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.TrackInteractor
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.model.Track
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.utils.EmptyItems
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.presentation.router.MainRouter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.ConsumerSingleObserver
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class FavouritesPlaylistPresenter(component: AppComponent) :
    MvpPresenter<FavouritesPlaylistView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var trackInteractor: TrackInteractor

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var playerInteractor: PlayerInteractor

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    @Inject
    lateinit var schedulers: SchedulerProvider

    private var currentTrackId = EmptyItems.NO_TRACK.id

    private val disposables = CompositeDisposable()


    init {
        component.inject(this)
    }


    override fun onFirstViewAttach() {
        viewState.setPlaylistChangerActivation(isPlaylistChangerActivated)
        trackInteractor.favourites
            // ожидаем пока прогрузится анимация входа
            .doOnSubscribe {
                @Suppress("ControlFlowWithEmptyBody")
                while (!enterSlideAnimationEnded);
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(
                ConsumerSingleObserver(
                    { tracks ->
                        viewState.refreshTracks(tracks)
                        viewState.setCurrentTrack(playerInteractor.currentTrackId())
                    },
                    Functions.ERROR_CONSUMER
                )
            )
        disposables.addAll(
            observeFavouritesChanged(),
            observeTrackItemViewUpdates(),
            observeIsItemDividerShowed(),
            observeCurrentTrackChanged(),
            observeTrackDeleting()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun observeFavouritesChanged(): Disposable {
        return trackRepo.observeFavouritesChanged()
            .flatMapSingle { trackInteractor.favourites }
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

    private fun observeCurrentTrackChanged(): Disposable {
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
            .flatMapSingle { trackInteractor.favourites }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe { tracks ->
                viewState.refreshTracks(tracks)
                viewState.setCurrentTrack(currentTrackId)
            }
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

    fun onClickMenuAddToPlaylist(trackId: Int) {
        router.openAddToPlaylistDialog(trackId)
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
        trackRepo.removeFromFavourites(trackId)
    }

    fun onMoveItem(positionFrom: Int, positionTo: Int) {
        trackRepo.moveFavouriteTrack(positionFrom, positionTo)
    }

}
