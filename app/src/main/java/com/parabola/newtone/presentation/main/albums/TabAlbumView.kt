package com.parabola.newtone.presentation.main.albums

import com.parabola.domain.model.Album
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface TabAlbumView : MvpView {
    fun refreshAlbums(albums: List<Album>)

    fun setAlbumViewSettings(viewSettings: AlbumItemView)
    fun setItemDividerShowing(showed: Boolean)

    fun setSectionShowing(enable: Boolean)
}
