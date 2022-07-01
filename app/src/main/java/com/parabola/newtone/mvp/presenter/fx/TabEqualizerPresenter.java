package com.parabola.newtone.mvp.presenter.fx;

import com.parabola.domain.interactor.player.AudioEffectsInteractor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.fx.TabEqualizerView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class TabEqualizerPresenter extends MvpPresenter<TabEqualizerView> {

    @Inject AudioEffectsInteractor fxInteractor;
    @Inject MainRouter router;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public TabEqualizerPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    public void onFirstViewAttach() {
        getViewState().setMaxEqLevel(fxInteractor.getMaxEqBandLevel());
        getViewState().setMinEqLevel(fxInteractor.getMinEqBandLevel());
        getViewState().refreshBands(fxInteractor.getBands());
        disposables.addAll(
                observeEqEnabling());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observeEqEnabling() {
        return fxInteractor.observeEqEnabling()
                .subscribe(enabled -> {
                    getViewState().setEqChecked(enabled);
                    getViewState().refreshBands(fxInteractor.getBands());
                });
    }


    public void onClickEqSwitcher(boolean enabled) {
        fxInteractor.setEqEnable(enabled);
    }

    public void onChangeBandLevel(int bandId, int newLevel) {
        fxInteractor.setBandLevel(bandId, newLevel);
    }

    public void onClickShowPresets() {
        router.openEqPresetsSelectorDialog();
    }

}
