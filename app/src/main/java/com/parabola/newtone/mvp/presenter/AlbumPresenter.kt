package com.parabola.newtone.mvp.presenter

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.TrackInteractor
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.model.Track
import com.parabola.domain.repository.AlbumRepository
import com.parabola.domain.repository.ResourceRepository
import com.parabola.domain.repository.SortingRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.utils.EmptyItems
import com.parabola.newtone.R
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.mvp.view.AlbumView
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class AlbumPresenter(appComponent: AppComponent, private val albumId: Int) :
    MvpPresenter<AlbumView>() {

    @Volatile
    private var currentTrackId = EmptyItems.NO_TRACK.id

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var schedulers: SchedulerProvider

    @Inject
    lateinit var albumRepo: AlbumRepository

    @Inject
    lateinit var trackInteractor: TrackInteractor

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var playerInteractor: PlayerInteractor

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    @Inject
    lateinit var sortingRepo: SortingRepository

    @Inject
    lateinit var resourceRepo: ResourceRepository

    private val disposables = CompositeDisposable()


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        disposables.addAll(
            loadAlbum(),
            observeCurrentTrack(),
            observeSortingUpdates(),
            observeTrackItemViewUpdates(),
            observeIsItemDividerShowed(),
            observeTrackDeleting()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun loadAlbum(): Disposable {
        return albumRepo.getById(albumId)
            .doOnError {
                @Suppress("ControlFlowWithEmptyBody")
                while (!enterSlideAnimationEnded);
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe({ album ->
                viewState.setAlbumTitle(album.title)
                viewState.setAlbumArtist(album.artistName)
                viewState.setAlbumArt(album.getArtImage())
            }) {
                val toastText =
                    resourceRepo.getString(R.string.album_screen_album_load_error_toast)
                router.showToast(toastText)
                router.goBack()
            }
    }

    private fun observeCurrentTrack(): Disposable {
        return playerInteractor.onChangeCurrentTrackId()
            .doOnNext { currentTrackId = it }
            .subscribe(viewState::setCurrentTrack)
    }

    private fun observeSortingUpdates(): Disposable {
        return sortingRepo.observeAlbumTracksSorting()
            .flatMapSingle { trackInteractor.getByAlbum(albumId) }
            // ожидаем пока прогрузится анимация входа
            .doOnNext {
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

    private fun observeTrackDeleting(): Disposable {
        return trackRepo.observeTrackDeleting()
            .doOnNext { removedTrackId ->
                if (removedTrackId == currentTrackId)
                    currentTrackId = EmptyItems.NO_TRACK.id
            }
            .flatMapSingle { trackInteractor.getByAlbum(albumId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe { tracks: List<Track> ->
                if (tracks.isNotEmpty()) {
                    viewState.refreshTracks(tracks)
                    viewState.setCurrentTrack(currentTrackId)
                } else {
                    router.backToRoot()
                }
            }
    }


    fun onClickBack() {
        router.goBack()
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

    fun onClickMenuAddToFavourites(trackId: Int) {
        trackRepo.addToFavourites(trackId)
    }

    fun onClickMenuRemoveFromFavourites(trackId: Int) {
        trackRepo.removeFromFavourites(trackId)
    }

    fun onClickMenuShareTrack(track: Track) {
        router.openShareTrack(track.filePath)
    }

    fun onClickMenuDeleteTrack(trackId: Int) {
        router.openDeleteTrackDialog(trackId)
    }

    fun onClickMenuAdditionalInfo(trackId: Int) {
        router.openTrackAdditionInfo(trackId)
    }

}
