package com.parabola.newtone.presentation.playlist.recentlyadded

import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface RecentlyAddedPlaylistView : MvpView {
    fun setCurrentTrack(trackId: Int)
    fun refreshTracks(tracks: List<Track>)

    fun setItemViewSettings(viewSettings: TrackItemView)
    fun setItemDividerShowing(showed: Boolean)

    fun removeTrack(trackId: Int)
}
