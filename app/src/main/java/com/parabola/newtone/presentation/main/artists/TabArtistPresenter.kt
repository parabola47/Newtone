package com.parabola.newtone.presentation.main.artists

import com.parabola.domain.interactor.ArtistInteractor
import com.parabola.domain.model.Track
import com.parabola.domain.repository.ArtistRepository
import com.parabola.domain.repository.SortingRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.presentation.base.BasePresenter
import com.parabola.newtone.presentation.router.MainRouter
import io.reactivex.Observable
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class TabArtistPresenter(
    appComponent: AppComponent,
) : BasePresenter<TabArtistView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var artistInteractor: ArtistInteractor

    @Inject
    lateinit var sortingRepo: SortingRepository

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var useCases: TabArtistScreenUseCases

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        observeAllArtists()
        observeAllArtistsSorting()
        observeTrackItemViewUpdates()
        observeIsItemDividerShowed()
        observeTrackDeleting()
    }

    private fun observeAllArtists() {
        artistInteractor.observeAllArtistsUpdates()
            .schedule(onNext = viewState::refreshArtists)
    }

    private fun observeAllArtistsSorting() {
        sortingRepo.observeAllArtistsSorting() //включаем/отключаем показ секции в списке, если отсортирован по имени
            .map { it == ArtistRepository.Sorting.BY_NAME }
            .schedule(onNext = viewState::setSectionShowing)
    }

    private fun observeTrackItemViewUpdates() {
        viewSettingsInteractor.observeArtistItemViewUpdates()
            .schedule(onNext = viewState::setItemViewSettings)
    }

    private fun observeIsItemDividerShowed() {
        viewSettingsInteractor.observeIsItemDividerShowed()
            .schedule(onNext = viewState::setItemDividerShowing)
    }

    private fun observeTrackDeleting() {
        trackRepo.observeTrackDeleting()
            .flatMapSingle { artistInteractor.all }
            .schedule(onNext = viewState::refreshArtists)
    }


    fun onItemClick(artistId: Int) {
        router.openArtist(artistId)
    }

    fun onClickMenuShuffle(artistId: Int) {
        useCases.shuffleArtist(artistId)
            .schedule()
    }

    fun onClickMenuAddToPlaylist(artistId: Int) {
        trackRepo.getByArtist(artistId)
            .flatMapObservable { artistTracks -> Observable.fromIterable(artistTracks) }
            .map(Track::getId)
            .toList()
            .map { trackIds -> trackIds.toIntArray() }
            .schedule(onSuccess = router::openAddToPlaylistDialog)
    }

}
