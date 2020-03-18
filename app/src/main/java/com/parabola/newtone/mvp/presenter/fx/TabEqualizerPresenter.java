package com.parabola.newtone.mvp.presenter.fx;

import com.parabola.domain.interactor.player.AudioEffectsInteractor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.fx.TabEqualizerView;

import javax.inject.Inject;

import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class TabEqualizerPresenter extends MvpPresenter<TabEqualizerView> {

    @Inject AudioEffectsInteractor fxInteractor;

    public TabEqualizerPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    public void onFirstViewAttach() {
        getViewState().setEqChecked(fxInteractor.isEqEnabled());
        getViewState().setMaxEqLevel(fxInteractor.getMaxEqBandLevel());
        getViewState().setMinEqLevel(fxInteractor.getMinEqBandLevel());
        getViewState().refreshBands(fxInteractor.getBands());
    }


    public void onClickEqSwitcher(boolean enabled) {
        fxInteractor.setEqEnable(enabled);
    }

    public void onChangeBandLevel(int bandId, short newLevel) {
        fxInteractor.setBandLevel(bandId, newLevel);
    }
}
