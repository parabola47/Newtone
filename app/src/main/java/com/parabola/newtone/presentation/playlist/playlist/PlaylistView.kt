package com.parabola.newtone.presentation.playlist.playlist

import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface PlaylistView : MvpView {
    fun setPlaylistTitle(playlistTitle: String)

    fun setPlaylistChangerActivation(activate: Boolean)

    fun refreshTracks(tracks: List<Track>)
    fun setTracksCount(playlistSize: Int)
    fun setCurrentTrack(trackId: Int)

    fun setItemViewSettings(viewSettings: TrackItemView)
    fun setItemDividerShowing(showed: Boolean)
}
