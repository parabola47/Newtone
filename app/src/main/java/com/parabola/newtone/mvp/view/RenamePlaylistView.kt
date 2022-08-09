package com.parabola.newtone.mvp.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.OneExecution

@OneExecution
interface RenamePlaylistView : MvpView {
    fun focusOnInputField()

    fun setPlaylistTitle(playlistTitle: String)
    fun setTitleSelected()

    fun closeScreen()

    fun showPlaylistTitleIsEmptyError()
    fun showPlaylistTitleAlreadyExistsError()
}
