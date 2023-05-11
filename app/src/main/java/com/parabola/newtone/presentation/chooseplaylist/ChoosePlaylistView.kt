package com.parabola.newtone.presentation.chooseplaylist

import com.parabola.domain.model.Playlist
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface ChoosePlaylistView : MvpView {
    fun refreshPlaylists(playlists: List<Playlist>)

    fun closeScreen()
}
