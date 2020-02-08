package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

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
}
