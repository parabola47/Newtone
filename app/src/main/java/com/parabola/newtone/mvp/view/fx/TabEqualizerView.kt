package com.parabola.newtone.mvp.view.fx

import com.parabola.domain.interactor.player.AudioEffectsInteractor.EqBand
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface TabEqualizerView : MvpView {

    fun setEqChecked(checked: Boolean)
    fun setMaxEqLevel(level: Int)
    fun setMinEqLevel(level: Int)
    fun refreshBands(bands: List<EqBand>)

}
