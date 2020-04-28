package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.interactor.player.PlayerSetting;
import com.parabola.domain.repository.ResourceRepository;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.ColorTheme;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.SettingView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class SettingPresenter extends MvpPresenter<SettingView> {
    @Inject MainRouter router;

    @Inject PlayerSetting playerSetting;
    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject ResourceRepository resourceRepo;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public SettingPresenter(AppComponent appComponent) {
        appComponent.inject(this);

        disposables.addAll(
                observeIsNotificationBackgroundColorized(),
                observeNotificationArtworkShow(),
                observeColorTheme());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeColorTheme() {
        return viewSettingsInteractor.observeColorTheme()
                .doOnNext(getViewState()::setCurrentColorTheme)
                .map(colorTheme -> {
                    switch (colorTheme) {
                        case DARK: return resourceRepo.getString(R.string.setting_color_theme_desc_dark);
                        case LIGHT: return resourceRepo.getString(R.string.setting_color_theme_desc_light);
                        default: return "";
                    }
                })
                .subscribe(getViewState()::setColorThemeDescription);
    }

    private Disposable observeIsNotificationBackgroundColorized() {
        return playerSetting.observeIsNotificationBackgroundColorized()
                .subscribe(getViewState()::setNotificationColorSwitchChecked);
    }

    private Disposable observeNotificationArtworkShow() {
        return playerSetting.observeNotificationArtworkShow()
                .subscribe(getViewState()::setNotificationArtworkSwitchChecked);
    }

    public void onClickBack() {
        router.goBack();
    }

    public void onSelectColorTheme(ColorTheme colorTheme) {
        viewSettingsInteractor.setColorTheme(colorTheme);
    }

    public void onClickNotificationColorSetting() {
        playerSetting.setNotificationBackgroundColorized(!playerSetting.isNotificationBackgroundColorized());
    }

    public void onClickNotificationArtworkShowSetting() {
        playerSetting.setNotificationArtworkShow(!playerSetting.isNotificationArtworkShow());
    }

    public void onClickTrackItemViewSettings() {
        router.openTrackItemDisplaySettings();
    }

    public void onClickAlbumItemViewSettings() {
        router.openAlbumItemDisplaySettings();
    }

    public void onClickArtistItemViewSettings() {
        router.openArtistItemDisplaySettings();
    }

    public void onClickPrivacyPolicy() {
        router.openPrivacyPolicyWebPage();
    }

    public void onClickNewtoneTenTimes() {
        router.openNewtoneDialog();
    }

}
