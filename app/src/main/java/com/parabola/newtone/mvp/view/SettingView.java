package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface SettingView extends MvpView {
    void setNotificationColorSwitchChecked(boolean isChecked);
    void setNotificationArtworkSwitchChecked(boolean isChecked);
}
