package com.parabola.newtone.presentation.main

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface StartView : MvpView {
    fun setPermissionPanelVisibility(visible: Boolean)
}
