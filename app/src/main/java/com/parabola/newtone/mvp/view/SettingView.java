package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface SettingView extends MvpView {
    void setNotificationColorSwitchChecked(boolean isChecked);
    void setNotificationArtworkSwitchChecked(boolean isChecked);
}
