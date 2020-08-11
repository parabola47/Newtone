package com.parabola.newtone.mvp.view;

import com.parabola.domain.settings.ViewSettingsInteractor.ColorTheme;
import com.parabola.domain.settings.ViewSettingsInteractor.PrimaryColor;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

@AddToEndSingle
public interface ColorThemeSelectorView extends MvpView {

    void setDarkLightTheme(ColorTheme colorTheme);
    void setPrimaryColor(PrimaryColor primaryColor);
}
