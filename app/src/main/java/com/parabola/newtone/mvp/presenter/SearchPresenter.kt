package com.parabola.newtone.mvp.presenter

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.mvp.view.SearchFragmentView
import com.parabola.newtone.ui.router.MainRouter
import com.parabola.search_feature.SearchInteractor
import com.parabola.search_feature.SearchResult
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class SearchPresenter(appComponent: AppComponent) : MvpPresenter<SearchFragmentView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var playerInteractor: PlayerInteractor

    @Inject
    lateinit var searchInteractor: SearchInteractor

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    @Inject
    lateinit var schedulers: SchedulerProvider


    private val disposables = CompositeDisposable()

    private var querySearchDisposable: Disposable? = null


    private var lastQuery = ""


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        viewState.clearAllLists()
        viewState.focusOnSearchView()

        disposables.addAll(
            observeTrackItemViewUpdates(),
            observeIsItemDividerShowed(),
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun observeTrackItemViewUpdates(): Disposable {
        return viewSettingsInteractor.observeTrackItemViewUpdates()
            .subscribe(viewState::setTrackItemViewSettings)
    }

    private fun observeIsItemDividerShowed(): Disposable {
        return viewSettingsInteractor.observeIsItemDividerShowed()
            .subscribe(viewState::setItemDividerShowing)
    }


    fun onClickBackButton() {
        router.goBack()
    }

    fun onQueryTextSubmit(query: String) {
        var queryFormatted = query
        queryFormatted = queryFormatted.trim { it <= ' ' }

        if (queryFormatted == lastQuery)
            return

        lastQuery = queryFormatted

        querySearchDisposable?.dispose()

        querySearchDisposable = searchInteractor.search(queryFormatted)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .doOnSubscribe { viewState.setLoadDataProgressBarVisibility(true) }
            .doFinally { viewState.setLoadDataProgressBarVisibility(false) }
            .subscribe(::refreshAll)
            .also { disposables.add(it) }
    }

    private fun refreshAll(searchResult: SearchResult) {
        viewState.apply {
            refreshArtists(searchResult.artists)
            refreshAlbums(searchResult.albums)
            refreshTracks(searchResult.tracks)
            refreshPlaylists(searchResult.playlists)
        }
    }

    fun onClearText() {
        viewState.clearAllLists()
    }

    fun onClickArtistItem(artistId: Int) {
        router.openArtist(artistId)
    }

    fun onClickAlbumItem(albumId: Int) {
        router.openAlbum(albumId)
    }

    fun onClickTrackItem(tracks: List<Track>, position: Int) {
        playerInteractor.start(tracks, position)
    }

    fun onClickPlaylistItem(playlistId: Int) {
        router.openPlaylist(playlistId)
    }

}
