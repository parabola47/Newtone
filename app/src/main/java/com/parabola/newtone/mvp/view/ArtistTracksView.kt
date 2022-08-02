package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

@AddToEndSingle
public interface ArtistTracksView extends MvpView {
    void refreshTracks(List<Track> tracks);
    void setItemViewSettings(TrackItemView viewSettings);
    void setItemDividerShowing(boolean showed);
    void setSectionShowing(boolean enable);

    void setArtistName(String artistName);
    void setTracksCountTxt(String tracksCountStr);

    void setCurrentTrack(int trackId);

}
