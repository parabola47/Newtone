package com.parabola.newtone.mvp.view

import com.parabola.domain.model.Artist
import com.parabola.domain.settings.ViewSettingsInteractor.ArtistItemView
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface TabArtistView : MvpView {
    fun refreshArtists(artists: List<Artist>)
    fun setItemViewSettings(viewSettings: ArtistItemView)
    fun setItemDividerShowing(showed: Boolean)

    fun setSectionShowing(enable: Boolean)
}
