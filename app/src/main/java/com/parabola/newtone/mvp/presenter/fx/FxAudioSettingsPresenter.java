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
        //BASS BOOST
        if (fxInteractor.isBassBoostAvailable()) {
            getViewState().setBassBoostSwitch(fxInteractor.isBassBoostEnabled());
        } else {
            getViewState().hideBassBoostPanel();
        }

        //VIRTUALIZER
        if (fxInteractor.isVirtualizerAvailable()) {
            getViewState().setVirtualizerSwitch(fxInteractor.isVirtualizerEnabled());
        } else {
            getViewState().hideVirtualizerPanel();
        }

        disposables.addAll(
                observePlaybackSpeed(), observePlaybackSpeedEnabling(),
                observePlaybackPitch(), observerPlaybackPitchEnabling(),
                observeVirtualizerLevel(),
                observeBassBoostLevel());
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    private Disposable observePlaybackSpeed() {
        return fxInteractor.observePlaybackSpeed()
                .subscribe(savedSpeed -> {
                    getViewState().setPlaybackSpeedText(savedSpeed);
                    getViewState().setPlaybackSpeedSeekbar((int) ((savedSpeed * 100f) - 50));
                });
    }

    private Disposable observePlaybackSpeedEnabling() {
        return fxInteractor.observeIsPlaybackSpeedEnabled()
                .subscribe(getViewState()::setPlaybackSpeedSwitch);
    }

    private Disposable observePlaybackPitch() {
        return fxInteractor.observePlaybackPitch()
                .subscribe(savedPitch -> {
                    getViewState().setPlaybackPitchText(savedPitch);
                    getViewState().setPlaybackPitchSeekbar((int) ((savedPitch * 100f) - 50));
                });
    }

    private Disposable observerPlaybackPitchEnabling() {
        return fxInteractor.observeIsPlaybackPitchEnabled()
                .subscribe(getViewState()::setPlaybackPitchSwitch);
    }


    private Disposable observeBassBoostLevel() {
        return fxInteractor.observeBassBoostLevel()
                .subscribe(getViewState()::setBassBoostSeekbar);
    }

    private Disposable observeVirtualizerLevel() {
        return fxInteractor.observeVirtualizerLevel()
                .subscribe(getViewState()::setVirtualizerSeekbar);
    }


    public void onPlaybackSpeedSwitchClick(boolean enable) {
        fxInteractor.setPlaybackSpeedEnabled(enable);
    }


    public void onPlaybackPitchSwitchClick(boolean enable) {
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
        getViewState().setBassBoostSeekbar(progress);
    }

    public void onBassBoostSwitchClick(boolean enable) {
        fxInteractor.setBassBoostEnable(enable);
        getViewState().setBassBoostSwitch(enable);
    }

    public void onVirtualizerProgressChange(int progress) {
        fxInteractor.setVirtualizerLevel((short) progress);
    }


    public void onVirtualizerSwitchClick(boolean enable) {
        fxInteractor.setVirtualizerEnable(enable);
        getViewState().setVirtualizerSwitch(enable);
    }
}
