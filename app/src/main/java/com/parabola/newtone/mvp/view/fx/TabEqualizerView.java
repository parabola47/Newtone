package com.parabola.newtone.mvp.view.fx;

import com.parabola.domain.interactor.player.AudioEffectsInteractor.EqBand;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface TabEqualizerView extends MvpView {
    void setEqChecked(boolean checked);
    void setMaxEqLevel(int level);
    void setMinEqLevel(int level);
    void refreshBands(List<EqBand> bands);
}
