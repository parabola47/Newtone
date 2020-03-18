package com.parabola.newtone.mvp.view.fx;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface FxAudioSettingsView extends MvpView {

    void setPlaybackSpeedSwitch(boolean enabled);
    void setPlaybackPitchSwitch(boolean enabled);


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
