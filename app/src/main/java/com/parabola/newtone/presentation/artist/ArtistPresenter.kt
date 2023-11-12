package com.parabola.newtone.presentation.artist

import com.parabola.domain.interactor.AlbumInteractor
import com.parabola.domain.model.Track
import com.parabola.domain.repository.ArtistRepository
import com.parabola.domain.repository.SortingRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.presentation.base.BasePresenter
import com.parabola.newtone.presentation.router.MainRouter
import com.parabola.newtone.util.Observables
import io.reactivex.Observable
import io.reactivex.Single
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class ArtistPresenter(
    appComponent: AppComponent,
    private val artistId: Int,
) : BasePresenter<ArtistView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var artistRepo: ArtistRepository

    @Inject
    lateinit var albumInteractor: AlbumInteractor

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var useCases: ArtistScreenUseCases

    @Inject
    lateinit var sortingRepo: SortingRepository

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        loadArtist()
        observeAlbumItemViewUpdates()
        observeArtistAlbumsSorting()
        observeTrackDeleting()
    }

    private fun loadArtist() {
        artistRepo.getById(artistId)
            .schedule(
                onSuccess = { artist ->
                    viewState.setArtistName(artist.name)
                    viewState.setTracksCount(artist.tracksCount)
                    viewState.setAlbumsCount(artist.albumsCount)
                },
            )
    }

    private fun observeArtistAlbumsSorting() {
        sortingRepo.observeArtistAlbumsSorting()
            .flatMapSingle { albumInteractor.getByArtist(artistId) }
            // ожидаем пока прогрузится анимация входа
            .doOnNext {
                @Suppress("ControlFlowWithEmptyBody")
                while (!enterSlideAnimationEnded);
            }
            .schedule(onNext = viewState::refreshAlbums)
    }

    private fun observeTrackDeleting() {
        trackRepo.observeTrackDeleting()
            .flatMapSingle {
                Single.zip(artistRepo.getById(artistId), albumInteractor.getByArtist(artistId))
                { artist, albums -> Pair(artist, albums) }
            }
            .schedule(
                onNext = { (artist, albums) ->
                    viewState.setTracksCount(artist.tracksCount)
                    viewState.setAlbumsCount(artist.albumsCount)
                    viewState.refreshAlbums(albums)
                },
                onError = { router.backToRoot() },
            )
    }

    private fun observeAlbumItemViewUpdates() {
        Observables.combineLatest(
            viewSettingsInteractor.observeAlbumItemViewUpdates(),
            viewSettingsInteractor.observeIsItemDividerShowed(),
        )
            .schedule(
                onNext = { (albumItemView, isItemDividerShowed) ->
                    viewState.setAlbumViewSettings(albumItemView)
                    viewState.setItemDividerShowing(
                        isItemDividerShowed && albumItemView.viewType == AlbumItemView.AlbumViewType.LIST
                    )
                }
            )
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
        useCases.shuffleAlbum(albumId)
            .schedule()
    }

    fun onClickMenuAddToPlaylist(albumId: Int) {
        trackRepo.getByAlbum(albumId)
            .flatMapObservable { Observable.fromIterable(it) }
            .map(Track::getId)
            .toList()
            .map { trackIds -> trackIds.toIntArray() }
            .schedule(onSuccess = router::openAddToPlaylistDialog)
    }

}
