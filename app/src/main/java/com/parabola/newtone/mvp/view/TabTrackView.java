package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

@AddToEndSingle
public interface TabTrackView extends MvpView {

    void refreshTracks(List<Track> tracks);
    void setItemViewSettings(TrackItemView trackItemViewSettings);
    void setItemDividerShowing(boolean showed);

    void setCurrentTrack(int trackId);
    void setSectionShowing(boolean enable);

    void removeTrack(int trackId);
}
