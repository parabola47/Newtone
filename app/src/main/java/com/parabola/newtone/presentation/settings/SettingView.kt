package com.parabola.newtone.presentation.settings

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface SettingView : MvpView {
    fun setNotificationColorSwitchChecked(isChecked: Boolean)
    fun setNotificationArtworkSwitchChecked(isChecked: Boolean)
    fun setShowListItemDividerSwitchChecked(isChecked: Boolean)
}
