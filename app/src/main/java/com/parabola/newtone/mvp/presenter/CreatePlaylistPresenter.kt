package com.parabola.newtone.mvp.presenter

import com.parabola.domain.exception.AlreadyExistsException
import com.parabola.domain.repository.PlaylistRepository
import com.parabola.domain.repository.ResourceRepository
import com.parabola.newtone.R
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.mvp.view.CreatePlaylistView
import com.parabola.newtone.ui.router.MainRouter
import io.reactivex.internal.observers.BiConsumerSingleObserver
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class CreatePlaylistPresenter(appComponent: AppComponent) :
    MvpPresenter<CreatePlaylistView>() {

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var playlistRepo: PlaylistRepository

    @Inject
    lateinit var resourceRepo: ResourceRepository


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        viewState.focusOnInputField()
    }

    fun onClickCreatePlaylist(newPlaylistTitle: String) {
        val newPlaylistTitleFormatted = newPlaylistTitle.trim { it <= ' ' }

        if (newPlaylistTitleFormatted.isEmpty()) {
            viewState.showPlaylistTitleIsEmptyError()
            return
        }

        playlistRepo.addNew(newPlaylistTitleFormatted)
            .subscribe(BiConsumerSingleObserver { playlist, error ->
                if (playlist != null) {
                    val toastText =
                        resourceRepo.getString(R.string.toast_playlist_created, playlist.title)
                    router.showToast(toastText)
                    viewState.closeScreen()
                } else if (error is AlreadyExistsException) {
                    viewState.showPlaylistTitleAlreadyExistsError()
                } else if (error != null) {
                    throw RuntimeException(error)
                }
            })
    }

}
