package com.parabola.newtone.mvp.view

import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface TabTrackView : MvpView {
    fun setCurrentTrack(trackId: Int)
    fun refreshTracks(tracks: List<Track>)

    fun setItemViewSettings(viewSettings: TrackItemView)
    fun setItemDividerShowing(showed: Boolean)

    fun setSectionShowing(enable: Boolean)

    fun removeTrack(trackId: Int)
}
