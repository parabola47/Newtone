package com.parabola.newtone.mvp.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

@AddToEndSingle
interface TimeToSleepInfoView : MvpView {
    fun updateTimeToEndText(timeToEndText: String)

    @OneExecution
    fun closeScreen()
}
