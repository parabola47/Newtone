package com.parabola.newtone.mvp.presenter

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.AlbumInteractor
import com.parabola.domain.interactor.type.Irrelevant
import com.parabola.domain.model.Track
import com.parabola.domain.repository.ArtistRepository
import com.parabola.domain.repository.SortingRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.mvp.view.ArtistView
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.ConsumerSingleObserver
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class ArtistPresenter(appComponent: AppComponent, private val artistId: Int) :
    MvpPresenter<ArtistView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var artistRepo: ArtistRepository

    @Inject
    lateinit var albumInteractor: AlbumInteractor

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var sortingRepo: SortingRepository

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
            loadArtist(),
            observeAlbumItemViewUpdates(),
            observeArtistAlbumsSorting(),
            observeTrackDeleting()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun loadArtist(): Disposable {
        return artistRepo.getById(artistId)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe { artist ->
                viewState.setArtistName(artist.name)
                viewState.setTracksCount(artist.tracksCount)
                viewState.setAlbumsCount(artist.albumsCount)
            }
    }

    private fun observeArtistAlbumsSorting(): Disposable {
        return sortingRepo.observeArtistAlbumsSorting()
            .flatMapSingle { albumInteractor.getByArtist(artistId) }
            // ожидаем пока прогрузится анимация входа
            .doOnNext {
                @Suppress("ControlFlowWithEmptyBody")
                while (!enterSlideAnimationEnded);
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(viewState::refreshAlbums)
    }

    private fun observeTrackDeleting(): Disposable {
        return trackRepo.observeTrackDeleting()
            .flatMapSingle {
                Single.zip(artistRepo.getById(artistId), albumInteractor.getByArtist(artistId))
                { artist, albums -> Pair(artist, albums) }
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(
                { (artist, albums) ->
                    viewState.setTracksCount(artist.tracksCount)
                    viewState.setAlbumsCount(artist.albumsCount)
                    viewState.refreshAlbums(albums)
                }
            ) { router.backToRoot() }
    }

    private fun observeAlbumItemViewUpdates(): Disposable {
        return Observable.combineLatest(
            viewSettingsInteractor.observeAlbumItemViewUpdates(),
            viewSettingsInteractor.observeIsItemDividerShowed(),
        ) { albumItemView, isItemDividerShowed ->
            viewState.setAlbumViewSettings(albumItemView)
            viewState.setItemDividerShowing(
                isItemDividerShowed && albumItemView.viewType == AlbumItemView.AlbumViewType.LIST
            )
            Irrelevant.INSTANCE
        }
            .subscribe()
    }


    fun onClickAllTracks() {
        router.openArtistTracks(artistId)
    }

    fun onClickBack() {
        router.goBack()
    }

    @Volatile
    private var enterSlideAnimationEnded = false

    fun onEnterSlideAnimationEnded() {
        enterSlideAnimationEnded = true
    }

    fun onAlbumItemClick(albumId: Int) {
        router.openAlbum(albumId)
    }


    fun onClickMenuShuffle(albumId: Int) {
        albumInteractor.shuffleAlbum(albumId)
    }

    fun onClickMenuAddToPlaylist(albumId: Int) {
        trackRepo.getByAlbum(albumId)
            .flatMapObservable { Observable.fromIterable(it) }
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
