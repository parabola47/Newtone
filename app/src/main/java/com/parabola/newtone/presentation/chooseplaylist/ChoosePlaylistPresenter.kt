package com.parabola.newtone.presentation.chooseplaylist

import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.repository.PlaylistRepository
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.observers.CallbackCompletableObserver
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class ChoosePlaylistPresenter(
    appComponent: AppComponent,
    private val trackIds: IntArray,
) : MvpPresenter<ChoosePlaylistView>() {

    @Inject
    lateinit var playlistRepo: PlaylistRepository

    @Inject
    lateinit var schedulers: SchedulerProvider

    @Inject
    lateinit var router: MainRouter

    private val disposables = CompositeDisposable()


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        disposables.addAll(observePlaylistsUpdates())
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


    fun onClickCreateNewPlaylist() {
        router.openCreatePlaylistDialog()
    }

    fun onClickPlaylistItem(playlistId: Int) {
        playlistRepo.addTracksToPlaylist(playlistId, *trackIds)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(CallbackCompletableObserver { viewState.closeScreen() })
    }

}
