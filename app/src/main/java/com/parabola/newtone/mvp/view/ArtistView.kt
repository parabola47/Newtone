package com.parabola.newtone.mvp.view

import com.parabola.domain.model.Album
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface ArtistView : MvpView {
    fun setArtistName(artistName: String)
    fun setTracksCount(tracksCount: Int)
    fun setAlbumsCount(albumsCount: Int)

    fun refreshAlbums(albums: List<Album>)

    fun setAlbumViewSettings(albumViewSettings: AlbumItemView)
    fun setItemDividerShowing(showed: Boolean)
}
