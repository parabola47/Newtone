package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface MainView extends MvpView {

    void setTrackTitle(String trackTitle);
    void setArtistName(String artist);

    void setPlaybackButtonAsPause();
    void setPlaybackButtonAsPlay();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showBottomSlider();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void hideBottomSlider();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void requestStoragePermissionDialog();

    void setDurationMax(int max);
    void setDurationProgress(int progress);
}
