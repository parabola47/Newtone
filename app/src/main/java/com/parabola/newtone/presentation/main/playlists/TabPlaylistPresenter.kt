package com.parabola.newtone.presentation.main.playlists

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.repository.PlaylistRepository
import com.parabola.domain.repository.TrackRepository
import com.parabola.domain.settings.ViewSettingsInteractor
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.ConsumerSingleObserver
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class TabPlaylistPresenter(appComponent: AppComponent) : MvpPresenter<TabPlaylistView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var playerInteractor: PlayerInteractor

    @Inject
    lateinit var playlistRepo: PlaylistRepository

    @Inject
    lateinit var trackRepo: TrackRepository

    @Inject
    lateinit var schedulers: SchedulerProvider

    @Inject
    lateinit var viewSettingsInteractor: ViewSettingsInteractor

    private val disposables = CompositeDisposable()


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        disposables.addAll(
            observePlaylistsUpdates(),
            observeIsItemDividerShowed(),
            observeTrackDeleting()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun observePlaylistsUpdates(): Disposable {
        return playlistRepo.observePlaylistsUpdates()
            .flatMapSingle { playlistRepo.all }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(viewState::refreshPlaylists)
    }

    private fun observeIsItemDividerShowed(): Disposable {
        return viewSettingsInteractor.observeIsItemDividerShowed()
            .subscribe(viewState::setItemDividerShowing)
    }

    private fun observeTrackDeleting(): Disposable {
        return trackRepo.observeTrackDeleting()
            .flatMapSingle { playlistRepo.all }
            .observeOn(schedulers.ui())
            .subscribe(viewState::refreshPlaylists)
    }


    fun onClickPlaylistItem(selectedPlaylistId: Int) {
        router.openPlaylist(selectedPlaylistId)
    }

    fun onClickMenuRename(playlistId: Int) {
        router.openRenamePlaylistDialog(playlistId)
    }

    fun onClickMenuShuffle(playlistId: Int) {
        trackRepo.getByPlaylist(playlistId)
            .subscribe(
                ConsumerSingleObserver(
                    playerInteractor::startInShuffleMode,
                    Functions.ERROR_CONSUMER
                )
            )
    }

    fun onClickMenuDelete(deletedPlaylistId: Int) {
        playlistRepo.remove(deletedPlaylistId)
            .subscribe()
    }

    fun onClickRecentlyAdded() {
        router.openRecentlyAdded()
    }

    fun onClickFavourites() {
        router.openFavourites()
    }

    fun onClickQueue() {
        router.openQueue()
    }

    fun onClickFolders() {
        router.openFoldersList()
    }

}
