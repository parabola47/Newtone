package com.parabola.newtone.presentation.main.playlists

import com.parabola.domain.model.Playlist
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface TabPlaylistView : MvpView {
    fun refreshPlaylists(playlists: List<Playlist>)

    fun setItemDividerShowing(showed: Boolean)
}
