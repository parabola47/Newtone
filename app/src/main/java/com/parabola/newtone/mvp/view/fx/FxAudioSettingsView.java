package com.parabola.newtone.mvp.view.fx;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface FxAudioSettingsView extends MvpView {

    @StateStrategyType(OneExecutionStateStrategy.class)
    void setPlaybackSpeedSeekbar(int progress);
    void setPlaybackSpeedText(float speed);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void setPlaybackPitchSeekbar(int progress);
    void setPlaybackPitchText(float pitch);


    void hideBassBoostPanel();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void setBassBoostSeekbar(int currentLevel);
    void setMaxBassBoostSeekbar(int maxStrength);
    void setBassBoostSwitch(boolean bassBoostEnabled);


    void hideVirtualizerPanel();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void setVirtualizerSeekbar(int currentLevel);
    void setMaxVirtualizerSeekbar(int maxStrength);
    void setVirtualizerSwitch(boolean virtualizerEnabled);
}
