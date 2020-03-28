package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.interactor.player.PlayerSetting;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.SettingView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public class SettingPresenter extends MvpPresenter<SettingView> {
    @Inject MainRouter router;

    @Inject PlayerSetting playerSetting;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public SettingPresenter(AppComponent appComponent) {
        appComponent.inject(this);

        disposables.addAll(
                observeIsNotificationBackgroundColorized(),
                observeNotificationArtworkShow());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
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

    public void onClickNotificationColorSetting() {
        playerSetting.setNotificationBackgroundColorized(!playerSetting.isNotificationBackgroundColorized());
    }

    public void onClickNotificationArtworkShowSetting() {
        playerSetting.setNotificationArtworkShow(!playerSetting.isNotificationArtworkShow());
    }

    public void onClickTrackItemViewSettings() {
        router.openTrackItemDisplaySettings();
    }

    public void onClickPrivacyPolicy() {
        router.openPrivacyPolicyWebPage();
    }

    public void onClickNewtoneTenTimes() {
        router.openNewtoneDialog();
    }
}
