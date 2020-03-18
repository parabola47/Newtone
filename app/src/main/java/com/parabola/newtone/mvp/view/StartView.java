package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface StartView extends MvpView {
    void setPermissionPanelVisibility(boolean visible);
}
