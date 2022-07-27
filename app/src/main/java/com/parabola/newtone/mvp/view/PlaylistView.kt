package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

@AddToEndSingle
public interface PlaylistView extends MvpView {

    void setPlaylistChangerActivation(boolean activate);

    void setTracksCount(int playlistSize);
    void setPlaylistTitle(String playlistTitle);
    void refreshTracks(List<Track> tracks);
    void setItemViewSettings(TrackItemView viewSettings);
    void setItemDividerShowing(boolean showed);

    void setCurrentTrack(int trackId);

}
