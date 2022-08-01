package com.parabola.newtone.mvp.presenter.fx

import com.parabola.domain.interactor.player.AudioEffectsInteractor
import com.parabola.newtone.di.app.AppComponent
import com.parabola.newtone.mvp.view.fx.FxAudioSettingsView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class FxAudioSettingsPresenter(appComponent: AppComponent) : MvpPresenter<FxAudioSettingsView>() {

    @Inject
    lateinit var fxInteractor: AudioEffectsInteractor

    private val disposables = CompositeDisposable()


    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        //BASS BOOST
        if (fxInteractor.isBassBoostAvailable) {
            viewState.setBassBoostSwitch(fxInteractor.isBassBoostEnabled)
        } else {
            viewState.hideBassBoostPanel()
        }

        //VIRTUALIZER
        if (fxInteractor.isVirtualizerAvailable) {
            viewState.setVirtualizerSwitch(fxInteractor.isVirtualizerEnabled)
        } else {
            viewState.hideVirtualizerPanel()
        }

        disposables.addAll(
            observePlaybackSpeed(), observePlaybackSpeedEnabling(),
            observePlaybackPitch(), observerPlaybackPitchEnabling(),
            observeVirtualizerLevel(),
            observeBassBoostLevel()
        )
    }

    override fun onDestroy() {
        disposables.dispose()
    }


    private fun observePlaybackSpeed(): Disposable {
        return fxInteractor.observePlaybackSpeed()
            .subscribe { savedSpeed ->
                viewState.setPlaybackSpeedText(savedSpeed)
                viewState.setPlaybackSpeedSeekbar((savedSpeed * 100f - 50).toInt())
            }
    }

    private fun observePlaybackSpeedEnabling(): Disposable {
        return fxInteractor.observeIsPlaybackSpeedEnabled()
            .subscribe(viewState::setPlaybackSpeedSwitch)
    }

    private fun observePlaybackPitch(): Disposable {
        return fxInteractor.observePlaybackPitch()
            .subscribe { savedPitch ->
                viewState.setPlaybackPitchText(savedPitch)
                viewState.setPlaybackPitchSeekbar((savedPitch * 100f - 50).toInt())
            }
    }

    private fun observerPlaybackPitchEnabling(): Disposable {
        return fxInteractor.observeIsPlaybackPitchEnabled()
            .subscribe(viewState::setPlaybackPitchSwitch)
    }

    private fun observeBassBoostLevel(): Disposable {
        return fxInteractor.observeBassBoostLevel()
            .subscribe { currentLevel -> viewState.setBassBoostSeekbar(currentLevel.toInt()) }
    }

    private fun observeVirtualizerLevel(): Disposable {
        return fxInteractor.observeVirtualizerLevel()
            .subscribe { currentLevel -> viewState.setVirtualizerSeekbar(currentLevel.toInt()) }
    }


    fun onPlaybackSpeedSwitchClick(enable: Boolean) {
        fxInteractor.setPlaybackSpeedEnabled(enable)
    }

    fun onPlaybackPitchSwitchClick(enable: Boolean) {
        fxInteractor.setPlaybackPitchEnabled(enable)
    }

    fun onPlaybackSpeedProgressChanged(progress: Int) {
        val realSpeed = (progress + 50) / 100f
        if (realSpeed != fxInteractor.savedPlaybackSpeed) {
            fxInteractor.savedPlaybackSpeed = realSpeed
        }
    }

    fun onPlaybackPitchProgressChanged(progress: Int) {
        val realPitch = (progress + 50) / 100f
        if (realPitch != fxInteractor.savedPlaybackPitch) {
            fxInteractor.savedPlaybackPitch = realPitch
        }
    }

    fun onBassBoostProgressChange(progress: Int) {
        fxInteractor.setBassBoostLevel(progress.toShort())
        viewState.setBassBoostSeekbar(progress)
    }

    fun onBassBoostSwitchClick(enable: Boolean) {
        fxInteractor.setBassBoostEnable(enable)
        viewState.setBassBoostSwitch(enable)
    }

    fun onVirtualizerProgressChange(progress: Int) {
        fxInteractor.setVirtualizerLevel(progress.toShort())
        viewState.setVirtualizerSeekbar(progress)
    }

    fun onVirtualizerSwitchClick(enable: Boolean) {
        fxInteractor.setVirtualizerEnable(enable)
        viewState.setVirtualizerSwitch(enable)
    }

}
