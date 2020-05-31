package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Album;
import com.parabola.domain.model.Artist;
import com.parabola.domain.model.Playlist;
import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.OneExecution;

@AddToEndSingle
public interface SearchFragmentView extends MvpView {

    @OneExecution
    void focusOnSearchView();

    void refreshArtists(List<Artist> artists);
    void refreshAlbums(List<Album> albums);

    void refreshTracks(List<Track> tracks);
    void setTrackItemViewSettings(TrackItemView trackItemView);
    void setItemDividerShowing(boolean showed);

    void refreshPlaylists(List<Playlist> playlists);

    void clearAllLists();

    void setLoadDataProgressBarVisibility(boolean visible);
}
