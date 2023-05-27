package com.parabola.newtone.presentation.playlist.renameplaylist

import com.parabola.domain.exception.AlreadyExistsException
import com.parabola.domain.repository.PlaylistRepository
import com.parabola.newtone.di.app.AppComponent
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.CallbackCompletableObserver
import io.reactivex.internal.observers.ConsumerSingleObserver
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class RenamePlaylistPresenter(
    appComponent: AppComponent,
    private val playlistId: Int,
) : MvpPresenter<RenamePlaylistView>() {

    @Inject
    lateinit var playlistRepo: PlaylistRepository


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        viewState.focusOnInputField()
        playlistRepo.getById(playlistId)
            .subscribe(
                ConsumerSingleObserver(
                    { playlist ->
                        viewState.setPlaylistTitle(playlist.title)
                        viewState.setTitleSelected()
                    },
                    Functions.ERROR_CONSUMER,
                )
            )
    }

    fun onClickRenamePlaylist(newPlaylistTitle: String) {
        val newPlaylistTitleFormatted = newPlaylistTitle.trim { it <= ' ' }
        if (newPlaylistTitleFormatted.isEmpty()) {
            viewState.showPlaylistTitleIsEmptyError()
            return
        }

        playlistRepo.rename(playlistId, newPlaylistTitleFormatted)
            .subscribe(
                CallbackCompletableObserver(
                    { error ->
                        if (error is AlreadyExistsException)
                            viewState.showPlaylistTitleAlreadyExistsError()
                    },
                    { viewState.closeScreen() },
                )
            )
    }

}
