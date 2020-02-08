package com.parabola.newtone.mvp.presenter.fx;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.interactors.player.AudioEffectsInteractor;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.fx.FxAudioSettingsView;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

@InjectViewState
public final class FxAudioSettingsPresenter extends MvpPresenter<FxAudioSettingsView> {

    @Inject AudioEffectsInteractor fxInteractor;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public FxAudioSettingsPresenter(AppComponent appComponent) {
        appComponent.inject(this);

        float realSpeed = fxInteractor.getPlaybackSpeed();
        getViewState().setPlaybackSpeedSeekbar((int) ((realSpeed * 100f) - 50));

        float realPitch = fxInteractor.getPlaybackPitch();
        getViewState().setPlaybackPitchSeekbar((int) ((realPitch * 100f) - 50));

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
                observePlaybackSpeed(),
                observePlaybackPitch());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observePlaybackSpeed() {
        return fxInteractor.observePlaybackSpeed()
                .subscribe(getViewState()::setPlaybackSpeedText);
    }

    private Disposable observePlaybackPitch() {
        return fxInteractor.observePlaybackPitch()
                .subscribe(getViewState()::setPlaybackPitchText);
    }

    public void onPlaybackSpeedProgressChanged(int progress) {
        float realSpeed = (progress + 50) / 100f;
        if (realSpeed != fxInteractor.getPlaybackSpeed()) {
            fxInteractor.setPlaybackSpeed(realSpeed);
        }
    }

    public void onPlaybackPitchProgressChanged(int progress) {
        float realPitch = (progress + 50) / 100f;
        if (realPitch != fxInteractor.getPlaybackPitch()) {
            fxInteractor.setPlaybackPitch(realPitch);
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
