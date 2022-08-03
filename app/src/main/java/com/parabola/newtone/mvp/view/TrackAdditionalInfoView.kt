package com.parabola.newtone.mvp.view

import com.parabola.domain.model.Track
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface TrackAdditionalInfoView : MvpView {
    fun setTrack(track: Track)

    @OneExecution
    fun closeScreen()
}