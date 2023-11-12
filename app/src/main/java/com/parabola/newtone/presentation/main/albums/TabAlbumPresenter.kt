package com.parabola.newtone.presentation.main.albums

import com.parabola.domain.interactor.AlbumInteractor
import com.parabola.domain.model.Track
import com.parabola.domain.repository.AlbumRepository
import com.parabola.domain.repository.SortingRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.presentation.base.BasePresenter
import com.parabola.newtone.presentation.router.MainRouter
import com.parabola.newtone.util.Observables
import io.reactivex.Observable
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class TabAlbumPresenter(
    appComponent: AppComponent,
) : BasePresenter<TabAlbumView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var albumInteractor: AlbumInteractor

    @Inject
    lateinit var sortingRepo: SortingRepository

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var useCases: TabAlbumScreenUseCases

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        observerAllAlbums()
        observeAllAlbumsSorting()
        observeAlbumItemViewUpdates()
        observeTrackDeleting()
    }


    private fun observerAllAlbums() {
        albumInteractor.observeAllAlbumsUpdates()
            .schedule(onNext = viewState::refreshAlbums)
    }

    private fun observeAllAlbumsSorting() {
        sortingRepo.observeAllAlbumsSorting() //включаем/отключаем показ секции в списке, если отсортирован по названию
            .map { sorting -> sorting == AlbumRepository.Sorting.BY_TITLE }
            .schedule(onNext = viewState::setSectionShowing)
    }

    private fun observeTrackDeleting() {
        trackRepo.observeTrackDeleting()
            .flatMapSingle { albumInteractor.all }
            .schedule(onNext = viewState::refreshAlbums)
    }

    private fun observeAlbumItemViewUpdates() {
        Observables
            .combineLatest(
                viewSettingsInteractor.observeAlbumItemViewUpdates(),
                viewSettingsInteractor.observeIsItemDividerShowed(),
            )
            .schedule(
                onNext = { (albumItemView, isItemDividerShowed) ->
                    viewState.setAlbumViewSettings(albumItemView)
                    viewState.setItemDividerShowing(isItemDividerShowed && albumItemView.viewType == AlbumViewType.LIST)
                },
            )
    }

    fun onItemClick(albumId: Int) {
        router.openAlbum(albumId)
    }

    fun onClickMenuShuffle(albumId: Int) {
        useCases.shuffleAlbum(albumId)
            .schedule()
    }

    fun onClickMenuAddToPlaylist(albumId: Int) {
        trackRepo.getByAlbum(albumId)
            .flatMapObservable { albumTracks -> Observable.fromIterable(albumTracks) }
            .map(Track::getId)
            .toList()
            .map { trackIds -> trackIds.toIntArray() }
            .schedule(onSuccess = router::openAddToPlaylistDialog)
    }

}
