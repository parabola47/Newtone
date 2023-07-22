package com.parabola.newtone.presentation.playlist.favourites

import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface FavouritesPlaylistView : MvpView {
    fun setPlaylistChangerActivation(activate: Boolean)

    fun setCurrentTrack(trackId: Int)
    fun refreshTracks(tracks: List<Track>)

    fun setItemViewSettings(viewSettings: TrackItemView)
    fun setItemDividerShowing(showed: Boolean)
}
