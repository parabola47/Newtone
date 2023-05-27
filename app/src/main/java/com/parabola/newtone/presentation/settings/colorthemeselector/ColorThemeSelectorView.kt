package com.parabola.newtone.presentation.settings.colorthemeselector

import com.parabola.domain.settings.ViewSettingsInteractor.ColorTheme
import com.parabola.domain.settings.ViewSettingsInteractor.PrimaryColor
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface ColorThemeSelectorView : MvpView {
    fun setDarkLightTheme(colorTheme: ColorTheme)
    fun setPrimaryColor(primaryColor: PrimaryColor)
}
