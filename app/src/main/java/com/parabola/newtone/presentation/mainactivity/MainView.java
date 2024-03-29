package com.parabola.newtone.presentation.mainactivity;

import com.parabola.domain.settings.ViewSettingsInteractor.PrimaryColor;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;
import moxy.viewstate.strategy.alias.AddToEndSingle;

@AddToEndSingle
public interface MainView extends MvpView {

    void refreshPrimaryColor(PrimaryColor primaryColor);

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

    void setPlayerBarOpacity(float alpha);
    void setPlayerBarVisibility(boolean visible);

}
