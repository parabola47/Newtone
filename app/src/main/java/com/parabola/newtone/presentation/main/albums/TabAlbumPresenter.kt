package com.parabola.newtone.presentation.main.albums

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.AlbumInteractor
import com.parabola.domain.interactor.type.Irrelevant
import com.parabola.domain.model.Track
import com.parabola.domain.repository.AlbumRepository
import com.parabola.domain.repository.SortingRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType
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
class TabAlbumPresenter(appComponent: AppComponent) : MvpPresenter<TabAlbumView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var albumInteractor: AlbumInteractor

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
            observerAllAlbums(),
            observeAllAlbumsSorting(),
            observeAlbumItemViewUpdates(),
            observeTrackDeleting()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun observerAllAlbums(): Disposable {
        return albumInteractor.observeAllAlbumsUpdates()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(viewState::refreshAlbums)
    }

    private fun observeAllAlbumsSorting(): Disposable {
        return sortingRepo.observeAllAlbumsSorting() //включаем/отключаем показ секции в списке, если отсортирован по названию
            .map { it == AlbumRepository.Sorting.BY_TITLE }
            .subscribe(viewState::setSectionShowing)
    }

    private fun observeTrackDeleting(): Disposable {
        return trackRepo.observeTrackDeleting()
            .flatMapSingle { albumInteractor.all }
            .observeOn(schedulers.ui())
            .subscribe(viewState::refreshAlbums)
    }

    private fun observeAlbumItemViewUpdates(): Disposable {
        return Observable.combineLatest(
            viewSettingsInteractor.observeAlbumItemViewUpdates(),
            viewSettingsInteractor.observeIsItemDividerShowed()
        ) { albumItemView, isItemDividerShowed ->
            viewState.setAlbumViewSettings(albumItemView)
            viewState.setItemDividerShowing(isItemDividerShowed && albumItemView.viewType == AlbumViewType.LIST)

            return@combineLatest Irrelevant.INSTANCE
        }
            .subscribe()
    }


    fun onItemClick(albumId: Int) {
        router.openAlbum(albumId)
    }

    fun onClickMenuShuffle(albumId: Int) {
        albumInteractor.shuffleAlbum(albumId)
    }

    fun onClickMenuAddToPlaylist(albumId: Int) {
        trackRepo.getByAlbum(albumId)
            .flatMapObservable { albumTracks -> Observable.fromIterable(albumTracks) }
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
