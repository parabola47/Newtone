package com.parabola.newtone.mvp.view.fx;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.parabola.domain.interactor.player.AudioEffectsInteractor.EqBand;

import java.util.List;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface TabEqualizerView extends MvpView {
    void setEqChecked(boolean checked);
    void setMaxEqLevel(int level);
    void setMinEqLevel(int level);
    void refreshBands(List<EqBand> bands);
}
