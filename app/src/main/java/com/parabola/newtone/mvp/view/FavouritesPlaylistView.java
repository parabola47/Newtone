package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.OneExecution;

@AddToEndSingle
public interface FavouritesPlaylistView extends MvpView {

    void setPlaylistChangerActivation(boolean activate);

    void refreshTracks(List<Track> tracks);
    void setItemViewSettings(TrackItemView viewSettings);
    void setItemDividerShowing(boolean showed);

    void setCurrentTrack(int trackId);

    @OneExecution
    void showToast(String message);

    //трек удаляется только если на данной позиции находится указанный trackId
    @OneExecution
    void removeTrack(int trackId, int position);

}
