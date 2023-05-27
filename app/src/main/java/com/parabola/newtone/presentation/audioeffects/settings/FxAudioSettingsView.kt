package com.parabola.newtone.presentation.audioeffects.settings

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface FxAudioSettingsView : MvpView {
    fun setPlaybackSpeedSwitch(enabled: Boolean)
    fun setPlaybackSpeedSeekbar(progress: Int)
    fun setPlaybackSpeedText(speed: Float)

    fun setPlaybackPitchSwitch(enabled: Boolean)
    fun setPlaybackPitchSeekbar(progress: Int)
    fun setPlaybackPitchText(pitch: Float)

    fun hideBassBoostPanel()
    fun setBassBoostSeekbar(currentLevel: Int)
    fun setBassBoostSwitch(bassBoostEnabled: Boolean)

    fun hideVirtualizerPanel()
    fun setVirtualizerSeekbar(currentLevel: Int)
    fun setVirtualizerSwitch(virtualizerEnabled: Boolean)
}
