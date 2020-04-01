package com.parabola.newtone.mvp.view.fx;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

@AddToEndSingle
public interface FxAudioSettingsView extends MvpView {

    void setPlaybackSpeedSwitch(boolean enabled);
    void setPlaybackSpeedSeekbar(int progress);
    void setPlaybackSpeedText(float speed);


    void setPlaybackPitchSwitch(boolean enabled);
    void setPlaybackPitchSeekbar(int progress);
    void setPlaybackPitchText(float pitch);


    void hideBassBoostPanel();
    void setBassBoostSeekbar(int currentLevel);
    void setBassBoostSwitch(boolean bassBoostEnabled);


    void hideVirtualizerPanel();
    void setVirtualizerSeekbar(int currentLevel);
    void setVirtualizerSwitch(boolean virtualizerEnabled);
}
