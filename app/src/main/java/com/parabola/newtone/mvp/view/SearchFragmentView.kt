package com.parabola.newtone.mvp.view

import com.parabola.domain.model.Album
import com.parabola.domain.model.Artist
import com.parabola.domain.model.Playlist
import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface SearchFragmentView : MvpView {
    @OneExecution
    fun focusOnSearchView()

    fun refreshArtists(artists: List<Artist>)
    fun refreshAlbums(albums: List<Album>)
    fun refreshTracks(tracks: List<Track>)
    fun refreshPlaylists(playlists: List<Playlist>)

    fun setTrackItemViewSettings(trackItemView: TrackItemView)
    fun setItemDividerShowing(showed: Boolean)

    fun clearAllLists()

    fun setLoadDataProgressBarVisibility(visible: Boolean)
}
