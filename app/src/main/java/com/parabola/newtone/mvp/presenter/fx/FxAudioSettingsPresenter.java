package com.parabola.newtone.mvp.presenter.fx;

import com.parabola.domain.interactor.player.AudioEffectsInteractor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.fx.FxAudioSettingsView;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public final class FxAudioSettingsPresenter extends MvpPresenter<FxAudioSettingsView> {

    @Inject AudioEffectsInteractor fxInteractor;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public FxAudioSettingsPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }


    @Override
    protected void onFirstViewAttach() {
        float savedSpeed = fxInteractor.getSavedPlaybackSpeed();
        getViewState().setPlaybackSpeedSeekbar((int) ((savedSpeed * 100f) - 50));

        float savedPitch = fxInteractor.getSavedPlaybackPitch();
        getViewState().setPlaybackPitchSeekbar((int) ((savedPitch * 100f) - 50));

        //BASS BOOST
        if (fxInteractor.isBassBoostAvailable()) {
            int bassBoostMaxStrength = fxInteractor.getBassBoostMaxLevel();
            int bassBoostCurrentLevel = fxInteractor.getBassBoostCurrentLevel();
            getViewState().setMaxBassBoostSeekbar(bassBoostMaxStrength);
            getViewState().setBassBoostSwitch(fxInteractor.isBassBoostEnabled());
            getViewState().setBassBoostSeekbar(bassBoostCurrentLevel);
        } else {
            getViewState().hideBassBoostPanel();
        }

        //VIRTUALIZER
        if (fxInteractor.isVirtualizerAvailable()) {
            int virtualizerMaxStrength = fxInteractor.getVirtualizerMaxLevel();
            int virtualizerCurrentLevel = fxInteractor.getVirtualizerCurrentLevel();
            getViewState().setMaxVirtualizerSeekbar(virtualizerMaxStrength);
            getViewState().setVirtualizerSwitch(fxInteractor.isVirtualizerEnabled());
            getViewState().setVirtualizerSeekbar(virtualizerCurrentLevel);
        } else {
            getViewState().hideVirtualizerPanel();
        }

        disposables.addAll(
                observePlaybackSpeed(), observePlaybackSpeedEnabling(),
                observePlaybackPitch(), observerPlaybackPitchEnabling());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observePlaybackSpeed() {
        return fxInteractor.observePlaybackSpeed()
                .subscribe(getViewState()::setPlaybackSpeedText);
    }

    private Disposable observePlaybackSpeedEnabling() {
        return fxInteractor.observeIsPlaybackSpeedEnabled()
                .subscribe(getViewState()::setPlaybackSpeedSwitch);
    }

    private Disposable observePlaybackPitch() {
        return fxInteractor.observePlaybackPitch()
                .subscribe(getViewState()::setPlaybackPitchText);
    }

    private Disposable observerPlaybackPitchEnabling() {
        return fxInteractor.observeIsPlaybackPitchEnabled()
                .subscribe(getViewState()::setPlaybackPitchSwitch);
    }


    public void onPlaybackSpeedSwitchCheck(boolean enable) {
        fxInteractor.setPlaybackSpeedEnabled(enable);
    }


    public void onPlaybackPitchSwitchCheck(boolean enable) {
        fxInteractor.setPlaybackPitchEnabled(enable);
    }

    public void onPlaybackSpeedProgressChanged(int progress) {
        float realSpeed = (progress + 50) / 100f;
        if (realSpeed != fxInteractor.getSavedPlaybackSpeed()) {
            fxInteractor.setSavedPlaybackSpeed(realSpeed);
        }
    }

    public void onPlaybackPitchProgressChanged(int progress) {
        float realPitch = (progress + 50) / 100f;
        if (realPitch != fxInteractor.getSavedPlaybackPitch()) {
            fxInteractor.setSavedPlaybackPitch(realPitch);
        }
    }


    public void onBassBoostProgressChange(int progress) {
        fxInteractor.setBassBoostLevel((short) progress);
    }

    public void onBassBoostSwitchCheck(boolean enable) {
        fxInteractor.setBassBoostEnable(enable);
    }

    public void onVirtualizerProgressChange(int progress) {
        fxInteractor.setVirtualizerLevel((short) progress);
    }


    public void onVirtualizerSwitchCheck(boolean enable) {
        fxInteractor.setVirtualizerEnable(enable);
    }
}
