package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface FolderView extends MvpView {
    void setFolderPath(String folderPath);

    void refreshTracks(List<Track> tracks);
    void setSectionShowing(boolean enable);

    void setCurrentTrack(int trackId);
}
