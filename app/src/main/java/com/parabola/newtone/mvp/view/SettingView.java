package com.parabola.newtone.mvp.view;

import com.parabola.domain.settings.ViewSettingsInteractor.ColorTheme;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

@AddToEndSingle
public interface SettingView extends MvpView {
    void setColorThemeDescription(String colorThemeDescription);
    void setNotificationColorSwitchChecked(boolean isChecked);
    void setNotificationArtworkSwitchChecked(boolean isChecked);

    void setCurrentColorTheme(ColorTheme colorTheme);
}
