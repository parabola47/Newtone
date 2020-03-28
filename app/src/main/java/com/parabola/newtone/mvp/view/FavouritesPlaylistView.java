package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface FavouritesPlaylistView extends MvpView {

    void refreshTracks(List<Track> tracks);
    void setItemViewSettings(TrackItemView viewSettings);

    void setCurrentTrack(int trackId);

    void clearItems();

}
