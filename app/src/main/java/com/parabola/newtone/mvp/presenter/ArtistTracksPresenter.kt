package com.parabola.newtone.mvp.presenter

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.TrackInteractor
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.model.Track
import com.parabola.domain.repository.ArtistRepository
import com.parabola.domain.repository.ResourceRepository
import com.parabola.domain.repository.SortingRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.utils.EmptyItems
import com.parabola.newtone.R
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.mvp.view.ArtistTracksView
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@InjectViewState
class ArtistTracksPresenter(appComponent: AppComponent, private val artistId: Int) :
    MvpPresenter<ArtistTracksView>() {

    @Volatile
    private var currentTrackId = EmptyItems.NO_TRACK.id

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var trackInteractor: TrackInteractor

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var artistRepository: ArtistRepository

    @Inject
    lateinit var playerInteractor: PlayerInteractor

    @Inject
    lateinit var sortingRepo: SortingRepository

    @Inject
    lateinit var resourceRepo: ResourceRepository

    @Inject
    lateinit var schedulers: SchedulerProvider

    private val disposables = CompositeDisposable()


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        disposables.addAll(
            loadArtist(),
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


    private fun loadArtist(): Disposable {
        return artistRepository.getById(artistId)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe { artist ->
                viewState.setArtistName(artist.name)
                updateTracksCount(artist.tracksCount)
            }
    }

    private fun observeCurrentTrack(): Disposable {
        return playerInteractor.onChangeCurrentTrackId()
            .doOnNext { currentTrackId = it }
            .subscribe(viewState::setCurrentTrack)
    }

    private fun observeSortingUpdates(): Disposable {
        val needToShowSection = AtomicBoolean(false)

        return sortingRepo.observeArtistTracksSorting()
            //включаем/отключаем показ секции в списке, если отсортирован по названию
            .doOnNext { sorting -> needToShowSection.set(sorting == TrackRepository.Sorting.BY_TITLE) }
            .flatMapSingle { trackInteractor.getByArtist(artistId) }
            // ожидаем пока прогрузится анимация входа
            .doOnNext {
                @Suppress("ControlFlowWithEmptyBody")
                while (!enterSlideAnimationEnded);
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe { tracks ->
                viewState.setSectionShowing(needToShowSection.get())
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
            .flatMapSingle { trackInteractor.getByArtist(artistId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe { tracks ->
                if (tracks.isNotEmpty()) {
                    viewState.refreshTracks(tracks)
                    viewState.setCurrentTrack(currentTrackId)
                    updateTracksCount(tracks.size)
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

    private fun updateTracksCount(tracksCount: Int) {
        val tracksCountStr = resourceRepo.getQuantityString(R.plurals.tracks_count, tracksCount)
        viewState!!.setTracksCountTxt(tracksCountStr)
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
