package com.parabola.newtone.mvp.view

import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface ArtistTracksView : MvpView {
    fun refreshTracks(tracks: List<Track>)
    fun setItemViewSettings(viewSettings: TrackItemView)
    fun setItemDividerShowing(showed: Boolean)
    fun setSectionShowing(enable: Boolean)

    fun setArtistName(artistName: String)
    fun setTracksCountTxt(tracksCountStr: String)

    fun setCurrentTrack(trackId: Int)
}
