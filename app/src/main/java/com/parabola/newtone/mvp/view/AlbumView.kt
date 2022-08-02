package com.parabola.newtone.mvp.view

import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface AlbumView : MvpView {
    fun setAlbumTitle(albumTitle: String)
    fun setAlbumArtist(artistName: String)
    fun setAlbumArt(artCover: Any?)

    fun refreshTracks(tracks: List<Track>)
    fun setItemViewSettings(itemViewSettings: TrackItemView)
    fun setItemDividerShowing(showed: Boolean)

    fun setCurrentTrack(trackId: Int)
}
