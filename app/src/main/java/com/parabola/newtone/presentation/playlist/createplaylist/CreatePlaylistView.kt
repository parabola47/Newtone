package com.parabola.newtone.presentation.playlist.createplaylist

import moxy.MvpView
import moxy.viewstate.strategy.alias.OneExecution

@OneExecution
interface CreatePlaylistView : MvpView {
    fun focusOnInputField()

    fun showPlaylistTitleIsEmptyError()
    fun showPlaylistTitleAlreadyExistsError()

    fun closeScreen()
}
