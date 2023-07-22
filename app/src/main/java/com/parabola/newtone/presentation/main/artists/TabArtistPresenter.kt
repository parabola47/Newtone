package com.parabola.newtone.presentation.main.artists

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.ArtistInteractor
import com.parabola.domain.model.Track
import com.parabola.domain.repository.ArtistRepository
import com.parabola.domain.repository.SortingRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.presentation.router.MainRouter
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.ConsumerSingleObserver
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class TabArtistPresenter(appComponent: AppComponent) : MvpPresenter<TabArtistView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var artistInteractor: ArtistInteractor

    @Inject
    lateinit var sortingRepo: SortingRepository

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    @Inject
    lateinit var schedulers: SchedulerProvider

    private val disposables = CompositeDisposable()


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        disposables.addAll(
            observeAllArtists(),
            observeAllArtistsSorting(),
            observeTrackItemViewUpdates(),
            observeIsItemDividerShowed(),
            observeTrackDeleting()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun observeAllArtists(): Disposable {
        return artistInteractor.observeAllArtistsUpdates()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(viewState::refreshArtists)
    }

    private fun observeAllArtistsSorting(): Disposable {
        return sortingRepo.observeAllArtistsSorting() //включаем/отключаем показ секции в списке, если отсортирован по имени
            .map { it == ArtistRepository.Sorting.BY_NAME }
            .subscribe(viewState::setSectionShowing)
    }

    private fun observeTrackItemViewUpdates(): Disposable {
        return viewSettingsInteractor.observeArtistItemViewUpdates()
            .subscribe(viewState::setItemViewSettings)
    }

    private fun observeIsItemDividerShowed(): Disposable {
        return viewSettingsInteractor.observeIsItemDividerShowed()
            .subscribe(viewState::setItemDividerShowing)
    }

    private fun observeTrackDeleting(): Disposable {
        return trackRepo.observeTrackDeleting()
            .flatMapSingle { artistInteractor.all }
            .observeOn(schedulers.ui())
            .subscribe(viewState::refreshArtists)
    }


    fun onItemClick(artistId: Int) {
        router.openArtist(artistId)
    }

    fun onClickMenuShuffle(artistId: Int) {
        artistInteractor.shuffleArtist(artistId)
    }

    fun onClickMenuAddToPlaylist(artistId: Int) {
        trackRepo.getByArtist(artistId)
            .flatMapObservable { artistTracks -> Observable.fromIterable(artistTracks) }
            .map(Track::getId)
            .toList()
            .map { trackIds -> trackIds.toIntArray() }
            .subscribe(
                ConsumerSingleObserver(
                    router::openAddToPlaylistDialog,
                    Functions.ERROR_CONSUMER
                )
            )
    }

}
