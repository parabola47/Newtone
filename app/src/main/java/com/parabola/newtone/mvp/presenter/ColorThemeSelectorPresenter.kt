package com.parabola.newtone.mvp.presenter;

import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.ColorTheme;
import com.parabola.domain.settings.ViewSettingsInteractor.PrimaryColor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.ColorThemeSelectorView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;


@InjectViewState
public class ColorThemeSelectorPresenter extends MvpPresenter<ColorThemeSelectorView> {

    @Inject MainRouter router;
    @Inject ViewSettingsInteractor viewSettingsInteractor;

    private final CompositeDisposable disposables = new CompositeDisposable();


    public ColorThemeSelectorPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        disposables.addAll(
                observeColorTheme(),
                observePrimaryColor());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeColorTheme() {
        return viewSettingsInteractor.observeColorTheme()
                .subscribe(getViewState()::setDarkLightTheme);
    }

    private Disposable observePrimaryColor() {
        return viewSettingsInteractor.observePrimaryColor()
                .subscribe(getViewState()::setPrimaryColor);
    }


    public void onDarkLightSelection(ColorTheme colorTheme) {
        viewSettingsInteractor.setColorTheme(colorTheme);
    }

    public void onPrimaryColorSelection(PrimaryColor primaryColor) {
        viewSettingsInteractor.setPrimaryColor(primaryColor);
    }

    public void onClickBackButton() {
        router.goBack();
    }

}
